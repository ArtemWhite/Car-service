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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@Transactional
class PaymentSearchIntegrationTest extends BaseIntegrationTest {

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

    @BeforeEach
    void setUp() throws Exception {
        createReferenceData();
        paymentId = createCompletedPayment();
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
    }

    private String createCar() throws Exception {
        return UUID.randomUUID().toString();
    }

    private String createOrderAndSetAwaitingPayment(String carId) throws Exception {
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

    private String createOrderWithoutPayment() throws Exception {
        String carId = createCar();

        Map<String, Object> orderRequest = new HashMap<>();
        orderRequest.put("carId", carId);
        orderRequest.put("orderType", "IN_STOCK");

        String orderResponse = mockMvc.perform(post("/api/client/orders")
                        .header("X-User-Id", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(orderResponse).get("id").asText();
    }

    private String createCompletedPayment() throws Exception {
        String carId = createCar();
        String orderId = createOrderAndSetAwaitingPayment(carId);
        this.orderId = orderId;

        Map<String, Object> createRequest = new HashMap<>();
        createRequest.put("orderId", orderId);
        createRequest.put("amount", 2500000.0);
        createRequest.put("method", "CARD");

        String response = mockMvc.perform(post("/api/client/payments")
                        .header("X-User-Id", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String paymentId = objectMapper.readTree(response).get("id").asText();

        Map<String, Object> processRequest = new HashMap<>();
        processRequest.put("transactionId", "TXN-12345");
        processRequest.put("success", true);

        mockMvc.perform(post("/api/client/payments/{id}/process", paymentId)
                        .header("X-User-Id", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(processRequest)))
                .andExpect(status().isOk());

        return paymentId;
    }

    @Test
    void shouldFindPaymentById() throws Exception {
        mockMvc.perform(get("/api/payments/{id}", paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(paymentId))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void shouldReturnEmptyWhenPaymentNotFound() throws Exception {
        mockMvc.perform(get("/api/payments/{id}", UUID.randomUUID().toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFindPaymentsByClientId() throws Exception {
        mockMvc.perform(get("/api/client/payments/my")
                        .header("X-User-Id", clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payments.length()").value(1))
                .andExpect(jsonPath("$.totalCount").value(1));
    }

    @Test
    void shouldFindPaymentsByOrderId() throws Exception {
        mockMvc.perform(get("/api/payments/order/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payments.length()").value(1));
    }

    @Test
    void shouldReturnEmptyListWhenOrderHasNoPayments() throws Exception {
        String newOrderId = createOrderWithoutPayment();

        mockMvc.perform(get("/api/payments/order/{orderId}", newOrderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payments.length()").value(0));
    }

    @Test
    void shouldFindPaymentsByStatus() throws Exception {
        mockMvc.perform(get("/api/admin/payments/status/COMPLETED")
                        .header("X-User-Id", adminId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payments.length()").value(1));
    }

    @Test
    void shouldFailFindPaymentsByInvalidStatus() throws Exception {
        mockMvc.perform(get("/api/admin/payments/status/INVALID_STATUS")
                        .header("X-User-Id", adminId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFindPaymentsByDateRange() throws Exception {
        String from = LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ISO_DATE_TIME);
        String to = LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_DATE_TIME);

        mockMvc.perform(get("/api/admin/payments/date-range")
                        .header("X-User-Id", adminId)
                        .param("from", from)
                        .param("to", to))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payments.length()").value(1));
    }

    @Test
    void shouldReturnEmptyListWhenDateRangeHasNoPayments() throws Exception {
        String from = LocalDateTime.now().minusDays(10).format(DateTimeFormatter.ISO_DATE_TIME);
        String to = LocalDateTime.now().minusDays(5).format(DateTimeFormatter.ISO_DATE_TIME);

        mockMvc.perform(get("/api/admin/payments/date-range")
                        .header("X-User-Id", adminId)
                        .param("from", from)
                        .param("to", to))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payments.length()").value(0));
    }

    @Test
    void shouldFailWhenStartDateAfterEndDate() throws Exception {
        String from = LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_DATE_TIME);
        String to = LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ISO_DATE_TIME);

        mockMvc.perform(get("/api/admin/payments/date-range")
                        .header("X-User-Id", adminId)
                        .param("from", from)
                        .param("to", to))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailWhenDateRangeHasInvalidFormat() throws Exception {
        String from = "invalid-date";
        String to = "invalid-date";

        mockMvc.perform(get("/api/admin/payments/date-range")
                        .header("X-User-Id", adminId)
                        .param("from", from)
                        .param("to", to))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFindPaymentsByClientIdAndStatus() throws Exception {
        mockMvc.perform(get("/api/client/payments/my")
                        .header("X-User-Id", clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payments[0].status").value("COMPLETED"));
    }
}