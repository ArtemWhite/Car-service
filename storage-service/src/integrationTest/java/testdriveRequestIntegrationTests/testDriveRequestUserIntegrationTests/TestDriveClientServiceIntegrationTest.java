package testdriveRequestIntegrationTests.testDriveRequestUserIntegrationTests;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import testdriveRequestIntegrationTests.testDriveRequestMainIntegrationTests.TestDriveBaseIntegrationTest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TestDriveClientServiceIntegrationTest extends TestDriveBaseIntegrationTest {

    private String createTestDriveRequest() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("carId", testCarId);
        request.put("startTime", getFutureTimeFormatted());

        String response = mockMvc.perform(post("/api/client/test-drives")
                        .header("X-User-Id", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("id").asText();
    }

    @Test
    void shouldCreateTestDriveRequest_Successfully() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("carId", testCarId);
        request.put("startTime", getFutureTimeFormatted());

        mockMvc.perform(post("/api/client/test-drives")
                        .header("X-User-Id", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.carId").value(testCarId))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.clientId").value(clientId))
                .andExpect(jsonPath("$.upcoming").value(true));
    }

    @Test
    void shouldCreateTestDriveRequest_WithNotes() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("carId", testCarId);
        request.put("startTime", getFutureTimeFormatted());
        request.put("notes", "Хочу тест-драйв на трассе");

        mockMvc.perform(post("/api/client/test-drives")
                        .header("X-User-Id", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.notes").value("Хочу тест-драйв на трассе"));
    }

    @Test
    void shouldFailCreateTestDriveRequest_WhenCarNotAvailable() throws Exception {
        jdbcTemplate.update(
                "UPDATE cars SET status_id = (SELECT id FROM car_statuses WHERE name = 'SOLD') WHERE id = ?::uuid",
                UUID.fromString(testCarId));

        entityManager.flush();
        entityManager.clear();

        String newStatus = jdbcTemplate.queryForObject(
                "SELECT cs.name FROM cars c JOIN car_statuses cs ON c.status_id = cs.id WHERE c.id = ?::uuid",
                String.class, UUID.fromString(testCarId));
        System.out.println("Car status after update: " + newStatus);

        var carOpt = carRepository.findById(testCarId);
        if (carOpt.isPresent()) {
            System.out.println("Car available for test drive: " + carOpt.get().isAvailableForTestDrive());
        }

        Map<String, Object> request = new HashMap<>();
        request.put("carId", testCarId);
        request.put("startTime", getFutureTimeFormatted());

        mockMvc.perform(post("/api/client/test-drives")
                        .header("X-User-Id", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailCreateTestDriveRequest_WhenTimeInPast() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("carId", testCarId);
        request.put("startTime", getPastTimeFormatted());

        mockMvc.perform(post("/api/client/test-drives")
                        .header("X-User-Id", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailCreateTestDriveRequest_WhenWithoutAuth() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("carId", testCarId);
        request.put("startTime", getFutureTimeFormatted());

        mockMvc.perform(post("/api/client/test-drives")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetMyTestDrives_Successfully() throws Exception {
        for (int i = 0; i < 2; i++) {
            Map<String, Object> request = new HashMap<>();
            request.put("carId", testCarId);
            request.put("startTime", formatDateTime(getFutureTime().plusHours(i)));
            mockMvc.perform(post("/api/client/test-drives")
                            .header("X-User-Id", clientId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(get("/api/client/test-drives/my")
                        .header("X-User-Id", clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.testDrives.length()").value(2))
                .andExpect(jsonPath("$.totalCount").value(2));
    }

    @Test
    void shouldGetMyTestDrives_EmptyList_WhenNoRequests() throws Exception {
        mockMvc.perform(get("/api/client/test-drives/my")
                        .header("X-User-Id", clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.testDrives.length()").value(0));
    }

    @Test
    void shouldCancelTestDriveRequest_Successfully() throws Exception {
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

        mockMvc.perform(post("/api/client/test-drives/{id}/cancel", requestId)
                        .header("X-User-Id", clientId)
                        .param("reason", "Передумал"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void shouldRescheduleTestDriveRequest_Successfully() throws Exception {
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
        String newTime = formatDateTime(getFutureTime().plusHours(3));

        mockMvc.perform(post("/api/client/test-drives/{id}/reschedule", requestId)
                        .header("X-User-Id", clientId)
                        .param("newTime", newTime))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void shouldFailReschedule_WhenNewTimeInPast() throws Exception {
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
        String pastTime = getPastTimeFormatted();

        mockMvc.perform(post("/api/client/test-drives/{id}/reschedule", requestId)
                        .header("X-User-Id", clientId)
                        .param("newTime", pastTime))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailReschedule_WhenNotOwner() throws Exception {
        String otherClientId = UUID.randomUUID().toString();
        createUser(otherClientId, "CLIENT");
        createClient(otherClientId);

        String otherCarId = createAdditionalTestCar();

        Map<String, Object> request = new HashMap<>();
        request.put("carId", otherCarId);
        request.put("startTime", getFutureTimeFormatted());

        String response = mockMvc.perform(post("/api/client/test-drives")
                        .header("X-User-Id", otherClientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String requestId = objectMapper.readTree(response).get("id").asText();
        String newTime = formatDateTime(getFutureTime().plusHours(2));

        mockMvc.perform(post("/api/client/test-drives/{id}/reschedule", requestId)
                        .header("X-User-Id", clientId)
                        .param("newTime", newTime))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldCreateTestDriveRequest_WithCarInfo() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("carId", testCarId);
        request.put("startTime", getFutureTimeFormatted());

        String response = mockMvc.perform(post("/api/client/test-drives")
                        .header("X-User-Id", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        assertThat(objectMapper.readTree(response).has("carInfo")).isTrue();
    }

    @Test
    void shouldGetMyTestDrives_WithMultipleRequests() throws Exception {
        for (int i = 0; i < 3; i++) {
            Map<String, Object> request = new HashMap<>();
            request.put("carId", testCarId);
            request.put("startTime", formatDateTime(getFutureTime().plusHours(i)));
            mockMvc.perform(post("/api/client/test-drives")
                            .header("X-User-Id", clientId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(get("/api/client/test-drives/my")
                        .header("X-User-Id", clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.testDrives.length()").value(3));
    }

    @Test
    @Transactional
    void shouldFailCancelRequest_WhenAlreadyCompleted() throws Exception {
        String requestId = createTestDriveRequest();

        mockMvc.perform(post("/api/manager/test-drives/{id}/assign", requestId)
                        .header("X-User-Id", managerId))
                .andExpect(status().isOk());

        entityManager.flush();

        jdbcTemplate.update(
                "UPDATE test_drive_requests SET confirmed_time = ?::timestamp WHERE id = ?::uuid",
                java.sql.Timestamp.valueOf(getPastTime()), UUID.fromString(requestId));

        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(post("/api/manager/test-drives/{id}/complete", requestId)
                        .header("X-User-Id", managerId))
                .andExpect(status().isOk());

        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(post("/api/client/test-drives/{id}/cancel", requestId)
                        .header("X-User-Id", clientId)
                        .param("reason", "Поздно"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailCancelRequest_WhenAlreadyCancelled() throws Exception {
        String requestId = createTestDriveRequest();

        mockMvc.perform(post("/api/client/test-drives/{id}/cancel", requestId)
                        .header("X-User-Id", clientId)
                        .param("reason", "Первый раз"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/client/test-drives/{id}/cancel", requestId)
                        .header("X-User-Id", clientId)
                        .param("reason", "Второй раз"))
                .andExpect(status().isBadRequest());
    }
}