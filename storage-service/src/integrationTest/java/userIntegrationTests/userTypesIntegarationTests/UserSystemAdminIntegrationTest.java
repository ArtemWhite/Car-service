package userIntegrationTests.userTypesIntegarationTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import userIntegrationTests.userMainIntegrationTests.UserBaseIntegrationTest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

class UserSystemAdminIntegrationTest extends UserBaseIntegrationTest {

    private String targetAdminId;
    private String testManagerId;

    @BeforeEach
    void setUp() {
        cleanUpUsers();
        createTestUsers();

        targetAdminId = UUID.randomUUID().toString();
        createUser(targetAdminId, "SYSTEM_ADMIN", "target@test.com", "ACTIVE");
        createSystemAdmin(targetAdminId, "JUNIOR_ADMIN");

        testManagerId = UUID.randomUUID().toString();
        createUser(testManagerId, "MANAGER", "promotemanager@test.com", "ACTIVE");
        createManager(testManagerId, "SALES_MANAGER");

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void shouldGrantUserCreationPermission() throws Exception {
        mockMvc.perform(post("/api/admin/admins/{targetAdminId}/permissions/{permission}", targetAdminId, "CREATE_USER")
                        .header("X-User-Id", adminId))
                .andExpect(status().isOk());

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM admin_permissions ap " +
                        "JOIN system_permissions sp ON ap.permission_id = sp.id " +
                        "WHERE ap.admin_id = ?::uuid AND sp.name = 'CREATE_USER'",
                Integer.class, UUID.fromString(targetAdminId)
        );
        assertThat(count).isEqualTo(1);
    }

    @Test
    void shouldGrantUserDeletionPermission() throws Exception {
        mockMvc.perform(post("/api/admin/admins/{targetAdminId}/permissions/{permission}", targetAdminId, "DELETE_USER")
                        .header("X-User-Id", adminId))
                .andExpect(status().isOk());

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM admin_permissions ap " +
                        "JOIN system_permissions sp ON ap.permission_id = sp.id " +
                        "WHERE ap.admin_id = ?::uuid AND sp.name = 'DELETE_USER'",
                Integer.class, UUID.fromString(targetAdminId)
        );
        assertThat(count).isEqualTo(1);
    }

    @Test
    void shouldRevokePermission() throws Exception {
        // First grant
        addPermissionToAdmin(targetAdminId, "CREATE_USER");

        // Then revoke
        mockMvc.perform(delete("/api/admin/admins/{targetAdminId}/permissions/{permission}", targetAdminId, "CREATE_USER")
                        .header("X-User-Id", adminId))
                .andExpect(status().isOk());

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM admin_permissions ap " +
                        "JOIN system_permissions sp ON ap.permission_id = sp.id " +
                        "WHERE ap.admin_id = ?::uuid AND sp.name = 'CREATE_USER'",
                Integer.class, UUID.fromString(targetAdminId)
        );
        assertThat(count).isZero();
    }

    @Test
    void shouldViewAuditLog() throws Exception {
        // Create some audit entries
        createAuditLogEntry(adminId, "TEST_ACTION_1", "Test audit 1");
        createAuditLogEntry(adminId, "TEST_ACTION_2", "Test audit 2");

        mockMvc.perform(get("/api/admin/audit-log")
                        .header("X-User-Id", adminId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operations.length()").value(org.hamcrest.Matchers.greaterThan(0)));
    }

    @Test
    void shouldViewUserAuditLog() throws Exception {
        createAuditLogEntry(adminId, "USER_ACTION", "Action on user");

        mockMvc.perform(get("/api/admin/audit-log/users/{userId}", clientId)
                        .header("X-User-Id", adminId))
                .andExpect(status().isOk());
    }

    @Test
    void shouldFilterAuditByAction() throws Exception {
        createAuditLogEntry(adminId, "SPECIFIC_ACTION", "Specific action");

        mockMvc.perform(get("/api/admin/audit-log")
                        .header("X-User-Id", adminId)
                        .param("action", "SPECIFIC_ACTION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operations[*].operationType").value(org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is("SPECIFIC_ACTION"))));
    }

    @Test
    void shouldFilterAuditByDate() throws Exception {
        mockMvc.perform(get("/api/admin/audit-log")
                        .header("X-User-Id", adminId)
                        .param("from", java.time.LocalDateTime.now().minusDays(1).toString())
                        .param("to", java.time.LocalDateTime.now().plusDays(1).toString()))
                .andExpect(status().isOk());
    }

    @Test
    void shouldChangeSystemSettings() throws Exception {
        Map<String, Object> settings = new HashMap<>();
        settings.put("maxLoginAttempts", 5);
        settings.put("sessionTimeout", 30);
        settings.put("passwordExpiryDays", 90);

        mockMvc.perform(put("/api/admin/system/settings")
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(settings)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldNotChangeSettingsWithoutPermission() throws Exception {
        Map<String, Object> settings = new HashMap<>();
        settings.put("maxLoginAttempts", 5);

        mockMvc.perform(put("/api/admin/system/settings")
                        .header("X-User-Id", targetAdminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(settings)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldPromoteManagerToSystemAdmin() throws Exception {
        mockMvc.perform(post("/api/admin/managers/{managerId}/promote-to-admin", testManagerId)
                        .header("X-User-Id", adminId)
                        .param("adminLevel", "JUNIOR_ADMIN"))
                .andExpect(status().isOk());

        String userType = getUserType(testManagerId);
        assertThat(userType).isEqualTo("SYSTEM_ADMIN");
    }

    @Test
    void shouldPromoteWarehouseAdmin() throws Exception {
        mockMvc.perform(post("/api/admin/warehouse-admins/{targetAdminId}/promote", warehouseAdminId)
                        .header("X-User-Id", adminId)
                        .param("newPosition", "SENIOR_WAREHOUSE_ADMIN"))
                .andExpect(status().isOk());

        String position = jdbcTemplate.queryForObject(
                "SELECT wp.name FROM warehouse_admins wa " +
                        "JOIN warehouse_positions wp ON wa.warehouse_position_id = wp.id " +
                        "WHERE wa.user_id = ?::uuid",
                String.class, UUID.fromString(warehouseAdminId)
        );
        assertThat(position).isEqualTo("SENIOR_WAREHOUSE_ADMIN");
    }

    @Test
    void shouldDemoteSystemAdmin() throws Exception {
        mockMvc.perform(post("/api/admin/admins/{targetAdminId}/demote", targetAdminId)
                        .header("X-User-Id", adminId)
                        .param("newLevel", "JUNIOR_ADMIN"))
                .andExpect(status().isOk());

        String level = jdbcTemplate.queryForObject(
                "SELECT al.name FROM system_admins sa " +
                        "JOIN admin_levels al ON sa.admin_level_id = al.id " +
                        "WHERE sa.user_id = ?::uuid",
                String.class, UUID.fromString(targetAdminId)
        );
        assertThat(level).isEqualTo("JUNIOR_ADMIN");
    }

    @Test
    void shouldNotDeleteOwnAccount() throws Exception {
        mockMvc.perform(delete("/api/admin/users/{userId}", adminId)
                        .header("X-User-Id", adminId)
                        .param("reason", "Trying to delete self"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Cannot delete your own account"));
    }

    @Test
    void shouldNotBlockSelf() throws Exception {
        mockMvc.perform(post("/api/admin/users/{userId}/block", adminId)
                        .header("X-User-Id", adminId)
                        .param("reason", "Trying to block self"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Cannot block your own account"));
    }

    @Test
    void shouldCreateAdminWithLevel() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("firstName", "New");
        request.put("lastName", "Admin");
        request.put("email", "newleveladmin@test.com");
        request.put("phone", "+71234567890");
        request.put("password", "SecurePass123");
        request.put("userType", "SYSTEM_ADMIN");
        request.put("employeeId", "ADM002");
        request.put("adminLevel", "ADMIN");

        mockMvc.perform(post("/api/admin/users")
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.adminLevel").value("ADMIN"));
    }

    @Test
    void shouldNotCreateAdminWithHigherLevel() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("firstName", "New");
        request.put("lastName", "Admin");
        request.put("email", "higheradmin@test.com");
        request.put("phone", "+71234567890");
        request.put("password", "SecurePass123");
        request.put("userType", "SYSTEM_ADMIN");
        request.put("employeeId", "ADM003");
        request.put("adminLevel", "SUPER_ADMIN");

        mockMvc.perform(post("/api/admin/users")
                        .header("X-User-Id", targetAdminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldGetAdminPermissions() throws Exception {
        mockMvc.perform(get("/api/admin/admins/{adminId}/permissions", adminId)
                        .header("X-User-Id", adminId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permissions").isArray());
    }

    @Test
    void shouldListAllSystemAdmins() throws Exception {
        mockMvc.perform(get("/api/admin/admins")
                        .header("X-User-Id", adminId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users.length()").value(org.hamcrest.Matchers.greaterThan(0)))
                .andExpect(jsonPath("$.users[*].userType").value(org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is("SYSTEM_ADMIN"))));
    }

    @Test
    void shouldListAllWarehouseAdmins() throws Exception {
        mockMvc.perform(get("/api/admin/warehouse-admins")
                        .header("X-User-Id", adminId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users.length()").value(org.hamcrest.Matchers.greaterThan(0)))
                .andExpect(jsonPath("$.users[*].userType").value(org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is("WAREHOUSE_ADMIN"))));
    }

    @Test
    void shouldGetSystemStats() throws Exception {
        mockMvc.perform(get("/api/admin/stats")
                        .header("X-User-Id", adminId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").exists())
                .andExpect(jsonPath("$.activeUsers").exists())
                .andExpect(jsonPath("$.clientsCount").exists())
                .andExpect(jsonPath("$.managersCount").exists())
                .andExpect(jsonPath("$.adminsCount").exists());
    }

    @Test
    void shouldGetUserRegistrationStats() throws Exception {
        mockMvc.perform(get("/api/admin/stats/registrations")
                        .header("X-User-Id", adminId)
                        .param("days", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dailyStats").isArray());
    }

    @Test
    void shouldPerformBulkStatusUpdate() throws Exception {
        String userId1 = UUID.randomUUID().toString();
        String userId2 = UUID.randomUUID().toString();
        createUser(userId1, "CLIENT", "bulkstatus1@test.com", "ACTIVE");
        createClient(userId1);
        createUser(userId2, "CLIENT", "bulkstatus2@test.com", "ACTIVE");
        createClient(userId2);

        mockMvc.perform(put("/api/admin/users/bulk/status")
                        .header("X-User-Id", adminId)
                        .param("status", "INACTIVE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new String[]{userId1, userId2})))
                .andExpect(status().isOk());

        assertThat(getUserStatus(userId1)).isEqualTo("INACTIVE");
        assertThat(getUserStatus(userId2)).isEqualTo("INACTIVE");
    }
}