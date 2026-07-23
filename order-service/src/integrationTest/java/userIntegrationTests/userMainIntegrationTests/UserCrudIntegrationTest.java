package userIntegrationTests.userMainIntegrationTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

class UserCrudIntegrationTest extends UserBaseIntegrationTest {

    @BeforeEach
    void setUp() {
        cleanUpUsers();
        createTestUsers();
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void shouldCreateClientUser() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("firstName", "New");
        request.put("lastName", "Client");
        request.put("middleName", "User");
        request.put("email", "newclient@test.com");
        request.put("phone", "+71234567890");
        request.put("password", "SecurePass123");
        request.put("userType", "CLIENT");

        mockMvc.perform(post("/api/admin/users")
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.firstName").value("New"))
                .andExpect(jsonPath("$.lastName").value("Client"))
                .andExpect(jsonPath("$.email").value("newclient@test.com"))
                .andExpect(jsonPath("$.userType").value("CLIENT"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldCreateManagerUser() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("firstName", "New");
        request.put("lastName", "Manager");
        request.put("middleName", "User");
        request.put("email", "newmanager@test.com");
        request.put("phone", "+71234567890");
        request.put("password", "SecurePass123");
        request.put("userType", "MANAGER");
        request.put("employeeId", "EMP001");
        request.put("position", "SALES_MANAGER");

        mockMvc.perform(post("/api/admin/users")
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userType").value("MANAGER"))
                .andExpect(jsonPath("$.position").value("SALES_MANAGER"));
    }

    @Test
    void shouldCreateSystemAdminUser() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("firstName", "New");
        request.put("lastName", "Admin");
        request.put("middleName", "User");
        request.put("email", "newadmin@test.com");
        request.put("phone", "+71234567890");
        request.put("password", "SecurePass123");
        request.put("userType", "SYSTEM_ADMIN");
        request.put("employeeId", "ADM001");
        request.put("adminLevel", "ADMIN");

        mockMvc.perform(post("/api/admin/users")
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userType").value("SYSTEM_ADMIN"))
                .andExpect(jsonPath("$.adminLevel").value("ADMIN"));
    }

    @Test
    void shouldCreateWarehouseAdminUser() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("firstName", "New");
        request.put("lastName", "Warehouse");
        request.put("middleName", "User");
        request.put("email", "newwarehouse@test.com");
        request.put("phone", "+71234567890");
        request.put("password", "SecurePass123");
        request.put("userType", "WAREHOUSE_ADMIN");
        request.put("employeeId", "WH001");
        request.put("managedSectionIds", Set.of("SEC-01", "SEC-02"));

        mockMvc.perform(post("/api/admin/users")
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userType").value("WAREHOUSE_ADMIN"));
    }

    @Test
    void shouldNotCreateUserWithDuplicateEmail() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("firstName", "Duplicate");
        request.put("lastName", "User");
        request.put("email", "client@test.com");
        request.put("phone", "+71234567890");
        request.put("password", "SecurePass123");
        request.put("userType", "CLIENT");

        mockMvc.perform(post("/api/admin/users")
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("User with this email already exists"));
    }

    @Test
    void shouldNotCreateUserWithInvalidEmail() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("firstName", "Invalid");
        request.put("lastName", "Email");
        request.put("email", "not-an-email");
        request.put("phone", "+71234567890");
        request.put("password", "SecurePass123");
        request.put("userType", "CLIENT");

        mockMvc.perform(post("/api/admin/users")
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotCreateUserWithWeakPassword() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("firstName", "Weak");
        request.put("lastName", "Password");
        request.put("email", "weak@test.com");
        request.put("phone", "+71234567890");
        request.put("password", "123");
        request.put("userType", "CLIENT");

        mockMvc.perform(post("/api/admin/users")
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password").exists());
    }

    @Test
    void shouldUpdateUserFirstName() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("firstName", "UpdatedFirstName");

        mockMvc.perform(put("/api/admin/users/{userId}", clientId)
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("UpdatedFirstName"));
    }

    @Test
    void shouldUpdateUserEmail() throws Exception {
        String newEmail = "updatedemail@test.com";
        Map<String, Object> request = new HashMap<>();
        request.put("email", newEmail);

        mockMvc.perform(put("/api/admin/users/{userId}", clientId)
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(newEmail));
    }

    @Test
    void shouldUpdateUserPhone() throws Exception {
        String newPhone = "+79998887766";
        Map<String, Object> request = new HashMap<>();
        request.put("phone", newPhone);

        mockMvc.perform(put("/api/admin/users/{userId}", clientId)
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phone").value(newPhone));
    }

    @Test
    void shouldUpdateUserStatus() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("status", "INACTIVE");

        mockMvc.perform(put("/api/admin/users/{userId}", clientId)
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    void shouldUpdateManagerPosition() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("position", "SENIOR_MANAGER");

        mockMvc.perform(put("/api/admin/users/{userId}", managerId)
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.position").value("SENIOR_MANAGER"));
    }

    @Test
    void shouldUpdateWarehouseAdminPosition() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("warehousePosition", "SENIOR_WAREHOUSE_ADMIN");

        mockMvc.perform(put("/api/admin/users/{userId}", warehouseAdminId)
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.warehousePosition").value("SENIOR_WAREHOUSE_ADMIN"));
    }

    @Test
    void shouldDeleteUserByAdmin() throws Exception {
        String userIdToDelete = UUID.randomUUID().toString();
        createUser(userIdToDelete, "CLIENT", "delete@test.com", "ACTIVE");
        createClient(userIdToDelete);

        mockMvc.perform(delete("/api/admin/users/{userId}", userIdToDelete)
                        .header("X-User-Id", adminId)
                        .param("reason", "Test deletion"))
                .andExpect(status().isNoContent());

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE id = ?::uuid AND removed = false",
                Integer.class, UUID.fromString(userIdToDelete)
        );
        assertThat(count).isZero();
    }

    @Test
    void shouldNotDeleteUserWithoutPermission() throws Exception {
        mockMvc.perform(delete("/api/admin/users/{userId}", clientId)
                        .header("X-User-Id", clientId)
                        .param("reason", "Trying to delete"))
                .andExpect(status().isForbidden());
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
    void shouldBlockUserByAdmin() throws Exception {
        mockMvc.perform(post("/api/admin/users/{userId}/block", clientId)
                        .header("X-User-Id", adminId)
                        .param("reason", "Violation detected"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOCKED"));
    }

    @Test
    void shouldUnblockUserByAdmin() throws Exception {
        jdbcTemplate.update(
                "UPDATE users SET status_id = (SELECT id FROM user_statuses WHERE name = 'BLOCKED') WHERE id = ?::uuid",
                UUID.fromString(clientId)
        );

        mockMvc.perform(post("/api/admin/users/{userId}/unblock", clientId)
                        .header("X-User-Id", adminId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldLogAuditOnUserDelete() throws Exception {
        String userIdToDelete = UUID.randomUUID().toString();
        createUser(userIdToDelete, "CLIENT", "auditdelete@test.com", "ACTIVE");
        createClient(userIdToDelete);

        int auditCountBefore = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM audit_log_entries WHERE action = 'DELETE_USER'",
                Integer.class
        );

        mockMvc.perform(delete("/api/admin/users/{userId}", userIdToDelete)
                        .header("X-User-Id", adminId)
                        .param("reason", "Audit test"))
                .andExpect(status().isNoContent());

        int auditCountAfter = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM audit_log_entries WHERE action = 'DELETE_USER'",
                Integer.class
        );

        assertThat(auditCountAfter).isGreaterThan(auditCountBefore);
    }

    @Test
    void shouldLogAuditOnUserBlock() throws Exception {
        int auditCountBefore = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM audit_log_entries WHERE action = 'BLOCK_USER'",
                Integer.class
        );

        mockMvc.perform(post("/api/admin/users/{userId}/block", clientId)
                        .header("X-User-Id", adminId)
                        .param("reason", "Audit block test"))
                .andExpect(status().isOk());

        int auditCountAfter = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM audit_log_entries WHERE action = 'BLOCK_USER'",
                Integer.class
        );

        assertThat(auditCountAfter).isGreaterThan(auditCountBefore);
    }

    @Test
    void shouldReturnUserDetailsById() throws Exception {
        mockMvc.perform(get("/api/admin/users/{userId}/details", clientId)
                        .header("X-User-Id", adminId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clientId))
                .andExpect(jsonPath("$.email").value("client@test.com"))
                .andExpect(jsonPath("$.userType").value("CLIENT"));
    }

    @Test
    void shouldReturnUserListWithFilters() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("X-User-Id", adminId)
                        .param("userType", "CLIENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users").isArray())
                .andExpect(jsonPath("$.totalCount").value(org.hamcrest.Matchers.greaterThan(0)));
    }

    @Test
    void shouldGetUsersByType() throws Exception {
        mockMvc.perform(get("/api/admin/users/type/{userType}", "CLIENT")
                        .header("X-User-Id", adminId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users.length()").value(org.hamcrest.Matchers.greaterThan(0)))
                .andExpect(jsonPath("$.users[0].userType").value("CLIENT"));
    }

    @Test
    void shouldGetAllUsers() throws Exception {
        mockMvc.perform(get("/api/admin/users/all")
                        .header("X-User-Id", adminId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users").isArray())
                .andExpect(jsonPath("$.totalCount").value(org.hamcrest.Matchers.greaterThan(0)));
    }

    @Test
    void shouldPaginateUserList() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("X-User-Id", adminId)
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users.length()").value(org.hamcrest.Matchers.lessThanOrEqualTo(2)));
    }

    @Test
    void shouldSortUserList() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("X-User-Id", adminId)
                        .param("sortBy", "firstName")
                        .param("sortDirection", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users").isArray());
    }

    @Test
    void shouldReturn404WhenUserNotFound() throws Exception {
        mockMvc.perform(get("/api/users/{id}", UUID.randomUUID().toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldBulkCreateUsers() throws Exception {
        for (int i = 0; i < 10; i++) {
            Map<String, Object> request = new HashMap<>();
            request.put("firstName", "Bulk" + i);
            request.put("lastName", "User" + i);
            request.put("email", "bulk" + i + "@test.com");
            request.put("phone", "+71234567890");
            request.put("password", "SecurePass123");
            request.put("userType", "CLIENT");

            mockMvc.perform(post("/api/admin/users")
                            .header("X-User-Id", adminId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE email LIKE 'bulk%@test.com' AND removed = false",
                Integer.class
        );
        assertThat(count).isEqualTo(10);
    }

    @Test
    void shouldBulkUpdateUserStatus() throws Exception {
        String userId1 = UUID.randomUUID().toString();
        String userId2 = UUID.randomUUID().toString();
        createUser(userId1, "CLIENT", "bulk1@test.com", "ACTIVE");
        createClient(userId1);
        createUser(userId2, "CLIENT", "bulk2@test.com", "ACTIVE");
        createClient(userId2);

        mockMvc.perform(post("/api/admin/users/bulk/status")
                        .header("X-User-Id", adminId)
                        .param("status", "INACTIVE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Set.of(userId1, userId2))))
                .andExpect(status().isOk());

        assertThat(getUserStatus(userId1)).isEqualTo("INACTIVE");
        assertThat(getUserStatus(userId2)).isEqualTo("INACTIVE");
    }

    @Test
    void shouldBulkDeleteUsers() throws Exception {
        String userId1 = UUID.randomUUID().toString();
        String userId2 = UUID.randomUUID().toString();
        createUser(userId1, "CLIENT", "bulkdel1@test.com", "ACTIVE");
        createClient(userId1);
        createUser(userId2, "CLIENT", "bulkdel2@test.com", "ACTIVE");
        createClient(userId2);

        mockMvc.perform(delete("/api/admin/users/bulk")
                        .header("X-User-Id", adminId)
                        .param("reason", "Bulk deletion test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Set.of(userId1, userId2))))
                .andExpect(status().isNoContent());

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE id IN (?::uuid, ?::uuid) AND removed = false",
                Integer.class, UUID.fromString(userId1), UUID.fromString(userId2)
        );
        assertThat(count).isZero();
    }
}