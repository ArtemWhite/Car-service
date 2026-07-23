package testdriveRequestIntegrationTests.testDriveRequestSpecificIntegrationTests;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import testdriveRequestIntegrationTests.testDriveRequestMainIntegrationTests.TestDriveBaseIntegrationTest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TestDriveSecurityIntegrationTest extends TestDriveBaseIntegrationTest {

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
    void shouldNotAllowClientToAssignManager() throws Exception {
        String requestId = createTestDriveRequest();

        mockMvc.perform(post("/api/manager/test-drives/{id}/assign", requestId)
                        .header("X-User-Id", clientId))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldNotAllowClientToAccessManagerEndpoint() throws Exception {
        mockMvc.perform(get("/api/manager/test-drives/pending")
                        .header("X-User-Id", clientId))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldNotAllowManagerToDeleteRequest() throws Exception {
        String requestId = createTestDriveRequest();

        mockMvc.perform(delete("/api/admin/test-drives/{id}", requestId)
                        .header("X-User-Id", managerId)
                        .param("reason", "Test delete"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldNotAllowClientToSeeOtherClientsRequests() throws Exception {
        String otherClientId = UUID.randomUUID().toString();
        createUser(otherClientId, "CLIENT");
        createClient(otherClientId);

        Map<String, Object> request = new HashMap<>();
        request.put("carId", testCarId);
        request.put("startTime", getFutureTime().toString());

        mockMvc.perform(post("/api/client/test-drives")
                        .header("X-User-Id", otherClientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/client/test-drives/my")
                        .header("X-User-Id", clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.testDrives.length()").value(0));
    }

    @Test
    void shouldNotAllowManagerToSeeOtherManagersRequests() throws Exception {
        String requestId = createTestDriveRequest();

        mockMvc.perform(post("/api/manager/test-drives/{id}/assign", requestId)
                        .header("X-User-Id", managerId))
                .andExpect(status().isOk());

        String otherManagerId = UUID.randomUUID().toString();
        createUser(otherManagerId, "MANAGER");
        createManager(otherManagerId);

        mockMvc.perform(get("/api/manager/test-drives/my")
                        .header("X-User-Id", otherManagerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.testDrives.length()").value(0));
    }

    @Test
    void shouldNotAllowClientToCompleteTestDrive() throws Exception {
        String requestId = createTestDriveRequest();

        mockMvc.perform(post("/api/manager/test-drives/{id}/complete", requestId)
                        .header("X-User-Id", clientId))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldNotAllowClientToMarkNoShow() throws Exception {
        String requestId = createTestDriveRequest();

        mockMvc.perform(post("/api/manager/test-drives/{id}/no-show", requestId)
                        .header("X-User-Id", clientId))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldNotAllowManagerToUpdateRequestAsAdmin() throws Exception {
        String requestId = createTestDriveRequest();

        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("notes", "Попытка обновить как менеджер");

        mockMvc.perform(put("/api/admin/test-drives/{id}", requestId)
                        .header("X-User-Id", managerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }
}