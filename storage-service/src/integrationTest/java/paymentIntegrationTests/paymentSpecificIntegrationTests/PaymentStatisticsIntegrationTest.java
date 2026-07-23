package paymentIntegrationTests.paymentSpecificIntegrationTests;

import carIntegrationTests.BaseIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
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
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@Transactional
class PaymentStatisticsIntegrationTest extends BaseIntegrationTest {

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

    @BeforeEach
    void setUp() throws Exception {
        createReferenceData();
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

    private String createCar(double price) throws Exception {
        String request = getString(price);

        String response = mockMvc.perform(post("/api/admin/cars")
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("id").asText();
    }

    @NotNull
    private static String getString(double price) {
        String priceStr = String.format(Locale.US, "%.2f", price);

        String request = String.format(Locale.US, """
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
                    "price": %s
                }
                """, priceStr);
        return request;
    }

    private String createOrderAndSetAwaitingPayment(String clientId, String carId) throws Exception {
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

    private String createCompletedPayment(String clientId, double amount) throws Exception {
        String carId = createCar(amount);
        String orderId = createOrderAndSetAwaitingPayment(clientId, carId);

        Map<String, Object> createRequest = new HashMap<>();
        createRequest.put("orderId", orderId);
        createRequest.put("amount", amount);
        createRequest.put("method", "CARD");

        String response = mockMvc.perform(post("/api/client/payments")
                        .header("X-User-Id", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String paymentId = objectMapper.readTree(response).get("id").asText();

        Map<String, Object> processRequest = new HashMap<>();
        processRequest.put("transactionId", "TXN-" + UUID.randomUUID().toString().substring(0, 8));
        processRequest.put("success", true);

        mockMvc.perform(post("/api/client/payments/{id}/process", paymentId)
                        .header("X-User-Id", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(processRequest)))
                .andExpect(status().isOk());

        return paymentId;
    }

    @Test
    void shouldGetTotalCompletedAmount() throws Exception {
        createCompletedPayment(clientId, 2500000.0);
        createCompletedPayment(clientId, 1500000.0);

        entityManager.flush();
        entityManager.clear();

        Double totalAmount = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(p.amount), 0) FROM payments p JOIN payment_statuses s ON p.status_id = s.id WHERE s.name = 'COMPLETED' AND p.removed = false",
                Double.class
        );

        assertThat(totalAmount).isEqualTo(4000000.0);
    }

    @Test
    void shouldGetTotalCompletedAmountByDateRange() throws Exception {
        createCompletedPayment(clientId, 2500000.0);

        String from = LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ISO_DATE_TIME);
        String to = LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_DATE_TIME);

        entityManager.flush();
        entityManager.clear();

        Double totalAmount = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(p.amount), 0) FROM payments p JOIN payment_statuses s ON p.status_id = s.id " +
                        "WHERE s.name = 'COMPLETED' AND p.created_at BETWEEN ?::timestamp AND ?::timestamp AND p.removed = false",
                Double.class, from, to
        );

        assertThat(totalAmount).isEqualTo(2500000.0);
    }

    @Test
    void shouldGetDailyPaymentStats() throws Exception {
        createCompletedPayment(clientId, 2500000.0);

        String from = LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ISO_DATE_TIME);
        String to = LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_DATE_TIME);

        entityManager.flush();
        entityManager.clear();

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM payments p JOIN payment_statuses s ON p.status_id = s.id " +
                        "WHERE s.name = 'COMPLETED' AND p.created_at BETWEEN ?::timestamp AND ?::timestamp AND p.removed = false",
                Integer.class, from, to
        );

        assertThat(count).isEqualTo(1);
    }

    @Test
    void shouldGetPaymentStatsByMethod() throws Exception {
        createCompletedPayment(clientId, 2500000.0);

        entityManager.flush();
        entityManager.clear();

        Integer cardCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM payments p JOIN payment_methods m ON p.method_id = m.id " +
                        "WHERE m.name = 'CARD' AND p.removed = false",
                Integer.class
        );

        assertThat(cardCount).isEqualTo(1);
    }

    @Test
    void shouldGetTotalSpentByClient() throws Exception {
        createCompletedPayment(clientId, 2500000.0);

        entityManager.flush();
        entityManager.clear();

        Double totalSpent = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(p.amount), 0) FROM payments p JOIN payment_statuses s ON p.status_id = s.id " +
                        "WHERE s.name = 'COMPLETED' AND p.client_id = ? AND p.removed = false",
                Double.class, clientId
        );

        assertThat(totalSpent).isEqualTo(2500000.0);
    }

    @Test
    void shouldReturnZeroWhenNoCompletedPayments() throws Exception {
        entityManager.flush();
        entityManager.clear();

        Double totalAmount = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(p.amount), 0) FROM payments p JOIN payment_statuses s ON p.status_id = s.id " +
                        "WHERE s.name = 'COMPLETED' AND p.removed = false",
                Double.class
        );

        assertThat(totalAmount).isEqualTo(0.0);
    }
}