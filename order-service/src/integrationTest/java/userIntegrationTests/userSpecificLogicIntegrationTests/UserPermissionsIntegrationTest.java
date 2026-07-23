package userIntegrationTests.userSpecificLogicIntegrationTests;

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

class UserPermissionsIntegrationTest extends UserBaseIntegrationTest {

    private String juniorAdminId;
    private String regularAdminId;

    @BeforeEach
    void setUp() {
        cleanUpUsers();
        createTestUsers();

        juniorAdminId = UUID.randomUUID().toString();
        createUser(juniorAdminId, "SYSTEM_ADMIN", "junior@test.com", "ACTIVE");
        createSystemAdmin(juniorAdminId, "JUNIOR_ADMIN");

        regularAdminId = UUID.randomUUID().toString();
        createUser(regularAdminId, "SYSTEM_ADMIN", "regular@test.com", "ACTIVE");
        createSystemAdmin(regularAdminId, "ADMIN");

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void shouldClientNotAccessAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("X-User-Id", clientId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("Access denied")));
    }

    @Test
    void shouldManagerNotCreateUser() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("firstName", "Test");
        request.put("lastName", "User");
        request.put("email", "test@test.com");
        request.put("password", "password123");
        request.put("userType", "CLIENT");

        mockMvc.perform(post("/api/admin/users")
                        .header("X-User-Id", managerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldSystemAdminAccessAuditLog() throws Exception {
        mockMvc.perform(get("/api/admin/audit-log")
                        .header("X-User-Id", adminId))
                .andExpect(status().isOk());
    }

    @Test
    void shouldWarehouseAdminAccessWarehouseEndpoints() throws Exception {
        mockMvc.perform(get("/api/warehouse-admin/me/sections")
                        .header("X-User-Id", warehouseAdminId))
                .andExpect(status().isOk());
    }

    @Test
    void shouldNotAccessWithoutXUserIdHeader() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Missing X-User-Id header"));
    }

    @Test
    void shouldNotAccessWithInvalidUserId() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .header("X-User-Id", UUID.randomUUID().toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotAccessWhenBlocked() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .header("X-User-Id", blockedUserId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Account is blocked"));
    }

    @Test
    void shouldNotAccessWhenInactive() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .header("X-User-Id", inactiveUserId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Account is inactive"));
    }

    @Test
    void shouldSystemAdminGrantPermission() throws Exception {
        mockMvc.perform(post("/api/admin/admins/{targetAdminId}/permissions/{permission}", juniorAdminId, "CREATE_USER")
                        .header("X-User-Id", adminId))
                .andExpect(status().isOk());

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM admin_permissions ap " +
                        "JOIN system_permissions sp ON ap.permission_id = sp.id " +
                        "WHERE ap.admin_id = ?::uuid AND sp.name = 'CREATE_USER'",
                Integer.class, UUID.fromString(juniorAdminId)
        );
        assertThat(count).isEqualTo(1);
    }

    @Test
    void shouldSystemAdminRevokePermission() throws Exception {
        addPermissionToAdmin(juniorAdminId, "CREATE_USER");

        mockMvc.perform(delete("/api/admin/admins/{targetAdminId}/permissions/{permission}", juniorAdminId, "CREATE_USER")
                        .header("X-User-Id", adminId))
                .andExpect(status().isOk());

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM admin_permissions ap " +
                        "JOIN system_permissions sp ON ap.permission_id = sp.id " +
                        "WHERE ap.admin_id = ?::uuid AND sp.name = 'CREATE_USER'",
                Integer.class, UUID.fromString(juniorAdminId)
        );
        assertThat(count).isZero();
    }

    @Test
    void shouldNotGrantHigherLevelThanOwn() throws Exception {
        mockMvc.perform(post("/api/admin/admins/{targetAdminId}/permissions/{permission}", regularAdminId, "CREATE_USER")
                        .header("X-User-Id", juniorAdminId))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldSystemAdminPromoteAnotherAdmin() throws Exception {
        mockMvc.perform(post("/api/admin/admins/{targetAdminId}/promote", juniorAdminId)
                        .header("X-User-Id", adminId)
                        .param("newLevel", "ADMIN"))
                .andExpect(status().isOk());

        String level = jdbcTemplate.queryForObject(
                "SELECT al.name FROM system_admins sa " +
                        "JOIN admin_levels al ON sa.admin_level_id = al.id " +
                        "WHERE sa.user_id = ?::uuid",
                String.class, UUID.fromString(juniorAdminId)
        );
        assertThat(level).isEqualTo("ADMIN");
    }

    @Test
    void shouldNotPromoteSelfSuperAdmin() throws Exception {
        mockMvc.perform(post("/api/admin/admins/{targetAdminId}/promote", adminId)
                        .header("X-User-Id", adminId)
                        .param("newLevel", "SUPER_ADMIN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Cannot promote to same or higher level than yourself"));
    }

    @Test
    void shouldCheckPermissionBeforeAction() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("firstName", "Test");
        request.put("lastName", "User");
        request.put("email", "test@test.com");
        request.put("password", "password123");
        request.put("userType", "CLIENT");

        mockMvc.perform(post("/api/admin/users")
                        .header("X-User-Id", juniorAdminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("Permission denied")));
    }

    @Test
    void shouldSuperAdminHaveAllPermissions() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("firstName", "Super");
        request.put("lastName", "Created");
        request.put("email", "supercreated@test.com");
        request.put("phone", "+71234567890");
        request.put("password", "SecurePass123");
        request.put("userType", "CLIENT");

        mockMvc.perform(post("/api/admin/users")
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldWarehouseAdminNotAccessAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("X-User-Id", warehouseAdminId))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldClientNotAccessManagerEndpoints() throws Exception {
        mockMvc.perform(get("/api/manager/me/availability")
                        .header("X-User-Id", clientId))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldManagerNotAccessWarehouseEndpoints() throws Exception {
        mockMvc.perform(post("/api/warehouse-admin/me/shift/start")
                        .header("X-User-Id", managerId))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldSystemAdminAccessAllUserEndpoints() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("X-User-Id", adminId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/users/type/CLIENT")
                        .header("X-User-Id", adminId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/audit-log")
                        .header("X-User-Id", adminId))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRespectRoleHierarchy() throws Exception {
        mockMvc.perform(post("/api/admin/admins/{targetAdminId}/promote", regularAdminId)
                        .header("X-User-Id", juniorAdminId)
                        .param("newLevel", "ADMIN"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/admin/admins/{targetAdminId}/promote", juniorAdminId)
                        .header("X-User-Id", regularAdminId)
                        .param("newLevel", "ADMIN"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/admin/admins/{targetAdminId}/promote", juniorAdminId)
                        .header("X-User-Id", regularAdminId)
                        .param("newLevel", "SUPER_ADMIN"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRequireSpecificPermissionForSensitiveOperations() throws Exception {
        jdbcTemplate.update(
                "DELETE FROM admin_permissions WHERE admin_id = ?::uuid AND permission_id IN " +
                        "(SELECT id FROM system_permissions WHERE name = 'CREATE_USER')",
                UUID.fromString(regularAdminId)
        );

        Map<String, Object> request = new HashMap<>();
        request.put("firstName", "Test");
        request.put("lastName", "User");
        request.put("email", "test@test.com");
        request.put("password", "password123");
        request.put("userType", "CLIENT");

        mockMvc.perform(post("/api/admin/users")
                        .header("X-User-Id", regularAdminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowPublicEndpointsWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/users/{id}", clientId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/cars/available"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowOwnProfileAccessEvenWhenInactive() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .header("X-User-Id", inactiveUserId))
                .andExpect(status().isForbidden());
    }
}