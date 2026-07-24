package userIntegrationTests.userMainIntegrationTests;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

class UserAuthIntegrationTest extends UserBaseIntegrationTest {

    private String testClientId;
    private String testClientEmail;
    private String testClientPasswordHash;

    @BeforeEach
    void setUp() {
        cleanUpUsers();
        createTestUsers();

        testClientId = UUID.randomUUID().toString();
        testClientEmail = "authclient@test.com";
        testClientPasswordHash = "hashed_" + testClientId.substring(0, 8);

        createUser(testClientId, "CLIENT", testClientEmail, "ACTIVE");
        createClient(testClientId);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void shouldAuthenticateWithCorrectPassword() throws Exception {
        Map<String, String> authRequest = new HashMap<>();
        authRequest.put("email", testClientEmail);
        authRequest.put("password", testClientPasswordHash);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(testClientId))
                .andExpect(jsonPath("$.userType").value("CLIENT"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldNotAuthenticateWithWrongPassword() throws Exception {
        Map<String, String> authRequest = new HashMap<>();
        authRequest.put("email", testClientEmail);
        authRequest.put("password", "wrong_password");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid credentials"));
    }

    @Test
    void shouldNotAuthenticateBlockedUser() throws Exception {
        Map<String, String> authRequest = new HashMap<>();
        authRequest.put("email", "blocked@test.com");
        authRequest.put("password", "hashed_" + blockedUserId.substring(0, 8));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Account is blocked"));
    }

    @Test
    void shouldNotAuthenticateInactiveUser() throws Exception {
        Map<String, String> authRequest = new HashMap<>();
        authRequest.put("email", "inactive@test.com");
        authRequest.put("password", "hashed_" + inactiveUserId.substring(0, 8));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Account is inactive"));
    }

    @Test
    void shouldChangePasswordSuccessfully() throws Exception {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", testClientEmail);
        loginRequest.put("password", testClientPasswordHash);

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode loginJson = objectMapper.readTree(loginResponse);
        String token = loginJson.get("token").asText();

        Map<String, String> changePasswordRequest = new HashMap<>();
        changePasswordRequest.put("oldPassword", testClientPasswordHash);
        changePasswordRequest.put("newPassword", "new_secure_password_456");

        mockMvc.perform(post("/api/users/me/password")
                        .header("Authorization", "Bearer " + token)
                        .header("X-User-Id", testClientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequest)))
                .andExpect(status().isOk());

        Map<String, String> newLoginRequest = new HashMap<>();
        newLoginRequest.put("email", testClientEmail);
        newLoginRequest.put("password", "new_secure_password_456");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(testClientId));
    }

    @Test
    void shouldNotChangePasswordWithWrongOldPassword() throws Exception {
        Map<String, String> changePasswordRequest = new HashMap<>();
        changePasswordRequest.put("oldPassword", "wrong_password");
        changePasswordRequest.put("newPassword", "new_password");

        mockMvc.perform(post("/api/users/me/password")
                        .header("X-User-Id", testClientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Old password is incorrect"));
    }

    @Test
    void shouldHashPasswordOnSave() {
        String userId = UUID.randomUUID().toString();
        String plainPassword = "mySecretPassword123";

        jdbcTemplate.update(
                "INSERT INTO users (id, first_name, last_name, email, phone, password_hash, status_id, user_type_id, created_at, updated_at, removed) " +
                        "VALUES (?::uuid, 'Test', 'User', 'hash@test.com', '123', ?, " +
                        "(SELECT id FROM user_statuses WHERE name = 'ACTIVE'), " +
                        "(SELECT id FROM user_types WHERE name = 'CLIENT'), NOW(), NOW(), false)",
                UUID.fromString(userId), Integer.toHexString(plainPassword.hashCode())
        );

        String storedHash = jdbcTemplate.queryForObject(
                "SELECT password_hash FROM users WHERE id = ?::uuid",
                String.class, UUID.fromString(userId)
        );

        assertThat(storedHash).isEqualTo(Integer.toHexString(plainPassword.hashCode()));
        assertThat(storedHash).isNotEqualTo(plainPassword);
    }

    @Test
    void shouldUpdateLastPasswordChangeAt() throws Exception {
        String originalDate = jdbcTemplate.queryForObject(
                "SELECT last_password_change_at FROM users WHERE id = ?::uuid",
                String.class, UUID.fromString(testClientId)
        );

        Map<String, String> changePasswordRequest = new HashMap<>();
        changePasswordRequest.put("oldPassword", testClientPasswordHash);
        changePasswordRequest.put("newPassword", "updated_password_789");

        mockMvc.perform(post("/api/users/me/password")
                        .header("X-User-Id", testClientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequest)))
                .andExpect(status().isOk());

        String newDate = jdbcTemplate.queryForObject(
                "SELECT last_password_change_at FROM users WHERE id = ?::uuid",
                String.class, UUID.fromString(testClientId)
        );

        assertThat(newDate).isNotEqualTo(originalDate);
    }

    @Test
    void shouldUpdateLastActiveAtOnLogin() throws Exception {
        String originalActive = jdbcTemplate.queryForObject(
                "SELECT last_active_at FROM users WHERE id = ?::uuid",
                String.class, UUID.fromString(testClientId)
        );

        Thread.sleep(1000);

        Map<String, String> authRequest = new HashMap<>();
        authRequest.put("email", testClientEmail);
        authRequest.put("password", testClientPasswordHash);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk());

        String newActive = jdbcTemplate.queryForObject(
                "SELECT last_active_at FROM users WHERE id = ?::uuid",
                String.class, UUID.fromString(testClientId)
        );

        assertThat(newActive).isNotEqualTo(originalActive);
    }

    @Test
    void shouldBlockUserAfterFailedAttempts() throws Exception {
        String vulnerableUserId = UUID.randomUUID().toString();
        String vulnerableEmail = "vulnerable@test.com";
        createUser(vulnerableUserId, "CLIENT", vulnerableEmail, "ACTIVE");
        createClient(vulnerableUserId);

        for (int i = 0; i < 5; i++) {
            Map<String, String> wrongAuth = new HashMap<>();
            wrongAuth.put("email", vulnerableEmail);
            wrongAuth.put("password", "wrong_attempt_" + i);

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(wrongAuth)))
                    .andExpect(status().isUnauthorized());
        }

        Map<String, String> finalWrongAuth = new HashMap<>();
        finalWrongAuth.put("email", vulnerableEmail);
        finalWrongAuth.put("password", "final_wrong");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(finalWrongAuth)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Account is blocked"));

        String status = getUserStatus(vulnerableUserId);
        assertThat(status).isEqualTo("BLOCKED");
    }

    @Test
    void shouldUnblockUserByAdmin() throws Exception {
        assertThat(getUserStatus(blockedUserId)).isEqualTo("BLOCKED");

        mockMvc.perform(post("/api/admin/users/{userId}/unblock", blockedUserId)
                        .header("X-User-Id", adminId))
                .andExpect(status().isOk());

        assertThat(getUserStatus(blockedUserId)).isEqualTo("ACTIVE");
    }

    @Test
    void shouldRequirePasswordComplexity() throws Exception {
        Map<String, Object> createRequest = new HashMap<>();
        createRequest.put("firstName", "Weak");
        createRequest.put("lastName", "Password");
        createRequest.put("email", "weak@test.com");
        createRequest.put("phone", "+71234567890");
        createRequest.put("password", "123");
        createRequest.put("userType", "CLIENT");

        mockMvc.perform(post("/api/admin/users")
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Password must be at least 8 characters"));
    }

    @Test
    void shouldNotReuseOldPassword() throws Exception {
        Map<String, String> changeRequest1 = new HashMap<>();
        changeRequest1.put("oldPassword", testClientPasswordHash);
        changeRequest1.put("newPassword", "new_password_123");

        mockMvc.perform(post("/api/users/me/password")
                        .header("X-User-Id", testClientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changeRequest1)))
                .andExpect(status().isOk());

        Map<String, String> changeRequest2 = new HashMap<>();
        changeRequest2.put("oldPassword", "new_password_123");
        changeRequest2.put("newPassword", testClientPasswordHash);

        mockMvc.perform(post("/api/users/me/password")
                        .header("X-User-Id", testClientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changeRequest2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Cannot reuse old password"));
    }

    @Test
    void shouldExpirePasswordAfterDays() throws Exception {
        jdbcTemplate.update(
                "UPDATE users SET last_password_change_at = '2020-01-01 00:00:00' WHERE id = ?::uuid",
                UUID.fromString(testClientId)
        );

        Map<String, String> authRequest = new HashMap<>();
        authRequest.put("email", testClientEmail);
        authRequest.put("password", testClientPasswordHash);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Password expired, please change your password"));
    }

    @Test
    void shouldLogoutAndInvalidateToken() throws Exception {
        Map<String, String> authRequest = new HashMap<>();
        authRequest.put("email", testClientEmail);
        authRequest.put("password", testClientPasswordHash);

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode loginJson = objectMapper.readTree(loginResponse);
        String token = loginJson.get("token").asText();

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + token)
                        .header("X-User-Id", testClientId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + token)
                        .header("X-User-Id", testClientId))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid or expired token"));
    }
}