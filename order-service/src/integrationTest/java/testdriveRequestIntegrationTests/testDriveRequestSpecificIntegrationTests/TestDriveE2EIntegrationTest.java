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

class TestDriveE2EIntegrationTest extends TestDriveBaseIntegrationTest {

    @Test
    void shouldCompleteFullTestDriveFlow() throws Exception {
        Map<String, Object> createRequest = new HashMap<>();
        createRequest.put("carId", testCarId);
        createRequest.put("startTime", getFutureTimeFormatted());

        String response = mockMvc.perform(post("/api/client/test-drives")
                        .header("X-User-Id", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String requestId = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(post("/api/manager/test-drives/{id}/assign", requestId)
                        .header("X-User-Id", managerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        LocalDateTime pastTime = LocalDateTime.now().minusHours(1);
        jdbcTemplate.update(
                "UPDATE test_drive_requests SET confirmed_time = ? WHERE id = ?::uuid",
                pastTime, UUID.fromString(requestId));

        entityManager.flush();
        entityManager.clear();

        LocalDateTime confirmedTime = jdbcTemplate.queryForObject(
                "SELECT confirmed_time FROM test_drive_requests WHERE id = ?::uuid",
                LocalDateTime.class, UUID.fromString(requestId));
        System.out.println("Confirmed time after update: " + confirmedTime);

        mockMvc.perform(post("/api/manager/test-drives/{id}/complete", requestId)
                        .header("X-User-Id", managerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void shouldHandleCancelFlow() throws Exception {
        Map<String, Object> createRequest = new HashMap<>();
        createRequest.put("carId", testCarId);
        createRequest.put("startTime", getFutureTime().toString());

        String response = mockMvc.perform(post("/api/client/test-drives")
                        .header("X-User-Id", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String requestId = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(post("/api/client/test-drives/{id}/cancel", requestId)
                        .header("X-User-Id", clientId)
                        .param("reason", "Передумал"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void shouldHandleRescheduleFlow() throws Exception {
        Map<String, Object> createRequest = new HashMap<>();
        createRequest.put("carId", testCarId);
        createRequest.put("startTime", getFutureTime().toString());

        String response = mockMvc.perform(post("/api/client/test-drives")
                        .header("X-User-Id", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String requestId = objectMapper.readTree(response).get("id").asText();

        LocalDateTime newTime = getFutureTime().plusHours(3);
        mockMvc.perform(post("/api/client/test-drives/{id}/reschedule", requestId)
                        .header("X-User-Id", clientId)
                        .param("newTime", newTime.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));

        mockMvc.perform(post("/api/manager/test-drives/{id}/assign", requestId)
                        .header("X-User-Id", managerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void shouldHandleNoShowFlow() throws Exception {
        Map<String, Object> createRequest = new HashMap<>();
        createRequest.put("carId", testCarId);
        createRequest.put("startTime", getFutureTime().toString());

        String response = mockMvc.perform(post("/api/client/test-drives")
                        .header("X-User-Id", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String requestId = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(post("/api/manager/test-drives/{id}/assign", requestId)
                        .header("X-User-Id", managerId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/manager/test-drives/{id}/no-show", requestId)
                        .header("X-User-Id", managerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NO_SHOW"));
    }

    @Test
    void shouldHandleAdminIntervention() throws Exception {
        Map<String, Object> createRequest = new HashMap<>();
        createRequest.put("carId", testCarId);
        createRequest.put("startTime", getFutureTime().toString());

        String response = mockMvc.perform(post("/api/client/test-drives")
                        .header("X-User-Id", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String requestId = objectMapper.readTree(response).get("id").asText();

        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("notes", "Админ вмешался");
        updateRequest.put("status", "CONFIRMED");

        mockMvc.perform(put("/api/admin/test-drives/{id}", requestId)
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notes").value("Админ вмешался"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void shouldHandleConflictResolution() throws Exception {
        LocalDateTime time = getFutureTime();

        Map<String, Object> request1 = new HashMap<>();
        request1.put("carId", testCarId);
        request1.put("startTime", time.toString());

        mockMvc.perform(post("/api/client/test-drives")
                        .header("X-User-Id", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        String otherClientId = UUID.randomUUID().toString();
        createUser(otherClientId, "CLIENT");
        createClient(otherClientId);

        Map<String, Object> request2 = new HashMap<>();
        request2.put("carId", testCarId);
        request2.put("startTime", time.toString());

        mockMvc.perform(post("/api/client/test-drives")
                        .header("X-User-Id", otherClientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isBadRequest());
    }
}