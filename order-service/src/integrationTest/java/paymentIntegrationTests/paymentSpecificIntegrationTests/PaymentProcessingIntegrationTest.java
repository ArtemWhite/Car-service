package paymentIntegrationTests.paymentSpecificIntegrationTests;

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

import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@Transactional
class PaymentProcessingIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    private String clientId;
    private String adminId;
    private String managerId;
    private String paymentId;
    private String orderId;
    private String carId;

    @BeforeEach
    void setUp() throws Exception {
        createReferenceData();
        orderId = createOrderAndSetAwaitingPayment();
        paymentId = createPayment();
    }

    private void createReferenceData() throws Exception {
        clientId = UUID.randomUUID().toString();
        adminId = UUID.randomUUID().toString();
        managerId = UUID.randomUUID().toString();

        UUID statusId = jdbcTemplate.queryForObject(
                "SELECT id FROM user_statuses WHERE name = 'ACTIVE'", UUID.class);
        UUID userTypeIdClient = jdbcTemplate.queryForObject(
                "SELECT id FROM user_types WHERE name = 'CLIENT'", UUID.class);
        UUID userTypeIdManager = jdbcTemplate.queryForObject(
                "SELECT id FROM user_types WHERE name = 'MANAGER'", UUID.class);
        UUID userTypeIdAdmin = jdbcTemplate.queryForObject(
                "SELECT id FROM user_types WHERE name = 'SYSTEM_ADMIN'", UUID.class);
        UUID adminLevelId = jdbcTemplate.queryForObject(
                "SELECT id FROM admin_levels WHERE name = 'SUPER_ADMIN'", UUID.class);
        UUID positionId = jdbcTemplate.queryForObject(
                "SELECT id FROM manager_positions WHERE name = 'SALES_MANAGER'", UUID.class);

        jdbcTemplate.update(
                "INSERT INTO users (id, first_name, last_name, email, phone, password_hash, status_id, user_type_id, created_at, updated_at, removed) " +
                        "VALUES (?::uuid, 'Client', 'User', 'client@test.com', '+71234567890', 'hashed', ?::uuid, ?::uuid, NOW(), NOW(), false)",
                UUID.fromString(clientId), statusId, userTypeIdClient
        );
        jdbcTemplate.update(
                "INSERT INTO clients (user_id, preferred_contact_method, newsletter_subscribed) VALUES (?::uuid, 'EMAIL', false)",
                UUID.fromString(clientId)
        );

        jdbcTemplate.update(
                "INSERT INTO users (id, first_name, last_name, email, phone, password_hash, status_id, user_type_id, created_at, updated_at, removed) " +
                        "VALUES (?::uuid, 'Manager', 'User', 'manager@test.com', '+71234567890', 'hashed', ?::uuid, ?::uuid, NOW(), NOW(), false)",
                UUID.fromString(managerId), statusId, userTypeIdManager
        );
        jdbcTemplate.update(
                "INSERT INTO managers (user_id, position_id, max_concurrent_orders, max_concurrent_test_drives, available) " +
                        "VALUES (?::uuid, ?::uuid, 10, 5, true)",
                UUID.fromString(managerId), positionId
        );

        jdbcTemplate.update(
                "INSERT INTO users (id, first_name, last_name, email, phone, password_hash, status_id, user_type_id, created_at, updated_at, removed) " +
                        "VALUES (?::uuid, 'Admin', 'User', 'admin@test.com', '+71234567890', 'hashed', ?::uuid, ?::uuid, NOW(), NOW(), false)",
                UUID.fromString(adminId), statusId, userTypeIdAdmin
        );
        jdbcTemplate.update(
                "INSERT INTO system_admins (user_id, admin_level_id, last_login_at) VALUES (?::uuid, ?::uuid, NOW())",
                UUID.fromString(adminId), adminLevelId
        );

        carId = createCar();
    }

    private String createCar() throws Exception {
        String request = """
                {
                    "brand": "BMW",
                    "model": "X5",
                    "bodyType": "SEDAN",
                    "color": "BLACK",
                    "driveType": "FRONT",
                    "engineFuelType": "PETROL",
                    "enginePower": 249.0,
                    "engineDisplacement": 2.0,
                    "transmissionGears": 8,
                    "transmissionType": "AUTOMATIC",
                    "price": 2500000.00
                }
                """;

        String response = mockMvc.perform(post("/api/admin/cars")
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("id").asText();
    }

    private String createOrderAndSetAwaitingPayment() throws Exception {
        Map<String, Object> orderRequest = new HashMap<>();
        orderRequest.put("carId", carId);
        orderRequest.put("orderType", "IN_STOCK");

        String orderResponse = mockMvc.perform(post("/api/client/orders")
                        .header("X-User-Id", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String orderId = objectMapper.readTree(orderResponse).get("id").asText();

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

        return orderId;
    }

    private String createPayment() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("orderId", orderId);
        request.put("amount", 2500000.0);
        request.put("method", "CARD");

        String response = mockMvc.perform(post("/api/client/payments")
                        .header("X-User-Id", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("id").asText();
    }

    @Test
    void shouldProcessPaymentSuccessfully() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("transactionId", "TXN-12345");
        request.put("success", true);

        mockMvc.perform(post("/api/client/payments/{id}/process", paymentId)
                        .header("X-User-Id", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.transactionId").value("TXN-12345"))
                .andExpect(jsonPath("$.processedAt").isNotEmpty());

        entityManager.flush();
        entityManager.clear();

        String status = jdbcTemplate.queryForObject(
                "SELECT s.name FROM payments p JOIN payment_statuses s ON p.status_id = s.id WHERE p.id = ?::uuid",
                String.class, UUID.fromString(paymentId)
        );
        assertThat(status).isEqualTo("COMPLETED");
    }

    @Test
    void shouldFailProcessAlreadyProcessedPayment() throws Exception {
        Map<String, Object> processRequest = new HashMap<>();
        processRequest.put("transactionId", "TXN-12345");
        processRequest.put("success", true);

        mockMvc.perform(post("/api/client/payments/{id}/process", paymentId)
                        .header("X-User-Id", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(processRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/client/payments/{id}/process", paymentId)
                        .header("X-User-Id", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(processRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldMarkPaymentAsFailedWhenProcessingFails() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("transactionId", "TXN-12345");
        request.put("success", false);
        request.put("paymentDetails", "Insufficient funds");

        mockMvc.perform(post("/api/client/payments/{id}/process", paymentId)
                        .header("X-User-Id", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.failureReason").value("Insufficient funds"));

        entityManager.flush();
        entityManager.clear();

        String status = jdbcTemplate.queryForObject(
                "SELECT s.name FROM payments p JOIN payment_statuses s ON p.status_id = s.id WHERE p.id = ?::uuid",
                String.class, UUID.fromString(paymentId)
        );
        assertThat(status).isEqualTo("FAILED");
    }

    @Test
    void shouldFailProcessPaymentWithInvalidTransactionId() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("success", true);

        mockMvc.perform(post("/api/client/payments/{id}/process", paymentId)
                        .header("X-User-Id", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailProcessPaymentWithNullSuccess() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("transactionId", "TXN-12345");
        request.put("success", null);

        mockMvc.perform(post("/api/client/payments/{id}/process", paymentId)
                        .header("X-User-Id", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}