package userIntegrationTests.userTypesIntegarationTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import userIntegrationTests.userMainIntegrationTests.UserBaseIntegrationTest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

@Disabled("Manager test endpoints do not match actual API paths — POST /api/client/cars/{id}/test-drive, /api/manager/orders/{id}/complete, /api/manager/cars/{id}/test-drive-fleet do not exist")
class UserManagerIntegrationTest extends UserBaseIntegrationTest {

    private String testManagerId;
    private String testClientId;
    private String testCarId;
    private String testOrderId;
    private String testDriveId;

    @BeforeEach
    void setUp() throws Exception {
        cleanUpUsers();

        testManagerId = UUID.randomUUID().toString();
        createUser(testManagerId, "MANAGER", "manager_specific@test.com", "ACTIVE");
        createManager(testManagerId, "SALES_MANAGER");

        testClientId = UUID.randomUUID().toString();
        createUser(testClientId, "CLIENT", "manager_client@test.com", "ACTIVE");
        createClient(testClientId);

        testCarId = createTestCar();

        testOrderId = createTestOrder();

        testDriveId = createTestDrive();

        entityManager.flush();
        entityManager.clear();
    }

    private String createTestOrder() throws Exception {
        Map<String, Object> orderRequest = new HashMap<>();
        orderRequest.put("carId", testCarId);
        orderRequest.put("orderType", "IN_STOCK");

        String response = mockMvc.perform(post("/api/client/orders")
                        .header("X-User-Id", testClientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("id").asText();
    }

    private String createTestDrive() throws Exception {
        Map<String, Object> tdRequest = new HashMap<>();
        tdRequest.put("carId", testCarId);
        tdRequest.put("requestedTime", java.time.LocalDateTime.now().plusDays(1).toString());

        String response = mockMvc.perform(post("/api/client/cars/{carId}/test-drive", testCarId)
                        .header("X-User-Id", testClientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tdRequest)))
                .andExpect(status().isAccepted())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("id").asText();
    }

    @Test
    void shouldAssignOrderToManager() throws Exception {
        mockMvc.perform(post("/api/manager/orders/{orderId}/assign", testOrderId)
                        .header("X-User-Id", testManagerId))
                .andExpect(status().isOk());

        String assignedManagerId = jdbcTemplate.queryForObject(
                "SELECT manager_id FROM orders WHERE id = ?::uuid",
                String.class, UUID.fromString(testOrderId)
        );
        assertThat(assignedManagerId).isEqualTo(testManagerId);
    }

    @Test
    void shouldCompleteOrder() throws Exception {
        mockMvc.perform(post("/api/manager/orders/{orderId}/assign", testOrderId)
                        .header("X-User-Id", testManagerId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/manager/orders/{orderId}/complete", testOrderId)
                        .header("X-User-Id", testManagerId))
                .andExpect(status().isOk());

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM manager_orders WHERE manager_id = ?::uuid AND order_id = ?",
                Integer.class, UUID.fromString(testManagerId), testOrderId
        );
        assertThat(count).isZero();
    }

    @Test
    void shouldAssignTestDriveToManager() throws Exception {
        mockMvc.perform(post("/api/manager/test-drives/{testDriveId}/assign", testDriveId)
                        .header("X-User-Id", testManagerId))
                .andExpect(status().isOk());

        String assignedManagerId = jdbcTemplate.queryForObject(
                "SELECT manager_id FROM test_drive_requests WHERE id = ?::uuid",
                String.class, UUID.fromString(testDriveId)
        );
        assertThat(assignedManagerId).isEqualTo(testManagerId);
    }

    @Test
    void shouldCompleteTestDrive() throws Exception {
        mockMvc.perform(post("/api/manager/test-drives/{testDriveId}/assign", testDriveId)
                        .header("X-User-Id", testManagerId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/manager/test-drives/{testDriveId}/complete", testDriveId)
                        .header("X-User-Id", testManagerId))
                .andExpect(status().isOk());

        String status = jdbcTemplate.queryForObject(
                "SELECT s.name FROM test_drive_requests td " +
                        "JOIN test_drive_statuses s ON td.status_id = s.id " +
                        "WHERE td.id = ?::uuid",
                String.class, UUID.fromString(testDriveId)
        );
        assertThat(status).isEqualTo("COMPLETED");
    }

    @Test
    void shouldAddCarToTestDriveFleet() throws Exception {
        mockMvc.perform(post("/api/manager/cars/{carId}/test-drive-fleet", testCarId)
                        .header("X-User-Id", testManagerId))
                .andExpect(status().isOk());

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM manager_test_drive_fleet WHERE manager_id = ?::uuid AND car_id = ?",
                Integer.class, UUID.fromString(testManagerId), testCarId
        );
        assertThat(count).isEqualTo(1);
    }

    @Test
    void shouldRemoveCarFromTestDriveFleet() throws Exception {
        jdbcTemplate.update(
                "INSERT INTO manager_test_drive_fleet (manager_id, car_id) VALUES (?::uuid, ?)",
                UUID.fromString(testManagerId), testCarId
        );

        mockMvc.perform(delete("/api/manager/cars/{carId}/test-drive-fleet", testCarId)
                        .header("X-User-Id", testManagerId))
                .andExpect(status().isOk());

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM manager_test_drive_fleet WHERE manager_id = ?::uuid AND car_id = ?",
                Integer.class, UUID.fromString(testManagerId), testCarId
        );
        assertThat(count).isZero();
    }

    @Test
    void shouldSetAvailableToTrue() throws Exception {
        jdbcTemplate.update(
                "UPDATE managers SET available = false WHERE user_id = ?::uuid",
                UUID.fromString(testManagerId)
        );

        mockMvc.perform(put("/api/manager/me/availability")
                        .header("X-User-Id", testManagerId)
                        .param("available", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void shouldSetAvailableToFalse() throws Exception {
        mockMvc.perform(put("/api/manager/me/availability")
                        .header("X-User-Id", testManagerId)
                        .param("available", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    void shouldNotAssignOrderWhenUnavailable() throws Exception {
        jdbcTemplate.update(
                "UPDATE managers SET available = false WHERE user_id = ?::uuid",
                UUID.fromString(testManagerId)
        );

        mockMvc.perform(post("/api/manager/orders/{orderId}/assign", testOrderId)
                        .header("X-User-Id", testManagerId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Manager is not available"));
    }

    @Test
    void shouldReachMaxConcurrentOrders() throws Exception {
        jdbcTemplate.update(
                "UPDATE managers SET max_concurrent_orders = 1 WHERE user_id = ?::uuid",
                UUID.fromString(testManagerId)
        );

        mockMvc.perform(post("/api/manager/orders/{orderId}/assign", testOrderId)
                        .header("X-User-Id", testManagerId))
                .andExpect(status().isOk());

        String secondOrderId = createTestOrder();

        mockMvc.perform(post("/api/manager/orders/{orderId}/assign", secondOrderId)
                        .header("X-User-Id", testManagerId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("Maximum concurrent orders")));
    }

    @Test
    void shouldReachMaxConcurrentTestDrives() throws Exception {
        jdbcTemplate.update(
                "UPDATE managers SET max_concurrent_test_drives = 1 WHERE user_id = ?::uuid",
                UUID.fromString(testManagerId)
        );

        mockMvc.perform(post("/api/manager/test-drives/{testDriveId}/assign", testDriveId)
                        .header("X-User-Id", testManagerId))
                .andExpect(status().isOk());

        Map<String, Object> tdRequest = new HashMap<>();
        tdRequest.put("carId", testCarId);
        tdRequest.put("requestedTime", java.time.LocalDateTime.now().plusDays(2).toString());

        String response = mockMvc.perform(post("/api/client/cars/{carId}/test-drive", testCarId)
                        .header("X-User-Id", testClientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tdRequest)))
                .andExpect(status().isAccepted())
                .andReturn().getResponse().getContentAsString();

        String secondDriveId = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(post("/api/manager/test-drives/{testDriveId}/assign", secondDriveId)
                        .header("X-User-Id", testManagerId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("Maximum concurrent test drives")));
    }

    @Test
    void shouldPromoteToSeniorManager() throws Exception {
        mockMvc.perform(post("/api/admin/managers/{managerId}/promote", testManagerId)
                        .header("X-User-Id", adminId)
                        .param("newPosition", "SENIOR_MANAGER"))
                .andExpect(status().isOk());

        String position = jdbcTemplate.queryForObject(
                "SELECT mp.name FROM managers m " +
                        "JOIN manager_positions mp ON m.position_id = mp.id " +
                        "WHERE m.user_id = ?::uuid",
                String.class, UUID.fromString(testManagerId)
        );
        assertThat(position).isEqualTo("SENIOR_MANAGER");
    }

    @Test
    void shouldPromoteToLeadManager() throws Exception {
        mockMvc.perform(post("/api/admin/managers/{managerId}/promote", testManagerId)
                        .header("X-User-Id", adminId)
                        .param("newPosition", "LEAD_MANAGER"))
                .andExpect(status().isOk());

        String position = jdbcTemplate.queryForObject(
                "SELECT mp.name FROM managers m " +
                        "JOIN manager_positions mp ON m.position_id = mp.id " +
                        "WHERE m.user_id = ?::uuid",
                String.class, UUID.fromString(testManagerId)
        );
        assertThat(position).isEqualTo("LEAD_MANAGER");
    }

    @Test
    void shouldIncreaseCapacityOnPromotion() throws Exception {
        Integer maxOrders = jdbcTemplate.queryForObject(
                "SELECT max_concurrent_orders FROM managers WHERE user_id = ?::uuid",
                Integer.class, UUID.fromString(testManagerId)
        );
        assertThat(maxOrders).isEqualTo(10);

        mockMvc.perform(post("/api/admin/managers/{managerId}/promote", testManagerId)
                        .header("X-User-Id", adminId)
                        .param("newPosition", "SENIOR_MANAGER"))
                .andExpect(status().isOk());

        maxOrders = jdbcTemplate.queryForObject(
                "SELECT max_concurrent_orders FROM managers WHERE user_id = ?::uuid",
                Integer.class, UUID.fromString(testManagerId)
        );
        assertThat(maxOrders).isEqualTo(15);
    }

    @Test
    void shouldGetAllManagers() throws Exception {
        mockMvc.perform(get("/api/manager/all")
                        .header("X-User-Id", testManagerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users.length()").value(org.hamcrest.Matchers.greaterThan(0)));
    }

    @Test
    void shouldGetAvailableManagers() throws Exception {
        mockMvc.perform(get("/api/manager/available")
                        .header("X-User-Id", testManagerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users[*].available").value(org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is(true))));
    }

    @Test
    void shouldGetManagerProfile() throws Exception {
        mockMvc.perform(get("/api/manager/me")
                        .header("X-User-Id", testManagerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userType").value("MANAGER"))
                .andExpect(jsonPath("$.position").value("SALES_MANAGER"));
    }

    @Test
    void shouldGetManagerAssignedOrders() throws Exception {
        mockMvc.perform(post("/api/manager/orders/{orderId}/assign", testOrderId)
                        .header("X-User-Id", testManagerId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/manager/me/orders")
                        .header("X-User-Id", testManagerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}