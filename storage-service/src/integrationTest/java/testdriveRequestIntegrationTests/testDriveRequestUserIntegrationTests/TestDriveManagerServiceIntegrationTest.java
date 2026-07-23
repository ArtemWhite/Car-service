package testdriveRequestIntegrationTests.testDriveRequestUserIntegrationTests;

import domain.repository.testDriveRequestRepository.TestDriveRequestRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import testdriveRequestIntegrationTests.testDriveRequestMainIntegrationTests.TestDriveBaseIntegrationTest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TestDriveManagerServiceIntegrationTest extends TestDriveBaseIntegrationTest {

    @Autowired
    private TestDriveRequestRepository testDriveRepository;

    private String createTestDriveRequest() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("carId", testCarId);
        request.put("startTime", getFutureTime().toString());

        String response = mockMvc.perform(post("/api/client/test-drives")
                        .header("X-User-Id", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("id").asText();
    }

    @Test
    void shouldAssignManagerToRequest_Successfully() throws Exception {
        String requestId = createTestDriveRequest();

        mockMvc.perform(post("/api/manager/test-drives/{id}/assign", requestId)
                        .header("X-User-Id", managerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.managerId").value(managerId));
    }

    @Test
    void shouldFailAssignManager_WhenNotManager() throws Exception {
        String requestId = createTestDriveRequest();

        mockMvc.perform(post("/api/manager/test-drives/{id}/assign", requestId)
                        .header("X-User-Id", clientId))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldFailAssignManager_WhenAlreadyAssigned() throws Exception {
        String requestId = createTestDriveRequest();

        mockMvc.perform(post("/api/manager/test-drives/{id}/assign", requestId)
                        .header("X-User-Id", managerId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/manager/test-drives/{id}/assign", requestId)
                        .header("X-User-Id", managerId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetMyAssignedRequests_Successfully() throws Exception {
        String requestId = createTestDriveRequest();
        mockMvc.perform(post("/api/manager/test-drives/{id}/assign", requestId)
                        .header("X-User-Id", managerId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/manager/test-drives/my")
                        .header("X-User-Id", managerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.testDrives.length()").value(1))
                .andExpect(jsonPath("$.testDrives[0].id").value(requestId));
    }

    @Test
    void shouldGetPendingRequests_Successfully() throws Exception {
        createTestDriveRequest();
        createTestDriveRequest();

        mockMvc.perform(get("/api/manager/test-drives/pending")
                        .header("X-User-Id", managerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.testDrives.length()").value(2));
    }

    @Test
    void shouldConfirmRequest_WithTime_Successfully() throws Exception {
        String requestId = createTestDriveRequest();
        LocalDateTime confirmTime = getFutureTime().plusHours(1);

        mockMvc.perform(post("/api/manager/test-drives/{id}/assign", requestId)
                        .header("X-User-Id", managerId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/manager/test-drives/{id}/confirm", requestId)
                        .header("X-User-Id", managerId)
                        .param("time", confirmTime.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void shouldFailConfirmRequest_WhenNotAssigned() throws Exception {
        String otherManagerId = UUID.randomUUID().toString();
        createUser(otherManagerId, "MANAGER");
        createManager(otherManagerId);

        String requestId = createTestDriveRequest();
        LocalDateTime confirmTime = getFutureTime().plusHours(1);

        mockMvc.perform(post("/api/manager/test-drives/{id}/confirm", requestId)
                        .header("X-User-Id", otherManagerId)
                        .param("time", confirmTime.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldCompleteTestDrive_Successfully() throws Exception {
        String requestId = createTestDriveRequest();
        LocalDateTime pastTime = LocalDateTime.now().minusHours(1);

        mockMvc.perform(post("/api/manager/test-drives/{id}/assign", requestId)
                        .header("X-User-Id", managerId))
                .andExpect(status().isOk());

        jdbcTemplate.update(
                "UPDATE test_drive_requests SET confirmed_time = ? WHERE id = ?::uuid",
                pastTime, UUID.fromString(requestId));

        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(post("/api/manager/test-drives/{id}/complete", requestId)
                        .header("X-User-Id", managerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void shouldMarkNoShow_Successfully() throws Exception {
        String requestId = createTestDriveRequest();

        mockMvc.perform(post("/api/manager/test-drives/{id}/assign", requestId)
                        .header("X-User-Id", managerId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/manager/test-drives/{id}/no-show", requestId)
                        .header("X-User-Id", managerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NO_SHOW"));
    }

    @Test
    void shouldFailMarkNoShow_WhenNotConfirmed() throws Exception {
        String requestId = createTestDriveRequest();

        mockMvc.perform(post("/api/manager/test-drives/{id}/no-show", requestId)
                        .header("X-User-Id", managerId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetMyAssignedRequests_EmptyList() throws Exception {
        mockMvc.perform(get("/api/manager/test-drives/my")
                        .header("X-User-Id", managerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.testDrives.length()").value(0));
    }

    @Test
    void shouldGetPendingRequests_WithFilters() throws Exception {
        createTestDriveRequest();
        createTestDriveRequest();

        mockMvc.perform(get("/api/manager/test-drives/pending")
                        .header("X-User-Id", managerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.testDrives.length()").value(2))
                .andExpect(jsonPath("$.testDrives[0].status").value("PENDING"));
    }

    @Test
    void shouldFailCompleteTestDrive_WhenFutureTime() throws Exception {
        String requestId = createTestDriveRequest();

        mockMvc.perform(post("/api/manager/test-drives/{id}/assign", requestId)
                        .header("X-User-Id", managerId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/manager/test-drives/{id}/complete", requestId)
                        .header("X-User-Id", managerId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetManagerStatistics() throws Exception {
        String requestId1 = createTestDriveRequest();
        String requestId2 = createTestDriveRequest();

        mockMvc.perform(post("/api/manager/test-drives/{id}/assign", requestId1)
                        .header("X-User-Id", managerId))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/manager/test-drives/{id}/assign", requestId2)
                        .header("X-User-Id", managerId))
                .andExpect(status().isOk());

        long count = testDriveRepository.countAssignedToManager(managerId);
        assertThat(count).isEqualTo(2);
    }

    @Test
    void shouldCheckManagerAvailability() throws Exception {
        String otherManagerId = UUID.randomUUID().toString();
        createUser(otherManagerId, "MANAGER");
        createManager(otherManagerId);

        for (int i = 0; i < 5; i++) {
            String requestId = createTestDriveRequest();
            mockMvc.perform(post("/api/manager/test-drives/{id}/assign", requestId)
                            .header("X-User-Id", otherManagerId))
                    .andExpect(status().isOk());
        }

        String requestId6 = createTestDriveRequest();
        mockMvc.perform(post("/api/manager/test-drives/{id}/assign", requestId6)
                        .header("X-User-Id", otherManagerId))
                .andExpect(status().isBadRequest());
    }
}