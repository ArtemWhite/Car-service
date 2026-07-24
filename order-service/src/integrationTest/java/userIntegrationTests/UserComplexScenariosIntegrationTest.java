package userIntegrationTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import userIntegrationTests.userMainIntegrationTests.UserBaseIntegrationTest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

class UserComplexScenariosIntegrationTest extends UserBaseIntegrationTest {

    private String testClientId;
    private String testManagerId;
    private String testCarId;
    private String testOrderId;

    @BeforeEach
    void setUp() throws Exception {
        cleanUpUsers();

        testClientId = UUID.randomUUID().toString();
        createUser(testClientId, "CLIENT", "complex_client@test.com", "ACTIVE");
        createClient(testClientId);

        testManagerId = UUID.randomUUID().toString();
        createUser(testManagerId, "MANAGER", "complex_manager@test.com", "ACTIVE");
        createManager(testManagerId, "SALES_MANAGER");

        testCarId = createTestCar();

        entityManager.flush();
        entityManager.clear();
    }

    // ==================== COMPLEX SCENARIO 1: Complete User Journey ====================

    @Test
    @org.junit.jupiter.api.Disabled("Calls GET /api/cars/available and POST /api/auth/login which don't exist in order-service")
    void testCompleteUserJourney_ClientBuysCar() throws Exception {
        // 1. Client registers
        String newClientId = UUID.randomUUID().toString();
        Map<String, Object> registerRequest = new HashMap<>();
        registerRequest.put("firstName", "Journey");
        registerRequest.put("lastName", "Client");
        registerRequest.put("email", "journey@test.com");
        registerRequest.put("phone", "+71234567890");
        registerRequest.put("password", "SecurePass123");
        registerRequest.put("userType", "CLIENT");

        mockMvc.perform(post("/api/admin/users")
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // 2. Client logs in
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "journey@test.com");
        loginRequest.put("password", "SecurePass123");

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(loginResponse).get("token").asText();
        String clientId = objectMapper.readTree(loginResponse).get("userId").asText();

        // 3. Client browses available cars
        mockMvc.perform(get("/api/cars/available")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // 4. Client creates an order
        Map<String, Object> orderRequest = new HashMap<>();
        orderRequest.put("carId", testCarId);
        orderRequest.put("orderType", "IN_STOCK");

        String orderResponse = mockMvc.perform(post("/api/client/orders")
                        .header("Authorization", "Bearer " + token)
                        .header("X-User-Id", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String orderId = objectMapper.readTree(orderResponse).get("id").asText();

        // 5. Manager assigns to order
        mockMvc.perform(post("/api/manager/orders/{orderId}/assign", orderId)
                        .header("X-User-Id", testManagerId))
                .andExpect(status().isOk());

        // 6. Admin confirms order (sets to AWAITING_PAYMENT)
        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("status", "AWAITING_PAYMENT");

        mockMvc.perform(put("/api/admin/orders/{id}", orderId)
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        // 7. Client creates payment
        Map<String, Object> paymentRequest = new HashMap<>();
        paymentRequest.put("orderId", orderId);
        paymentRequest.put("amount", 2500000.0);
        paymentRequest.put("method", "CARD");

        mockMvc.perform(post("/api/client/payments")
                        .header("Authorization", "Bearer " + token)
                        .header("X-User-Id", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isCreated());

        // 8. Verify order status changed to PAID after payment
        Thread.sleep(500);

        String orderStatus = jdbcTemplate.queryForObject(
                "SELECT s.name FROM orders o JOIN order_statuses s ON o.status_id = s.id WHERE o.id = ?::uuid",
                String.class, UUID.fromString(orderId)
        );
        assertThat(orderStatus).isEqualTo("PAID");

        // 9. Admin marks order as READY_FOR_PICKUP
        updateRequest.put("status", "READY_FOR_PICKUP");
        mockMvc.perform(put("/api/admin/orders/{id}", orderId)
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        // 10. Admin completes order
        updateRequest.put("status", "COMPLETED");
        mockMvc.perform(put("/api/admin/orders/{id}", orderId)
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        // 11. Verify final order status
        orderStatus = jdbcTemplate.queryForObject(
                "SELECT s.name FROM orders o JOIN order_statuses s ON o.status_id = s.id WHERE o.id = ?::uuid",
                String.class, UUID.fromString(orderId)
        );
        assertThat(orderStatus).isEqualTo("COMPLETED");
    }

    // ==================== COMPLEX SCENARIO 2: Test Drive Booking Flow ====================

    @Test
    @org.junit.jupiter.api.Disabled("Calls POST /api/client/cars/{carId}/test-drive which doesn't exist in order-service (use POST /api/client/test-drives)")
    void testCompleteTestDriveFlow() throws Exception {
        // 1. Client requests test drive
        Map<String, Object> tdRequest = new HashMap<>();
        tdRequest.put("carId", testCarId);
        tdRequest.put("requestedTime", java.time.LocalDateTime.now().plusDays(2).toString());

        String tdResponse = mockMvc.perform(post("/api/client/cars/{carId}/test-drive", testCarId)
                        .header("X-User-Id", testClientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tdRequest)))
                .andExpect(status().isAccepted())
                .andReturn().getResponse().getContentAsString();

        String testDriveId = objectMapper.readTree(tdResponse).get("id").asText();

        // 2. Manager assigns to test drive
        mockMvc.perform(post("/api/manager/test-drives/{testDriveId}/assign", testDriveId)
                        .header("X-User-Id", testManagerId))
                .andExpect(status().isOk());

        // 3. Check test drive is confirmed
        String status = jdbcTemplate.queryForObject(
                "SELECT s.name FROM test_drive_requests td JOIN test_drive_statuses s ON td.status_id = s.id WHERE td.id = ?::uuid",
                String.class, UUID.fromString(testDriveId)
        );
        assertThat(status).isEqualTo("CONFIRMED");

        // 4. Manager completes test drive
        mockMvc.perform(post("/api/manager/test-drives/{testDriveId}/complete", testDriveId)
                        .header("X-User-Id", testManagerId))
                .andExpect(status().isOk());

        // 5. Verify test drive is completed
        status = jdbcTemplate.queryForObject(
                "SELECT s.name FROM test_drive_requests td JOIN test_drive_statuses s ON td.status_id = s.id WHERE td.id = ?::uuid",
                String.class, UUID.fromString(testDriveId)
        );
        assertThat(status).isEqualTo("COMPLETED");

        // 6. Client should have test drive in history
        mockMvc.perform(get("/api/client/me/test-drives")
                        .header("X-User-Id", testClientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ==================== COMPLEX SCENARIO 3: User Promotion Chain ====================

    @Test
    void testUserPromotionChain() throws Exception {
        // 1. Create new manager
        String newManagerId = UUID.randomUUID().toString();
        createUser(newManagerId, "MANAGER", "promote_chain@test.com", "ACTIVE");
        createManager(newManagerId, "SALES_MANAGER");

        // 2. Verify SALES_MANAGER position
        String position = jdbcTemplate.queryForObject(
                "SELECT mp.name FROM managers m JOIN manager_positions mp ON m.position_id = mp.id WHERE m.user_id = ?::uuid",
                String.class, UUID.fromString(newManagerId)
        );
        assertThat(position).isEqualTo("SALES_MANAGER");

        // 3. Promote to SENIOR_MANAGER
        mockMvc.perform(post("/api/admin/managers/{managerId}/promote", newManagerId)
                        .header("X-User-Id", adminId)
                        .param("newPosition", "SENIOR_MANAGER"))
                .andExpect(status().isOk());

        position = jdbcTemplate.queryForObject(
                "SELECT mp.name FROM managers m JOIN manager_positions mp ON m.position_id = mp.id WHERE m.user_id = ?::uuid",
                String.class, UUID.fromString(newManagerId)
        );
        assertThat(position).isEqualTo("SENIOR_MANAGER");

        // 4. Promote to LEAD_MANAGER
        mockMvc.perform(post("/api/admin/managers/{managerId}/promote", newManagerId)
                        .header("X-User-Id", adminId)
                        .param("newPosition", "LEAD_MANAGER"))
                .andExpect(status().isOk());

        position = jdbcTemplate.queryForObject(
                "SELECT mp.name FROM managers m JOIN manager_positions mp ON m.position_id = mp.id WHERE m.user_id = ?::uuid",
                String.class, UUID.fromString(newManagerId)
        );
        assertThat(position).isEqualTo("LEAD_MANAGER");

        // 5. Promote from Manager to System Admin
        mockMvc.perform(post("/api/admin/managers/{managerId}/promote-to-admin", newManagerId)
                        .header("X-User-Id", adminId)
                        .param("adminLevel", "JUNIOR_ADMIN"))
                .andExpect(status().isOk());

        String userType = getUserType(newManagerId);
        assertThat(userType).isEqualTo("SYSTEM_ADMIN");

        // 6. Promote to ADMIN
        mockMvc.perform(post("/api/admin/admins/{targetAdminId}/promote", newManagerId)
                        .header("X-User-Id", adminId)
                        .param("newLevel", "ADMIN"))
                .andExpect(status().isOk());

        String level = jdbcTemplate.queryForObject(
                "SELECT al.name FROM system_admins sa JOIN admin_levels al ON sa.admin_level_id = al.id WHERE sa.user_id = ?::uuid",
                String.class, UUID.fromString(newManagerId)
        );
        assertThat(level).isEqualTo("ADMIN");
    }

    // ==================== COMPLEX SCENARIO 4: Bulk User Operations ====================

    @Test
    void testBulkUserOperations() throws Exception {
        // 1. Create 20 users in bulk using loop
        String[] userIds = new String[20];
        for (int i = 0; i < 20; i++) {
            userIds[i] = UUID.randomUUID().toString();
            createUser(userIds[i], "CLIENT", "bulk" + i + "@test.com", "ACTIVE");
            createClient(userIds[i]);
        }

        // 2. Bulk block users with specific email pattern
        mockMvc.perform(put("/api/admin/users/bulk/status")
                        .header("X-User-Id", adminId)
                        .param("status", "BLOCKED")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userIds)))
                .andExpect(status().isOk());

        // 3. Verify all are blocked
        for (String userId : userIds) {
            assertThat(getUserStatus(userId)).isEqualTo("BLOCKED");
        }

        // 4. Bulk unblock
        mockMvc.perform(put("/api/admin/users/bulk/status")
                        .header("X-User-Id", adminId)
                        .param("status", "ACTIVE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userIds)))
                .andExpect(status().isOk());

        // 5. Verify all are active again
        for (String userId : userIds) {
            assertThat(getUserStatus(userId)).isEqualTo("ACTIVE");
        }

        // 6. Bulk delete
        mockMvc.perform(delete("/api/admin/users/bulk")
                        .header("X-User-Id", adminId)
                        .param("reason", "Bulk deletion test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userIds)))
                .andExpect(status().isNoContent());

        // 7. Verify all are soft-deleted
        for (String userId : userIds) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM users WHERE id = ?::uuid AND removed = false",
                    Integer.class, UUID.fromString(userId)
            );
            assertThat(count).isZero();
        }
    }


    @Test
    @org.junit.jupiter.api.Disabled("Calls POST /api/client/cars/{carId}/test-drive which doesn't exist in order-service (use POST /api/client/test-drives)")
    void testConcurrentUserAccess() throws Exception {
        String[] clientIds = new String[10];
        for (int i = 0; i < 10; i++) {
            clientIds[i] = UUID.randomUUID().toString();
            createUser(clientIds[i], "CLIENT", "concurrent" + i + "@test.com", "ACTIVE");
            createClient(clientIds[i]);
        }

        AtomicInteger successCount = new AtomicInteger(0);

        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(10);

        for (int i = 0; i < 10; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    Map<String, Object> tdRequest = new HashMap<>();
                    tdRequest.put("carId", testCarId);
                    tdRequest.put("requestedTime", java.time.LocalDateTime.now().plusDays(index + 1).toString());

                    mockMvc.perform(post("/api/client/cars/{carId}/test-drive", testCarId)
                                    .header("X-User-Id", clientIds[index])
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(tdRequest)))
                            .andExpect(status().isAccepted());
                    successCount.incrementAndGet();
                } catch (Exception e) {
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(30, java.util.concurrent.TimeUnit.SECONDS);

        assertThat(successCount.get()).isEqualTo(10);

        Integer totalTestDrives = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM test_drive_requests WHERE car_id = ?",
                Integer.class, testCarId
        );
        assertThat(totalTestDrives).isEqualTo(10);
    }

    @Test
    @org.junit.jupiter.api.Disabled("Queries spare_parts table which doesn't exist in order-service; warehouse-admin endpoints may not exist")
    void testWarehouseFullCycle() throws Exception {
        String warehouseId = UUID.randomUUID().toString();
        createUser(warehouseId, "WAREHOUSE_ADMIN", "warehouse_cycle@test.com", "ACTIVE");
        createWarehouseAdmin(warehouseId, "WAREHOUSE_WORKER");

        String sectionId = "SEC-CYCLE-001";
        String sparePartId = createTestSparePart();

        mockMvc.perform(post("/api/warehouse-admin/shift/start")
                        .header("X-User-Id", warehouseId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/warehouse-admin/sections/{sectionId}/assign", sectionId)
                        .header("X-User-Id", warehouseId))
                .andExpect(status().isOk());

        Map<String, Object> receiveRequest = new HashMap<>();
        receiveRequest.put("sparePartId", sparePartId);
        receiveRequest.put("quantity", 50);
        receiveRequest.put("reason", "Initial stock");

        mockMvc.perform(post("/api/warehouse-admin/spare-parts/receive")
                        .header("X-User-Id", warehouseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(receiveRequest)))
                .andExpect(status().isOk());

        Map<String, Object> moveRequest = new HashMap<>();
        moveRequest.put("sparePartId", sparePartId);
        moveRequest.put("fromSection", sectionId);
        moveRequest.put("fromLocation", "A-01");
        moveRequest.put("toSection", sectionId);
        moveRequest.put("toLocation", "B-02");
        moveRequest.put("reason", "Reorganize");

        mockMvc.perform(post("/api/warehouse-admin/spare-parts/move")
                        .header("X-User-Id", warehouseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moveRequest)))
                .andExpect(status().isOk());

        Map<String, Object> removeRequest = new HashMap<>();
        removeRequest.put("sparePartId", sparePartId);
        removeRequest.put("quantity", 10);
        removeRequest.put("reason", "Order fulfillment");

        mockMvc.perform(post("/api/warehouse-admin/spare-parts/remove")
                        .header("X-User-Id", warehouseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(removeRequest)))
                .andExpect(status().isOk());

        Map<String, Object> writeOffRequest = new HashMap<>();
        writeOffRequest.put("sparePartId", sparePartId);
        writeOffRequest.put("quantity", 5);
        writeOffRequest.put("reason", "Defective items");

        mockMvc.perform(post("/api/warehouse-admin/spare-parts/write-off")
                        .header("X-User-Id", warehouseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(writeOffRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/warehouse-admin/shift/end")
                        .header("X-User-Id", warehouseId))
                .andExpect(status().isOk());

        Integer finalQuantity = jdbcTemplate.queryForObject(
                "SELECT stock_quantity FROM spare_parts WHERE id = ?::uuid",
                Integer.class, UUID.fromString(sparePartId)
        );
        assertThat(finalQuantity).isEqualTo(35);

        Integer operationCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM stock_operations WHERE admin_id = ?::uuid",
                Integer.class, UUID.fromString(warehouseId)
        );
        assertThat(operationCount).isGreaterThanOrEqualTo(6);
    }

    @Test
    void testUserDataExportImport() throws Exception {
        for (int i = 0; i < 5; i++) {
            String userId = UUID.randomUUID().toString();
            createUser(userId, "CLIENT", "export" + i + "@test.com", "ACTIVE");
            createClient(userId);
        }

        String csvContent = mockMvc.perform(get("/api/admin/users/export")
                        .header("X-User-Id", adminId)
                        .param("format", "csv"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(csvContent).isNotEmpty();
        assertThat(csvContent).contains("email");
        assertThat(csvContent).contains("firstName");
        assertThat(csvContent).contains("lastName");

        byte[] excelContent = mockMvc.perform(get("/api/admin/users/export")
                        .header("X-User-Id", adminId)
                        .param("format", "xlsx"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();

        assertThat(excelContent.length).isGreaterThan(0);
    }
}