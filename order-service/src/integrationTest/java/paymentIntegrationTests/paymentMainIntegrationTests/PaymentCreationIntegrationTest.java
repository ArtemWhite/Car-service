package paymentIntegrationTests.paymentMainIntegrationTests;

import dealerShipOrder.BaseIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@Transactional
class PaymentCreationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String clientId;
    private String adminId;
    private String managerId;
    private String orderId;
    private String carId;

    @BeforeEach
    void setUp() throws Exception {
        createReferenceData();
        createTestOrder();
    }

    private void createReferenceData() throws Exception {
        clientId = UUID.randomUUID().toString();
        adminId = UUID.randomUUID().toString();
        managerId = UUID.randomUUID().toString();

        createUser(clientId, "CLIENT");
        createUser(adminId, "SYSTEM_ADMIN");
        createUser(managerId, "MANAGER");
        createClient(clientId);
        createManager(managerId);
        createSystemAdmin(adminId);

        carId = createCar();
    }

    private String createCar() throws Exception {
        return UUID.randomUUID().toString();
    }

    private void createUser(String id, String type) {
        jdbcTemplate.update(
                "INSERT INTO users (id, first_name, last_name, email, phone, password_hash, status_id, user_type_id, created_at, updated_at, removed) " +
                        "VALUES (?::uuid, 'Test', 'User', ?, '1234567890', 'hash', " +
                        "(SELECT id FROM user_statuses WHERE name = 'ACTIVE'), " +
                        "(SELECT id FROM user_types WHERE name = ?), NOW(), NOW(), false)",
                UUID.fromString(id), id + "@test.com", type
        );
    }

    private void createClient(String id) {
        jdbcTemplate.update(
                "INSERT INTO clients (user_id, preferred_contact_method, newsletter_subscribed) " +
                        "VALUES (?::uuid, 'EMAIL', false)",
                UUID.fromString(id)
        );
    }

    private void createManager(String id) {
        jdbcTemplate.update(
                "INSERT INTO managers (user_id, position_id, max_concurrent_orders, max_concurrent_test_drives, available) " +
                        "VALUES (?::uuid, (SELECT id FROM manager_positions LIMIT 1), 10, 5, true)",
                UUID.fromString(id)
        );
    }

    private void createSystemAdmin(String id) {
        jdbcTemplate.update(
                "INSERT INTO system_admins (user_id, admin_level_id, last_login_at) " +
                        "VALUES (?::uuid, (SELECT id FROM admin_levels LIMIT 1), NOW())",
                UUID.fromString(id)
        );
    }

    private void createTestOrder() throws Exception {
        Map<String, Object> orderRequest = new HashMap<>();
        orderRequest.put("carId", carId);
        orderRequest.put("orderType", "IN_STOCK");

        String response = mockMvc.perform(post("/api/client/orders")
                        .header("X-User-Id", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        orderId = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(post("/api/manager/orders/{id}/assign", orderId)
                        .header("X-User-Id", managerId))
                .andExpect(status().isOk());

        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("status", "AWAITING_PAYMENT");
        mockMvc.perform(put("/api/admin/orders/{id}", orderId)
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldCreatePaymentSuccessfully() throws Exception {
        Map<String, Object> request = Map.of(
                "orderId", orderId,
                "amount", 2500000.0,
                "method", "CARD"
        );

        mockMvc.perform(post("/api/client/payments")
                        .header("X-User-Id", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(orderId))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.amount").value(2500000.0));
    }

    @Test
    void shouldFailCreatePaymentWithoutOrderId() throws Exception {
        Map<String, Object> request = Map.of(
                "amount", 2500000.0,
                "method", "CARD"
        );

        mockMvc.perform(post("/api/client/payments")
                        .header("X-User-Id", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailCreatePaymentWithoutAmount() throws Exception {
        Map<String, Object> request = Map.of(
                "orderId", orderId,
                "method", "CARD"
        );

        mockMvc.perform(post("/api/client/payments")
                        .header("X-User-Id", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailCreatePaymentWithNegativeAmount() throws Exception {
        Map<String, Object> request = Map.of(
                "orderId", orderId,
                "amount", -100.0,
                "method", "CARD"
        );

        mockMvc.perform(post("/api/client/payments")
                        .header("X-User-Id", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailCreatePaymentWithZeroAmount() throws Exception {
        Map<String, Object> request = Map.of(
                "orderId", orderId,
                "amount", 0.0,
                "method", "CARD"
        );

        mockMvc.perform(post("/api/client/payments")
                        .header("X-User-Id", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailCreatePaymentWithoutMethod() throws Exception {
        Map<String, Object> request = Map.of(
                "orderId", orderId,
                "amount", 2500000.0
        );

        mockMvc.perform(post("/api/client/payments")
                        .header("X-User-Id", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailCreatePaymentForNonExistentOrder() throws Exception {
        Map<String, Object> request = Map.of(
                "orderId", UUID.randomUUID().toString(),
                "amount", 2500000.0,
                "method", "CARD"
        );

        mockMvc.perform(post("/api/client/payments")
                        .header("X-User-Id", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailCreateDuplicatePaymentForOrder() throws Exception {
        Map<String, Object> request = Map.of(
                "orderId", orderId,
                "amount", 2500000.0,
                "method", "CARD"
        );

        mockMvc.perform(post("/api/client/payments")
                        .header("X-User-Id", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/client/payments")
                        .header("X-User-Id", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}