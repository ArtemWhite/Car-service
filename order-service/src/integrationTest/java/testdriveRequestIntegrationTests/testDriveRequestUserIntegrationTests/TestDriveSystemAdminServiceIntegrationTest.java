package testdriveRequestIntegrationTests.testDriveRequestUserIntegrationTests;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import testdriveRequestIntegrationTests.testDriveRequestMainIntegrationTests.TestDriveBaseIntegrationTest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TestDriveSystemAdminServiceIntegrationTest extends TestDriveBaseIntegrationTest {

    private String createTestDriveRequest() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("carId", testCarId);
        request.put("startTime", getFutureTime().toString());

        String response = mockMvc.perform(post("/api/client/test-drives")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("id").asText();
    }

    @Test
    void shouldUpdateRequest_Successfully() throws Exception {
        String requestId = createTestDriveRequest();

        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("notes", "Обновлено админом");
        updateRequest.put("status", "CONFIRMED");

        mockMvc.perform(put("/api/admin/test-drives/{id}", requestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notes").value("Обновлено админом"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void shouldUpdateRequestStatus_ToCancelled() throws Exception {
        String requestId = createTestDriveRequest();

        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("status", "CANCELLED");
        updateRequest.put("cancelReason", "Отменено админом");

        mockMvc.perform(put("/api/admin/test-drives/{id}", requestId)
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void shouldFailUpdateRequest_WhenNotAdmin() throws Exception {
        String requestId = createTestDriveRequest();

        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("notes", "Попытка обновить не админом");

        mockMvc.perform(put("/api/admin/test-drives/{id}", requestId)
                        .header("X-User-Id", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldFailUpdateRequest_WhenNotFound() throws Exception {
        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("notes", "Несуществующий запрос");

        mockMvc.perform(put("/api/admin/test-drives/{id}", UUID.randomUUID().toString())
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteRequest_Successfully() throws Exception {
        String requestId = createTestDriveRequest();

        mockMvc.perform(delete("/api/admin/test-drives/{id}", requestId)
                        .header("X-User-Id", adminId)
                        .param("reason", "Тестовое удаление"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/test-drives/{id}", requestId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFailDeleteRequest_WhenNotAdmin() throws Exception {
        String requestId = createTestDriveRequest();

        mockMvc.perform(delete("/api/admin/test-drives/{id}", requestId)
                        .header("X-User-Id", clientId)
                        .param("reason", "Попытка удалить"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldGetRequestsByStatus_Successfully() throws Exception {
        createTestDriveRequest();
        createTestDriveRequest();

        mockMvc.perform(get("/api/admin/test-drives/status/PENDING")
                        .header("X-User-Id", adminId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.testDrives.length()").value(2));
    }

    @Test
    void shouldFailGetRequestsByStatus_InvalidStatus() throws Exception {
        mockMvc.perform(get("/api/admin/test-drives/status/INVALID_STATUS")
                        .header("X-User-Id", adminId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdateRequest_WithNotes() throws Exception {
        String requestId = createTestDriveRequest();

        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("notes", "Новые заметки от админа");

        mockMvc.perform(put("/api/admin/test-drives/{id}", requestId)
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notes").value("Новые заметки от админа"));
    }

    @Test
    void shouldFailDeleteRequest_WhenNotFound() throws Exception {
        mockMvc.perform(delete("/api/admin/test-drives/{id}", UUID.randomUUID().toString())
                        .header("X-User-Id", adminId)
                        .param("reason", "Удаление"))
                .andExpect(status().isNotFound());
    }
}