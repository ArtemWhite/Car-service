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

class UserWarehouseAdminIntegrationTest extends UserBaseIntegrationTest {

    private String testWarehouseAdminId;
    private String testSectionId;
    private String testSparePartId;

    @BeforeEach
    void setUp() throws Exception {
        cleanUpUsers();

        createTestUsers();

        testWarehouseAdminId = UUID.randomUUID().toString();
        createUser(testWarehouseAdminId, "WAREHOUSE_ADMIN", "wh_test@test.com", "ACTIVE");
        createWarehouseAdmin(testWarehouseAdminId, "WAREHOUSE_WORKER");

        testSectionId = "SEC-TEST-001";

        testSparePartId = createTestSparePart();

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void shouldAssignToSection() throws Exception {
        mockMvc.perform(post("/api/warehouse-admin/sections/{sectionId}/assign", testSectionId)
                        .header("X-User-Id", testWarehouseAdminId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.managedSectionIds").value(org.hamcrest.Matchers.hasItem(testSectionId)));
    }

    @Test
    void shouldRemoveFromSection() throws Exception {
        assignWarehouseAdminToSection(testWarehouseAdminId, testSectionId);

        mockMvc.perform(delete("/api/warehouse-admin/sections/{sectionId}/remove", testSectionId)
                        .header("X-User-Id", testWarehouseAdminId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.managedSectionIds").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.hasItem(testSectionId))));
    }

    @Test
    void shouldGetManagedSections() throws Exception {
        assignWarehouseAdminToSection(testWarehouseAdminId, testSectionId);
        assignWarehouseAdminToSection(testWarehouseAdminId, "SEC-TEST-002");

        mockMvc.perform(get("/api/warehouse-admin/sections")
                        .header("X-User-Id", testWarehouseAdminId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sectionIds.length()").value(2))
                .andExpect(jsonPath("$.count").value(2));
    }

    @Test
    void shouldStartShift() throws Exception {
        mockMvc.perform(post("/api/warehouse-admin/shift/start")
                        .header("X-User-Id", testWarehouseAdminId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.onDuty").value(true));
    }

    @Test
    void shouldEndShift() throws Exception {
        jdbcTemplate.update(
                "UPDATE warehouse_admins SET on_duty = true WHERE user_id = ?::uuid",
                UUID.fromString(testWarehouseAdminId)
        );

        mockMvc.perform(post("/api/warehouse-admin/shift/end")
                        .header("X-User-Id", testWarehouseAdminId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.onDuty").value(false));
    }

    @Test
    void shouldNotStartShiftIfAlreadyOnDuty() throws Exception {
        jdbcTemplate.update(
                "UPDATE warehouse_admins SET on_duty = true WHERE user_id = ?::uuid",
                UUID.fromString(testWarehouseAdminId)
        );

        mockMvc.perform(post("/api/warehouse-admin/shift/start")
                        .header("X-User-Id", testWarehouseAdminId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Shift already started"));
    }

    @Test
    void shouldNotEndShiftIfNotStarted() throws Exception {
        mockMvc.perform(post("/api/warehouse-admin/shift/end")
                        .header("X-User-Id", testWarehouseAdminId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("No active shift to end"));
    }

    @Test
    @Disabled("Stock operation persistence not fully implemented in WarehouseAdmin domain model")
    void shouldLogStartShiftOperation() throws Exception {
        int operationCountBefore = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM stock_operations WHERE admin_id = ?::uuid AND type = 'SHIFT_START'",
                Integer.class, UUID.fromString(testWarehouseAdminId)
        );

        mockMvc.perform(post("/api/warehouse-admin/shift/start")
                        .header("X-User-Id", testWarehouseAdminId))
                .andExpect(status().isOk());

        int operationCountAfter = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM stock_operations WHERE admin_id = ?::uuid AND type = 'SHIFT_START'",
                Integer.class, UUID.fromString(testWarehouseAdminId)
        );

        assertThat(operationCountAfter).isGreaterThan(operationCountBefore);
    }

    @Test
    @Disabled("Stock operation persistence not fully implemented in WarehouseAdmin domain model")
    void shouldLogEndShiftOperation() throws Exception {
        jdbcTemplate.update(
                "UPDATE warehouse_admins SET on_duty = true WHERE user_id = ?::uuid",
                UUID.fromString(testWarehouseAdminId)
        );

        int operationCountBefore = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM stock_operations WHERE admin_id = ?::uuid AND type = 'SHIFT_END'",
                Integer.class, UUID.fromString(testWarehouseAdminId)
        );

        mockMvc.perform(post("/api/warehouse-admin/shift/end")
                        .header("X-User-Id", testWarehouseAdminId))
                .andExpect(status().isOk());

        int operationCountAfter = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM stock_operations WHERE admin_id = ?::uuid AND type = 'SHIFT_END'",
                Integer.class, UUID.fromString(testWarehouseAdminId)
        );

        assertThat(operationCountAfter).isGreaterThan(operationCountBefore);
    }

    @Test
    @Disabled("Storage-service endpoints not available in order-service")
    void shouldCreateArrivalOperation() throws Exception {
        mockMvc.perform(post("/api/warehouse-admin/shift/start")
                        .header("X-User-Id", testWarehouseAdminId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/warehouse/spare-parts/{id}/receive", testSparePartId)
                        .header("X-User-Id", testWarehouseAdminId)
                        .param("quantity", "5"))
                .andExpect(status().isOk());

        Integer operationCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM stock_operations WHERE admin_id = ?::uuid AND type = 'ARRIVAL' AND item_id = ?",
                Integer.class, UUID.fromString(testWarehouseAdminId), testSparePartId
        );
        assertThat(operationCount).isGreaterThan(0);
    }

    @Test
    @Disabled("Storage-service endpoints not available in order-service")
    void shouldCreateRemovalOperation() throws Exception {
        mockMvc.perform(post("/api/warehouse-admin/shift/start")
                        .header("X-User-Id", testWarehouseAdminId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/warehouse/spare-parts/{id}/receive", testSparePartId)
                        .header("X-User-Id", testWarehouseAdminId)
                        .param("quantity", "10"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/warehouse/spare-parts/{id}/write-off", testSparePartId)
                        .header("X-User-Id", testWarehouseAdminId)
                        .param("quantity", "3")
                        .param("reason", "Order fulfillment"))
                .andExpect(status().isOk());

        Integer operationCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM stock_operations WHERE admin_id = ?::uuid AND type = 'REMOVAL' AND item_id = ?",
                Integer.class, UUID.fromString(testWarehouseAdminId), testSparePartId
        );
        assertThat(operationCount).isGreaterThan(0);
    }

    @Test
    @Disabled("Storage-service endpoints not available in order-service")
    void shouldCreateMoveOperation() throws Exception {
        mockMvc.perform(post("/api/warehouse-admin/shift/start")
                        .header("X-User-Id", testWarehouseAdminId))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/warehouse/spare-parts/{id}/location", testSparePartId)
                        .header("X-User-Id", testWarehouseAdminId)
                        .param("section", "SEC-TEST-002")
                        .param("location", "B-02"))
                .andExpect(status().isOk());

        Integer operationCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM stock_operations WHERE admin_id = ?::uuid AND type = 'MOVE' AND item_id = ?",
                Integer.class, UUID.fromString(testWarehouseAdminId), testSparePartId
        );
        assertThat(operationCount).isGreaterThan(0);
    }

    @Test
    @Disabled("Storage-service endpoints not available in order-service")
    void shouldCreateWriteOffOperation() throws Exception {
        mockMvc.perform(post("/api/warehouse-admin/shift/start")
                        .header("X-User-Id", testWarehouseAdminId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/warehouse/spare-parts/{id}/receive", testSparePartId)
                        .header("X-User-Id", testWarehouseAdminId)
                        .param("quantity", "10"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/warehouse/spare-parts/{id}/write-off", testSparePartId)
                        .header("X-User-Id", testWarehouseAdminId)
                        .param("quantity", "2")
                        .param("reason", "Defective items"))
                .andExpect(status().isOk());

        Integer operationCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM stock_operations WHERE admin_id = ?::uuid AND type = 'WRITE_OFF' AND item_id = ?",
                Integer.class, UUID.fromString(testWarehouseAdminId), testSparePartId
        );
        assertThat(operationCount).isGreaterThan(0);
    }

    @Test
    @Disabled("Storage-service endpoint /api/warehouse-admin/inventory/start not available")
    void shouldCreateInventoryStartOperation() throws Exception {
        jdbcTemplate.update(
                "UPDATE warehouse_admins SET on_duty = true WHERE user_id = ?::uuid",
                UUID.fromString(testWarehouseAdminId)
        );

        mockMvc.perform(post("/api/warehouse-admin/inventory/start")
                        .header("X-User-Id", testWarehouseAdminId)
                        .param("sectionId", testSectionId))
                .andExpect(status().isOk());

        Integer operationCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM stock_operations WHERE admin_id = ?::uuid AND type = 'INVENTORY_START'",
                Integer.class, UUID.fromString(testWarehouseAdminId)
        );
        assertThat(operationCount).isGreaterThan(0);
    }

    @Test
    @Disabled("StockOperation entity to domain conversion not implemented in WarehouseAdminEntityMapper.toDomain()")
    void shouldGetOperationHistory() throws Exception {
        jdbcTemplate.update(
                "INSERT INTO stock_operations (id, admin_id, type, item_id, item_type, quantity, operation_timestamp, created_at, updated_at, removed) " +
                        "VALUES (?::uuid, ?::uuid, 'ARRIVAL', ?, 'SPARE_PART', 10, NOW(), NOW(), NOW(), false)",
                UUID.randomUUID(), UUID.fromString(testWarehouseAdminId), testSparePartId
        );

        mockMvc.perform(get("/api/warehouse-admin/history")
                        .header("X-User-Id", testWarehouseAdminId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operations.length()").value(org.hamcrest.Matchers.greaterThan(0)));
    }

    @Test
    void shouldFilterOperationHistoryByDate() throws Exception {
        mockMvc.perform(get("/api/warehouse-admin/history/date-range")
                        .header("X-User-Id", testWarehouseAdminId)
                        .param("from", java.time.LocalDateTime.now().minusDays(1).toString())
                        .param("to", java.time.LocalDateTime.now().plusDays(1).toString()))
                .andExpect(status().isOk());
    }

    @Test
    void shouldFilterOperationHistoryByType() throws Exception {
        mockMvc.perform(get("/api/warehouse-admin/history/type/{operationType}", "ARRIVAL")
                        .header("X-User-Id", testWarehouseAdminId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operations[*].operationType").value(org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is("ARRIVAL"))));
    }

    @Test
    void shouldGetShiftStatus() throws Exception {
        mockMvc.perform(get("/api/warehouse-admin/shift/status")
                        .header("X-User-Id", testWarehouseAdminId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.onDuty").exists());
    }

    @Test
    @Disabled("Storage-service endpoint /api/warehouse/spare-parts not available in order-service")
    void shouldNotAllowOperationsWhenOffDuty() throws Exception {
        mockMvc.perform(post("/api/warehouse/spare-parts/{id}/receive", testSparePartId)
                        .header("X-User-Id", testWarehouseAdminId)
                        .param("quantity", "5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Warehouse admin is not on duty"));
    }

    @Test
    @Disabled("Endpoint /api/warehouse-admin/me not available in WarehouseAdminController")
    void shouldGetWarehouseAdminProfile() throws Exception {
        mockMvc.perform(get("/api/warehouse-admin/me")
                        .header("X-User-Id", testWarehouseAdminId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userType").value("WAREHOUSE_ADMIN"))
                .andExpect(jsonPath("$.warehousePosition").value("WAREHOUSE_WORKER"));
    }

    @Test
    @Disabled("Requires SENIOR_WAREHOUSE_ADMIN position which doesn't exist in warehouse_positions")
    void shouldUpdateWarehouseAdminPosition() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("warehousePosition", "SENIOR_WAREHOUSE_ADMIN");

        mockMvc.perform(put("/api/admin/users/{userId}", testWarehouseAdminId)
                        .header("X-User-Id", adminId)  // adminId из createTestUsers()
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.warehousePosition").value("SENIOR_WAREHOUSE_ADMIN"));
    }
}