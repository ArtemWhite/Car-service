package testdriveRequestIntegrationTests.testDriveRequestMainIntegrationTests;

import dealerShipOrder.domain.models.testDriveRequest.TestDriveStatus;
import dealerShipOrder.domain.repository.testDriveRequestRepository.testDriveRequestRepository.TestDriveRequestRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TestDriveRepositoryIntegrationTest extends TestDriveBaseIntegrationTest {

    @Autowired
    private TestDriveRequestRepository testDriveRepository;

    private String createTestDriveRequest() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("carId", testCarId);
        request.put("startTime", getFutureTime().toString());

        String response = mockMvc.perform(post("/api/client/test-drives")
                        .header("X-User-Id", clientId)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("id").asText();
    }

    @Test
    void shouldSaveTestDriveRequest() throws Exception {
        String requestId = createTestDriveRequest();

        var found = testDriveRepository.findById(requestId);
        assertThat(found).isPresent();
        assertThat(found.get().getCarId()).isEqualTo(testCarId);
        assertThat(found.get().getClientId()).isEqualTo(clientId);
        assertThat(found.get().getStatus()).isEqualTo(TestDriveStatus.PENDING);
    }

    @Test
    void shouldFindById() throws Exception {
        String requestId = createTestDriveRequest();

        var found = testDriveRepository.findById(requestId);
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(requestId);
    }

    @Test
    void shouldFindByClientId() throws Exception {
        createTestDriveRequest();
        createTestDriveRequest();

        var requests = testDriveRepository.findByClientId(clientId);
        assertThat(requests).hasSize(2);
    }

    @Test
    void shouldFindByCarId() throws Exception {
        createTestDriveRequest();

        var requests = testDriveRepository.findByCarId(testCarId);
        assertThat(requests).isNotEmpty();
    }

    @Test
    void shouldFindByManagerId() throws Exception {
        String requestId = createTestDriveRequest();

        mockMvc.perform(post("/api/manager/test-drives/{id}/assign", requestId)
                        .header("X-User-Id", managerId))
                .andExpect(status().isOk());

        var requests = testDriveRepository.findByManagerId(managerId);
        assertThat(requests).hasSize(1);
        assertThat(requests.get(0).getManagerId()).isEqualTo(managerId);
    }

    @Test
    void shouldFindByStatus() throws Exception {
        createTestDriveRequest();
        createTestDriveRequest();

        var pendingRequests = testDriveRepository.findByStatus(TestDriveStatus.PENDING);
        assertThat(pendingRequests.size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    void shouldFindUpcomingTestDrives() throws Exception {
        createTestDriveRequest();

        var upcoming = testDriveRepository.findUpcomingTestDrives();
        assertThat(upcoming).isNotEmpty();
    }

    @Test
    void shouldFindPastTestDrives() throws Exception {
        String requestId = UUID.randomUUID().toString();
        UUID pendingStatusId = jdbcTemplate.queryForObject(
                "SELECT id FROM test_drive_statuses WHERE name = 'PENDING'", UUID.class);

        LocalDateTime pastTime = LocalDateTime.now().minusHours(2);

        jdbcTemplate.update(
                "INSERT INTO test_drive_requests (id, client_id, car_id, requested_time, status_id, created_at, updated_at, removed) " +
                        "VALUES (?::uuid, ?::uuid, ?::uuid, ?::timestamp, ?::uuid, NOW(), NOW(), false)",
                UUID.fromString(requestId), UUID.fromString(clientId),
                UUID.fromString(testCarId), pastTime, pendingStatusId);

        jdbcTemplate.update(
                "INSERT INTO client_test_drives (client_id, test_drive_id) VALUES (?::uuid, ?::uuid)",
                UUID.fromString(clientId), UUID.fromString(requestId));

        entityManager.flush();
        entityManager.clear();

        var past = testDriveRepository.findPastTestDrives();
        assertThat(past).isNotEmpty();

        assertThat(past.get(0).getId()).isEqualTo(requestId);
    }

    @Test
    void shouldFindByDateRange() throws Exception {
        createTestDriveRequest();

        LocalDateTime from = LocalDateTime.now().minusDays(1);
        LocalDateTime to = LocalDateTime.now().plusDays(2);

        var requests = testDriveRepository.findByDateRange(from, to);
        assertThat(requests).isNotEmpty();
    }

    @Test
    void shouldCheckHasConflict() throws Exception {
        LocalDateTime time = getFutureTime();

        Map<String, Object> request = new HashMap<>();
        request.put("carId", testCarId);
        request.put("startTime", time.toString());

        mockMvc.perform(post("/api/client/test-drives")
                        .header("X-User-Id", clientId)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        boolean hasConflict = testDriveRepository.hasConflict(testCarId, time);
        assertThat(hasConflict).isTrue();
    }

    @Test
    void shouldReturnFalse_WhenNoConflict() throws Exception {
        LocalDateTime time = getFutureTime();
        boolean hasConflict = testDriveRepository.hasConflict(testCarId, time);
        assertThat(hasConflict).isFalse();
    }

    @Test
    void shouldDeleteTestDriveRequest() throws Exception {
        String requestId = createTestDriveRequest();

        testDriveRepository.delete(requestId);

        var found = testDriveRepository.findById(requestId);
        assertThat(found).isEmpty();
    }

    @Test
    void shouldCountByStatus() throws Exception {
        createTestDriveRequest();
        createTestDriveRequest();

        long count = testDriveRepository.countByStatus(TestDriveStatus.PENDING);
        assertThat(count).isGreaterThanOrEqualTo(2);
    }

    @Test
    void shouldFindByStatusIn() throws Exception {
        createTestDriveRequest();
        createTestDriveRequest();

        List<TestDriveStatus> statuses = List.of(TestDriveStatus.PENDING, TestDriveStatus.CONFIRMED);
        var requests = testDriveRepository.findByStatusIn(statuses);
        assertThat(requests.size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    void shouldFindByDateTimeBetween() throws Exception {
        createTestDriveRequest();

        LocalDateTime from = LocalDateTime.now().minusDays(1);
        LocalDateTime to = LocalDateTime.now().plusDays(2);

        var requests = testDriveRepository.findByDateTimeBetween(from, to);
        assertThat(requests).isNotEmpty();
    }
}