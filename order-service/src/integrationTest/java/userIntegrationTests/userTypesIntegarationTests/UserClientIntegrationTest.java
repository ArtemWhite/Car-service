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

class UserClientIntegrationTest extends UserBaseIntegrationTest {

    private String testClientId;
    private String testCarId;
    private String testOrderId;

    @BeforeEach
    void setUp() throws Exception {
        cleanUpUsers();

        testClientId = UUID.randomUUID().toString();
        createUser(testClientId, "CLIENT", "client_specific@test.com", "ACTIVE");
        createClient(testClientId);

        testCarId = createTestCar();

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void shouldGetClientOrders() throws Exception {
        Map<String, Object> orderRequest = new HashMap<>();
        orderRequest.put("carId", testCarId);
        orderRequest.put("orderType", "IN_STOCK");

        String response = mockMvc.perform(post("/api/client/orders")
                        .header("X-User-Id", testClientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        testOrderId = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(get("/api/client/me/orders")
                        .header("X-User-Id", testClientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(testOrderId));
    }

    @Test
    void shouldGetClientTestDrives() throws Exception {
        Map<String, Object> tdRequest = new HashMap<>();
        tdRequest.put("carId", testCarId);
        tdRequest.put("requestedTime", java.time.LocalDateTime.now().plusDays(1).toString());

        mockMvc.perform(post("/api/client/cars/{carId}/test-drive", testCarId)
                        .header("X-User-Id", testClientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tdRequest)))
                .andExpect(status().isAccepted());

        mockMvc.perform(get("/api/client/me/test-drives")
                        .header("X-User-Id", testClientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldAddOrderToClient() throws Exception {
        Map<String, Object> orderRequest = new HashMap<>();
        orderRequest.put("carId", testCarId);
        orderRequest.put("orderType", "IN_STOCK");

        mockMvc.perform(post("/api/client/orders")
                        .header("X-User-Id", testClientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated());

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM client_orders WHERE client_id = ?::uuid",
                Integer.class, UUID.fromString(testClientId)
        );
        assertThat(count).isEqualTo(1);
    }

    @Test
    void shouldAddTestDriveToClient() throws Exception {
        Map<String, Object> tdRequest = new HashMap<>();
        tdRequest.put("carId", testCarId);
        tdRequest.put("requestedTime", java.time.LocalDateTime.now().plusDays(1).toString());

        mockMvc.perform(post("/api/client/cars/{carId}/test-drive", testCarId)
                        .header("X-User-Id", testClientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tdRequest)))
                .andExpect(status().isAccepted());

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM client_test_drives WHERE client_id = ?::uuid",
                Integer.class, UUID.fromString(testClientId)
        );
        assertThat(count).isEqualTo(1);
    }

    @Test
    void shouldReturnEmptyOrdersForNewClient() throws Exception {
        String newClientId = UUID.randomUUID().toString();
        createUser(newClientId, "CLIENT", "newclient@test.com", "ACTIVE");
        createClient(newClientId);

        mockMvc.perform(get("/api/client/me/orders")
                        .header("X-User-Id", newClientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldReturnOrderCount() throws Exception {
        for (int i = 0; i < 3; i++) {
            Map<String, Object> orderRequest = new HashMap<>();
            orderRequest.put("carId", testCarId);
            orderRequest.put("orderType", "IN_STOCK");

            mockMvc.perform(post("/api/client/orders")
                            .header("X-User-Id", testClientId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(orderRequest)))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(get("/api/client/me")
                        .header("X-User-Id", testClientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderCount").value(3));
    }

    @Test
    void shouldReturnTestDriveCount() throws Exception {
        for (int i = 0; i < 2; i++) {
            Map<String, Object> tdRequest = new HashMap<>();
            tdRequest.put("carId", testCarId);
            tdRequest.put("requestedTime", java.time.LocalDateTime.now().plusDays(i + 1).toString());

            mockMvc.perform(post("/api/client/cars/{carId}/test-drive", testCarId)
                            .header("X-User-Id", testClientId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(tdRequest)))
                    .andExpect(status().isAccepted());
        }

        mockMvc.perform(get("/api/client/me")
                        .header("X-User-Id", testClientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.testDriveCount").value(2));
    }

    @Test
    void shouldSubscribeToNewsletter() throws Exception {
        mockMvc.perform(post("/api/client/me/newsletter/subscribe")
                        .header("X-User-Id", testClientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newsletterSubscribed").value(true));

        Boolean subscribed = jdbcTemplate.queryForObject(
                "SELECT newsletter_subscribed FROM clients WHERE user_id = ?::uuid",
                Boolean.class, UUID.fromString(testClientId)
        );
        assertThat(subscribed).isTrue();
    }

    @Test
    void shouldUnsubscribeFromNewsletter() throws Exception {
        jdbcTemplate.update(
                "UPDATE clients SET newsletter_subscribed = true WHERE user_id = ?::uuid",
                UUID.fromString(testClientId)
        );

        mockMvc.perform(post("/api/client/me/newsletter/unsubscribe")
                        .header("X-User-Id", testClientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newsletterSubscribed").value(false));
    }

    @Test
    void shouldSetPreferredContactMethod() throws Exception {
        mockMvc.perform(put("/api/client/me/contact-method")
                        .header("X-User-Id", testClientId)
                        .param("method", "phone"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preferredContactMethod").value("phone"));

        String method = jdbcTemplate.queryForObject(
                "SELECT preferred_contact_method FROM clients WHERE user_id = ?::uuid",
                String.class, UUID.fromString(testClientId)
        );
        assertThat(method).isEqualTo("phone");
    }

    @Test
    void shouldNotUnsubscribeIfNotSubscribed() throws Exception {
        mockMvc.perform(post("/api/client/me/newsletter/unsubscribe")
                        .header("X-User-Id", testClientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newsletterSubscribed").value(false));
    }

    @Test
    void shouldPersistNewsletterPreference() throws Exception {
        mockMvc.perform(post("/api/client/me/newsletter/subscribe")
                        .header("X-User-Id", testClientId))
                .andExpect(status().isOk());

        entityManager.flush();
        entityManager.clear();

        Boolean subscribed = jdbcTemplate.queryForObject(
                "SELECT newsletter_subscribed FROM clients WHERE user_id = ?::uuid",
                Boolean.class, UUID.fromString(testClientId)
        );
        assertThat(subscribed).isTrue();
    }

    @Test
    void shouldPersistContactMethod() throws Exception {
        mockMvc.perform(put("/api/client/me/contact-method")
                        .header("X-User-Id", testClientId)
                        .param("method", "email"))
                .andExpect(status().isOk());

        entityManager.flush();
        entityManager.clear();

        String method = jdbcTemplate.queryForObject(
                "SELECT preferred_contact_method FROM clients WHERE user_id = ?::uuid",
                String.class, UUID.fromString(testClientId)
        );
        assertThat(method).isEqualTo("email");
    }

    @Test
    void shouldClientGetOwnFullName() throws Exception {
        mockMvc.perform(get("/api/client/me")
                        .header("X-User-Id", testClientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").exists());
    }

    @Test
    void shouldClientUpdateContactInfo() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("email", "updated_contact@test.com");
        request.put("phone", "+79991112233");

        mockMvc.perform(put("/api/users/me")
                        .header("X-User-Id", testClientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("updated_contact@test.com"))
                .andExpect(jsonPath("$.phone").value("+79991112233"));
    }

    @Test    void shouldGetClientProfileWithFullDetails() throws Exception {
        jdbcTemplate.update(
                "UPDATE clients SET preferred_contact_method = 'phone', newsletter_subscribed = true WHERE user_id = ?::uuid",
                UUID.fromString(testClientId)
        );

        mockMvc.perform(get("/api/client/me")
                        .header("X-User-Id", testClientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preferredContactMethod").value("phone"))
                .andExpect(jsonPath("$.newsletterSubscribed").value(true))
                .andExpect(jsonPath("$.userType").value("CLIENT"));
    }
}