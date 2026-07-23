package userIntegrationTests.userSpecificLogicIntegrationTests;

import com.fasterxml.jackson.databind.JsonNode;
import dealerShipOrder.domain.repository.usersRepository.userRepository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import userIntegrationTests.userMainIntegrationTests.UserBaseIntegrationTest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

class UserStatusIntegrationTest extends UserBaseIntegrationTest {

    private String testUserId;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        cleanUpUsers();
        createTestUsers();

        testUserId = UUID.randomUUID().toString();
        createUser(testUserId, "CLIENT", "statustest@test.com", "ACTIVE");
        createClient(testUserId);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void shouldActivateUser() throws Exception {
        jdbcTemplate.update(
                "UPDATE users SET status_id = (SELECT id FROM user_statuses WHERE name = 'INACTIVE') WHERE id = ?::uuid",
                UUID.fromString(testUserId)
        );

        assertThat(getUserStatus(testUserId)).isEqualTo("INACTIVE");

        mockMvc.perform(post("/api/admin/users/{userId}/activate", testUserId)
                        .header("X-User-Id", adminId))
                .andExpect(status().isOk());

        assertThat(getUserStatus(testUserId)).isEqualTo("ACTIVE");
    }

    @Test
    void shouldDeactivateUser() throws Exception {
        assertThat(getUserStatus(testUserId)).isEqualTo("ACTIVE");

        mockMvc.perform(post("/api/admin/users/{userId}/deactivate", testUserId)
                        .header("X-User-Id", adminId))
                .andExpect(status().isOk());

        assertThat(getUserStatus(testUserId)).isEqualTo("INACTIVE");
    }

    @Test
    void shouldBlockUser() throws Exception {
        assertThat(getUserStatus(testUserId)).isEqualTo("ACTIVE");

        mockMvc.perform(post("/api/admin/users/{userId}/block", testUserId)
                        .header("X-User-Id", adminId)
                        .param("reason", "Violation of terms"))
                .andExpect(status().isOk());

        assertThat(getUserStatus(testUserId)).isEqualTo("BLOCKED");
    }

    @Test
    void shouldNotActivateAlreadyActiveUser() throws Exception {
        mockMvc.perform(post("/api/admin/users/{userId}/activate", testUserId)
                        .header("X-User-Id", adminId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("User is already active"));
    }

    @Test
    void shouldNotBlockAlreadyBlockedUser() throws Exception {
        jdbcTemplate.update(
                "UPDATE users SET status_id = (SELECT id FROM user_statuses WHERE name = 'BLOCKED') WHERE id = ?::uuid",
                UUID.fromString(testUserId)
        );

        mockMvc.perform(post("/api/admin/users/{userId}/block", testUserId)
                        .header("X-User-Id", adminId)
                        .param("reason", "Try to block again"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("User is already blocked"));
    }

    @Test
    void shouldRestoreInactiveUser() throws Exception {
        jdbcTemplate.update(
                "UPDATE users SET status_id = (SELECT id FROM user_statuses WHERE name = 'INACTIVE') WHERE id = ?::uuid",
                UUID.fromString(testUserId)
        );

        mockMvc.perform(post("/api/admin/users/{userId}/restore", testUserId)
                        .header("X-User-Id", adminId))
                .andExpect(status().isOk());

        assertThat(getUserStatus(testUserId)).isEqualTo("ACTIVE");
    }

    @Test
    void shouldNotRestoreDeletedUser() throws Exception {
        jdbcTemplate.update(
                "UPDATE users SET removed = true WHERE id = ?::uuid",
                UUID.fromString(testUserId)
        );

        mockMvc.perform(post("/api/admin/users/{userId}/restore", testUserId)
                        .header("X-User-Id", adminId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldChangeStatusFromActiveToBlocked() throws Exception {
        mockMvc.perform(post("/api/admin/users/{userId}/block", testUserId)
                        .header("X-User-Id", adminId)
                        .param("reason", "Test blockage"))
                .andExpect(status().isOk());

        assertThat(getUserStatus(testUserId)).isEqualTo("BLOCKED");

        Map<String, String> authRequest = new HashMap<>();
        authRequest.put("email", "statustest@test.com");
        authRequest.put("password", "hashed_" + testUserId.substring(0, 8));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldChangeStatusFromBlockedToActive() throws Exception {
        jdbcTemplate.update(
                "UPDATE users SET status_id = (SELECT id FROM user_statuses WHERE name = 'BLOCKED') WHERE id = ?::uuid",
                UUID.fromString(testUserId)
        );

        mockMvc.perform(post("/api/admin/users/{userId}/unblock", testUserId)
                        .header("X-User-Id", adminId))
                .andExpect(status().isOk());

        assertThat(getUserStatus(testUserId)).isEqualTo("ACTIVE");

        Map<String, String> authRequest = new HashMap<>();
        authRequest.put("email", "statustest@test.com");
        authRequest.put("password", "hashed_" + testUserId.substring(0, 8));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldLogStatusChange() throws Exception {
        int auditCountBefore = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM audit_log_entries WHERE admin_id = ?::uuid AND action LIKE '%BLOCK%'",
                Integer.class, UUID.fromString(adminId)
        );

        mockMvc.perform(post("/api/admin/users/{userId}/block", testUserId)
                        .header("X-User-Id", adminId)
                        .param("reason", "Test block with audit"))
                .andExpect(status().isOk());

        int auditCountAfter = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM audit_log_entries WHERE admin_id = ?::uuid AND action LIKE '%BLOCK%'",
                Integer.class, UUID.fromString(adminId)
        );

        assertThat(auditCountAfter).isGreaterThan(auditCountBefore);
    }

    @Test
    void shouldPreventLoginWhenBlocked() throws Exception {
        jdbcTemplate.update(
                "UPDATE users SET status_id = (SELECT id FROM user_statuses WHERE name = 'BLOCKED') WHERE id = ?::uuid",
                UUID.fromString(testUserId)
        );

        Map<String, String> authRequest = new HashMap<>();
        authRequest.put("email", "statustest@test.com");
        authRequest.put("password", "hashed_" + testUserId.substring(0, 8));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Account is blocked"));
    }

    @Test
    void shouldAllowLoginWhenActive() throws Exception {
        Map<String, String> authRequest = new HashMap<>();
        authRequest.put("email", "statustest@test.com");
        authRequest.put("password", "hashed_" + testUserId.substring(0, 8));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(testUserId));
    }

    @Test
    void shouldShowCorrectStatusDisplayName() throws Exception {
        String response = mockMvc.perform(get("/api/users/{id}", testUserId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(response);

        assertThat(json.get("status").asText()).isEqualTo("ACTIVE");
        assertThat(json.get("statusDisplayName").asText()).isEqualTo("Активен");
    }

    @Test
    void shouldCountByStatus() throws Exception {
        long activeCount = userRepository.countByStatus(dealerShipOrder.domain.models.users.UserStatus.ACTIVE);
        long blockedCount = userRepository.countByStatus(dealerShipOrder.domain.models.users.UserStatus.BLOCKED);
        long inactiveCount = userRepository.countByStatus(dealerShipOrder.domain.models.users.UserStatus.INACTIVE);

        assertThat(activeCount).isGreaterThan(0);
    }

    @Test
    void shouldFilterUsersByStatus() throws Exception {
        mockMvc.perform(get("/api/admin/users/filters")
                        .header("X-User-Id", adminId)
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users").isArray())
                .andExpect(jsonPath("$.activeCount").value(org.hamcrest.Matchers.greaterThan(0)));
    }

    @Test
    void shouldTransitionThroughAllStatuses() throws Exception {
        assertThat(getUserStatus(testUserId)).isEqualTo("ACTIVE");

        mockMvc.perform(post("/api/admin/users/{userId}/deactivate", testUserId)
                        .header("X-User-Id", adminId))
                .andExpect(status().isOk());
        assertThat(getUserStatus(testUserId)).isEqualTo("INACTIVE");

        mockMvc.perform(post("/api/admin/users/{userId}/activate", testUserId)
                        .header("X-User-Id", adminId))
                .andExpect(status().isOk());
        assertThat(getUserStatus(testUserId)).isEqualTo("ACTIVE");

        mockMvc.perform(post("/api/admin/users/{userId}/block", testUserId)
                        .header("X-User-Id", adminId)
                        .param("reason", "Test"))
                .andExpect(status().isOk());
        assertThat(getUserStatus(testUserId)).isEqualTo("BLOCKED");

        mockMvc.perform(post("/api/admin/users/{userId}/unblock", testUserId)
                        .header("X-User-Id", adminId))
                .andExpect(status().isOk());
        assertThat(getUserStatus(testUserId)).isEqualTo("ACTIVE");
    }

    @Test
    void shouldNotAllowSelfDeactivation() throws Exception {
        mockMvc.perform(post("/api/admin/users/{userId}/deactivate", adminId)
                        .header("X-User-Id", adminId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Cannot deactivate your own account"));
    }

    @Test
    void shouldNotAllowSelfBlock() throws Exception {
        mockMvc.perform(post("/api/admin/users/{userId}/block", adminId)
                        .header("X-User-Id", adminId)
                        .param("reason", "Trying to block self"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Cannot block your own account"));
    }

    @Test
    void shouldUpdateUserStatusViaUpdateEndpoint() throws Exception {
        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("status", "INACTIVE");

        mockMvc.perform(put("/api/admin/users/{userId}", testUserId)
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        assertThat(getUserStatus(testUserId)).isEqualTo("INACTIVE");
    }
}