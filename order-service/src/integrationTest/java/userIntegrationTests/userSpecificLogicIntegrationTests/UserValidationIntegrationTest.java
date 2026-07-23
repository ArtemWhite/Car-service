package userIntegrationTests.userSpecificLogicIntegrationTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import userIntegrationTests.userMainIntegrationTests.UserBaseIntegrationTest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

class UserValidationIntegrationTest extends UserBaseIntegrationTest {

    @BeforeEach
    void setUp() {
        cleanUpUsers();
        createTestUsers();
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void shouldNotCreateUserWithBlankFirstName() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("firstName", "");
        request.put("lastName", "User");
        request.put("email", "blankfirst@test.com");
        request.put("password", "SecurePass123");
        request.put("userType", "CLIENT");

        mockMvc.perform(post("/api/admin/users")
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotCreateUserWithBlankLastName() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("firstName", "Test");
        request.put("lastName", "");
        request.put("email", "blanklast@test.com");
        request.put("password", "SecurePass123");
        request.put("userType", "CLIENT");

        mockMvc.perform(post("/api/admin/users")
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotCreateUserWithBlankEmail() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("firstName", "Test");
        request.put("lastName", "User");
        request.put("email", "");
        request.put("password", "SecurePass123");
        request.put("userType", "CLIENT");

        mockMvc.perform(post("/api/admin/users")
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotCreateUserWithInvalidEmailFormat() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("firstName", "Test");
        request.put("lastName", "User");
        request.put("email", "not-an-email");
        request.put("password", "SecurePass123");
        request.put("userType", "CLIENT");

        mockMvc.perform(post("/api/admin/users")
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotCreateUserWithPhoneTooLong() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("firstName", "Test");
        request.put("lastName", "User");
        request.put("email", "longphone@test.com");
        request.put("phone", "+123456789012345678901234567890");
        request.put("password", "SecurePass123");
        request.put("userType", "CLIENT");

        mockMvc.perform(post("/api/admin/users")
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotCreateUserWithEmailTooLong() throws Exception {
        String longEmail = "a".repeat(101) + "@test.com";
        Map<String, Object> request = new HashMap<>();
        request.put("firstName", "Test");
        request.put("lastName", "User");
        request.put("email", longEmail);
        request.put("password", "SecurePass123");
        request.put("userType", "CLIENT");

        mockMvc.perform(post("/api/admin/users")
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotCreateUserWithPasswordTooShort() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("firstName", "Test");
        request.put("lastName", "User");
        request.put("email", "shortpass@test.com");
        request.put("password", "123");
        request.put("userType", "CLIENT");

        mockMvc.perform(post("/api/admin/users")
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotCreateUserWithPasswordTooLong() throws Exception {
        String longPassword = "a".repeat(101);
        Map<String, Object> request = new HashMap<>();
        request.put("firstName", "Test");
        request.put("lastName", "User");
        request.put("email", "longpass@test.com");
        request.put("password", longPassword);
        request.put("userType", "CLIENT");

        mockMvc.perform(post("/api/admin/users")
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotCreateDuplicateUser() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("firstName", "Test");
        request.put("lastName", "User");
        request.put("email", "client@test.com");
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
    void shouldReturn404WhenUserNotFound() throws Exception {
        mockMvc.perform(get("/api/users/{id}", UUID.randomUUID().toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldHandleLargeUserList() throws Exception {
        for (int i = 0; i < 100; i++) {
            String userId = UUID.randomUUID().toString();
            createUser(userId, "CLIENT", "large" + i + "@test.com", "ACTIVE");
            createClient(userId);
        }

        entityManager.flush();
        entityManager.clear();

        long startTime = System.currentTimeMillis();

        mockMvc.perform(get("/api/admin/users")
                        .header("X-User-Id", adminId)
                        .param("page", "0")
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(org.hamcrest.Matchers.greaterThan(100)));

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        assertThat(duration).isLessThan(5000);
    }

    @Test
    void shouldHandleConcurrentUpdates() throws Exception {
        String concurrentUserId = UUID.randomUUID().toString();
        createUser(concurrentUserId, "CLIENT", "concurrent@test.com", "ACTIVE");
        createClient(concurrentUserId);

        ExecutorService executor = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 10; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    Map<String, Object> request = new HashMap<>();
                    request.put("firstName", "Concurrent" + index);

                    mockMvc.perform(put("/api/users/me")
                                    .header("X-User-Id", concurrentUserId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                            .andExpect(status().isOk());
                } catch (Exception e) {

                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        String firstName = jdbcTemplate.queryForObject(
                "SELECT first_name FROM users WHERE id = ?::uuid",
                String.class, UUID.fromString(concurrentUserId)
        );
        assertThat(firstName).startsWith("Concurrent");
    }

    @Test
    void shouldRollbackTransactionOnError() throws Exception {
        String duplicateEmail = "rollback@test.com";

        Map<String, Object> request1 = new HashMap<>();
        request1.put("firstName", "First");
        request1.put("lastName", "User");
        request1.put("email", duplicateEmail);
        request1.put("password", "SecurePass123");
        request1.put("userType", "CLIENT");

        mockMvc.perform(post("/api/admin/users")
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        Map<String, Object> request2 = new HashMap<>();
        request2.put("firstName", "Second");
        request2.put("lastName", "User");
        request2.put("email", duplicateEmail);
        request2.put("password", "SecurePass123");
        request2.put("userType", "CLIENT");

        mockMvc.perform(post("/api/admin/users")
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isBadRequest());

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE email = ? AND removed = false",
                Integer.class, duplicateEmail
        );
        assertThat(count).isEqualTo(1);
    }

    @Test
    void shouldNotCreateUserWithNullUserType() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("firstName", "Test");
        request.put("lastName", "User");
        request.put("email", "nulltype@test.com");
        request.put("password", "SecurePass123");
        request.put("userType", null);

        mockMvc.perform(post("/api/admin/users")
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotUpdateUserWithInvalidStatus() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("status", "INVALID_STATUS");

        mockMvc.perform(put("/api/admin/users/{userId}", clientId)
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotCreateUserWithInvalidUserType() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("firstName", "Test");
        request.put("lastName", "User");
        request.put("email", "invalidtype@test.com");
        request.put("password", "SecurePass123");
        request.put("userType", "INVALID_TYPE");

        mockMvc.perform(post("/api/admin/users")
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotCreateUserWithNegativeAge() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("firstName", "Test");
        request.put("lastName", "User");
        request.put("email", "negativeage@test.com");
        request.put("password", "SecurePass123");
        request.put("userType", "CLIENT");
        request.put("age", -5);

        mockMvc.perform(post("/api/admin/users")
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldValidateSpecialCharactersInName() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("firstName", "Test@#$%");
        request.put("lastName", "User");
        request.put("email", "special@test.com");
        request.put("password", "SecurePass123");
        request.put("userType", "CLIENT");

        mockMvc.perform(post("/api/admin/users")
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldValidateMaxFieldLengths() throws Exception {
        String longFirstName = "a".repeat(51);
        Map<String, Object> request = new HashMap<>();
        request.put("firstName", longFirstName);
        request.put("lastName", "User");
        request.put("email", "maxlength@test.com");
        request.put("password", "SecurePass123");
        request.put("userType", "CLIENT");

        mockMvc.perform(post("/api/admin/users")
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}