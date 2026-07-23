package testdriveRequestIntegrationTests.testDriveRequestSpecificIntegrationTests;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import testdriveRequestIntegrationTests.testDriveRequestMainIntegrationTests.TestDriveBaseIntegrationTest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TestDrivePublicServiceIntegrationTest extends TestDriveBaseIntegrationTest {

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
    void shouldGetTestDriveById_Successfully() throws Exception {
        String requestId = createTestDriveRequest();

        mockMvc.perform(get("/api/test-drives/{id}", requestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(requestId))
                .andExpect(jsonPath("$.clientId").value(clientId))
                .andExpect(jsonPath("$.carId").value(testCarId));
    }

    @Test
    void shouldReturn404_WhenTestDriveNotFound() throws Exception {
        mockMvc.perform(get("/api/test-drives/{id}", UUID.randomUUID().toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetAllTestDrives_Successfully() throws Exception {
        createTestDriveRequest();
        createTestDriveRequest();

        mockMvc.perform(get("/api/test-drives"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.testDrives.length()").value(2));
    }

    @Test
    void shouldFilterByStatus() throws Exception {
        createTestDriveRequest();
        createTestDriveRequest();

        mockMvc.perform(get("/api/test-drives")
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.testDrives.length()").value(2));
    }

    @Test
    void shouldFilterByDateRange() throws Exception {
        createTestDriveRequest();
        String from = LocalDateTime.now().minusDays(1).toString();
        String to = LocalDateTime.now().plusDays(2).toString();

        mockMvc.perform(get("/api/test-drives")
                        .param("dateFrom", from)
                        .param("dateTo", to))
                .andExpect(status().isOk());
    }

    @Test
    void shouldFilterByUpcoming() throws Exception {
        createTestDriveRequest();

        mockMvc.perform(get("/api/test-drives")
                        .param("upcoming", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.testDrives[0].upcoming").value(true));
    }

    @Test
    void shouldPaginateResults() throws Exception {
        for (int i = 0; i < 25; i++) {
            createTestDriveRequest();
        }

        mockMvc.perform(get("/api/test-drives")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.testDrives.length()").value(10));
    }

    @Test
    void shouldSortByRequestedTime() throws Exception {
        createTestDriveRequest();

        mockMvc.perform(get("/api/test-drives")
                        .param("sortBy", "startTime")
                        .param("sortDirection", "DESC"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldCombineMultipleFilters() throws Exception {
        createTestDriveRequest();

        mockMvc.perform(get("/api/test-drives")
                        .param("status", "PENDING")
                        .param("carId", testCarId)
                        .param("upcoming", "true"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnEmptyList_WhenNoMatches() throws Exception {
        mockMvc.perform(get("/api/test-drives")
                        .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.testDrives.length()").value(0));
    }

    @Test
    void shouldFilterByClientId() throws Exception {
        String otherClientId = UUID.randomUUID().toString();
        createUser(otherClientId, "CLIENT");
        createClient(otherClientId);

        Map<String, Object> request1 = new HashMap<>();
        request1.put("carId", testCarId);
        request1.put("startTime", getFutureTime().toString());
        mockMvc.perform(post("/api/client/test-drives")
                        .header("X-User-Id", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        Map<String, Object> request2 = new HashMap<>();
        request2.put("carId", testCarId);
        request2.put("startTime", getFutureTime().plusHours(1).toString());
        mockMvc.perform(post("/api/client/test-drives")
                        .header("X-User-Id", otherClientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/test-drives")
                        .param("clientId", clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.testDrives.length()").value(1));
    }

//    @Test
//    void shouldFilterByCarId() throws Exception {
//        String otherCarId = createTestCar();
//
//        createTestDriveRequest();
//
//        Map<String, Object> request = new HashMap<>();
//        request.put("carId", otherCarId);
//        request.put("startTime", getFutureTime().toString());
//        mockMvc.perform(post("/api/client/test-drives")
//                        .header("X-User-Id", clientId)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isCreated());
//
//        mockMvc.perform(get("/api/test-drives")
//                        .param("carId", testCarId))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.testDrives.length()").value(1));
//    }

    @Test
    void shouldFilterByManagerId() throws Exception {
        String otherManagerId = UUID.randomUUID().toString();
        createUser(otherManagerId, "MANAGER");
        createManager(otherManagerId);

        String requestId1 = createTestDriveRequest();
        String requestId2 = createTestDriveRequest();

        mockMvc.perform(post("/api/manager/test-drives/{id}/assign", requestId1)
                        .header("X-User-Id", managerId))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/manager/test-drives/{id}/assign", requestId2)
                        .header("X-User-Id", otherManagerId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/test-drives")
                        .param("managerId", managerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.testDrives.length()").value(1));
    }

    @Test
    void shouldSortByStatus() throws Exception {
        createTestDriveRequest();
        String completedRequestId = createTestDriveRequest();

        mockMvc.perform(post("/api/manager/test-drives/{id}/assign", completedRequestId)
                        .header("X-User-Id", managerId))
                .andExpect(status().isOk());

        LocalDateTime pastTime = LocalDateTime.now().minusHours(1);
        jdbcTemplate.update(
                "UPDATE test_drive_requests SET confirmed_time = ? WHERE id = ?::uuid",
                pastTime, UUID.fromString(completedRequestId));

        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(post("/api/manager/test-drives/{id}/complete", completedRequestId)
                        .header("X-User-Id", managerId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/test-drives")
                        .param("sortBy", "status")
                        .param("sortDirection", "ASC"))
                .andExpect(status().isOk());
    }
}