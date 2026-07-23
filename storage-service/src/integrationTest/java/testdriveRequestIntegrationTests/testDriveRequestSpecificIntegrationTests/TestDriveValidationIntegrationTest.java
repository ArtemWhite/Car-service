package testdriveRequestIntegrationTests.testDriveRequestSpecificIntegrationTests;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import testdriveRequestIntegrationTests.testDriveRequestMainIntegrationTests.TestDriveBaseIntegrationTest;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TestDriveValidationIntegrationTest extends TestDriveBaseIntegrationTest {

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
    void shouldFailCreate_WithoutCarId() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("startTime", getFutureTime().toString());

        mockMvc.perform(post("/api/client/test-drives")
                        .header("X-User-Id", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailCreate_WithoutStartTime() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("carId", testCarId);

        mockMvc.perform(post("/api/client/test-drives")
                        .header("X-User-Id", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailCreate_WithNullClientId() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("carId", testCarId);
        request.put("startTime", getFutureTime().toString());

        mockMvc.perform(post("/api/client/test-drives")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailCreate_WithTooLongNotes() throws Exception {
        String longNotes = "a".repeat(501);
        Map<String, Object> request = new HashMap<>();
        request.put("carId", testCarId);
        request.put("startTime", getFutureTime().toString());
        request.put("notes", longNotes);

        mockMvc.perform(post("/api/client/test-drives")
                        .header("X-User-Id", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailConfirm_WithNullTime() throws Exception {
        String requestId = createTestDriveRequest();

        mockMvc.perform(post("/api/manager/test-drives/{id}/confirm", requestId)
                        .header("X-User-Id", managerId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailCancel_WithoutReason() throws Exception {
        String requestId = createTestDriveRequest();

        mockMvc.perform(post("/api/client/test-drives/{id}/cancel", requestId)
                        .header("X-User-Id", clientId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailCancel_WithEmptyReason() throws Exception {
        String requestId = createTestDriveRequest();

        mockMvc.perform(post("/api/client/test-drives/{id}/cancel", requestId)
                        .header("X-User-Id", clientId)
                        .param("reason", ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailUpdate_WithInvalidStatus() throws Exception {
        String requestId = createTestDriveRequest();

        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("status", "INVALID_STATUS");

        mockMvc.perform(put("/api/admin/test-drives/{id}", requestId)
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailReschedule_WithNullTime() throws Exception {
        String requestId = createTestDriveRequest();

        mockMvc.perform(post("/api/client/test-drives/{id}/reschedule", requestId)
                        .header("X-User-Id", clientId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailCreate_WithNullCarId() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("carId", null);
        request.put("startTime", getFutureTime().toString());

        mockMvc.perform(post("/api/client/test-drives")
                        .header("X-User-Id", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailCreate_WithEmptyCarId() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("carId", "");
        request.put("startTime", getFutureTime().toString());

        mockMvc.perform(post("/api/client/test-drives")
                        .header("X-User-Id", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailConfirm_WithPastTime() throws Exception {
        String requestId = createTestDriveRequest();

        mockMvc.perform(post("/api/manager/test-drives/{id}/assign", requestId)
                        .header("X-User-Id", managerId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/manager/test-drives/{id}/confirm", requestId)
                        .header("X-User-Id", managerId)
                        .param("time", getPastTime().toString()))
                .andExpect(status().isBadRequest());
    }
}