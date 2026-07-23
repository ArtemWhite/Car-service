package paymentIntegrationTests.paymentUserIntegrationTests;

import dealerShipOrder.BaseIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
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
class PaymentAdminControllerIntegrationTest extends BaseIntegrationTest {

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
    private String carId;
    private String orderId;

    @BeforeEach
    void setUp() throws Exception {
        createReferenceDataIfNotExists();
        createUsers();
        carId = createCarViaApi();
        orderId = createOrderViaApi();
    }

    private void createReferenceDataIfNotExists() {
        if (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_statuses WHERE name = 'ACTIVE'", Integer.class) == 0) {
            jdbcTemplate.update("INSERT INTO user_statuses (id, name, display_name, can_authenticate, created_at, updated_at, removed) VALUES (?::uuid, 'ACTIVE', 'Активен', true, NOW(), NOW(), false)", UUID.randomUUID());
            jdbcTemplate.update("INSERT INTO user_statuses (id, name, display_name, can_authenticate, created_at, updated_at, removed) VALUES (?::uuid, 'INACTIVE', 'Неактивен', false, NOW(), NOW(), false)", UUID.randomUUID());
            jdbcTemplate.update("INSERT INTO user_statuses (id, name, display_name, can_authenticate, created_at, updated_at, removed) VALUES (?::uuid, 'BLOCKED', 'Заблокирован', false, NOW(), NOW(), false)", UUID.randomUUID());
        }

        if (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_types WHERE name = 'CLIENT'", Integer.class) == 0) {
            jdbcTemplate.update("INSERT INTO user_types (id, name, display_name, created_at, updated_at, removed) VALUES (?::uuid, 'CLIENT', 'Клиент', NOW(), NOW(), false)", UUID.randomUUID());
            jdbcTemplate.update("INSERT INTO user_types (id, name, display_name, created_at, updated_at, removed) VALUES (?::uuid, 'MANAGER', 'Менеджер', NOW(), NOW(), false)", UUID.randomUUID());
            jdbcTemplate.update("INSERT INTO user_types (id, name, display_name, created_at, updated_at, removed) VALUES (?::uuid, 'SYSTEM_ADMIN', 'Системный администратор', NOW(), NOW(), false)", UUID.randomUUID());
            jdbcTemplate.update("INSERT INTO user_types (id, name, display_name, created_at, updated_at, removed) VALUES (?::uuid, 'WAREHOUSE_ADMIN', 'Складской администратор', NOW(), NOW(), false)", UUID.randomUUID());
        }

        if (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM admin_levels WHERE name = 'SUPER_ADMIN'", Integer.class) == 0) {
            jdbcTemplate.update("INSERT INTO admin_levels (id, name, display_name, level, created_at, updated_at, removed) VALUES (?::uuid, 'SUPER_ADMIN', 'Супер администратор', 100, NOW(), NOW(), false)", UUID.randomUUID());
            jdbcTemplate.update("INSERT INTO admin_levels (id, name, display_name, level, created_at, updated_at, removed) VALUES (?::uuid, 'ADMIN', 'Администратор', 50, NOW(), NOW(), false)", UUID.randomUUID());
        }

        if (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM manager_positions WHERE name = 'SALES_MANAGER'", Integer.class) == 0) {
            jdbcTemplate.update("INSERT INTO manager_positions (id, name, display_name, max_concurrent_orders, max_concurrent_test_drives, created_at, updated_at, removed) VALUES (?::uuid, 'SALES_MANAGER', 'Менеджер по продажам', 10, 5, NOW(), NOW(), false)", UUID.randomUUID());
        }

        if (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM payment_methods WHERE name = 'CARD'", Integer.class) == 0) {
            jdbcTemplate.update("INSERT INTO payment_methods (id, name, display_name, created_at, updated_at, removed) VALUES (?::uuid, 'CARD', 'Банковская карта', NOW(), NOW(), false)", UUID.randomUUID());
            jdbcTemplate.update("INSERT INTO payment_methods (id, name, display_name, created_at, updated_at, removed) VALUES (?::uuid, 'CASH', 'Наличные', NOW(), NOW(), false)", UUID.randomUUID());
        }

        if (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM payment_statuses WHERE name = 'COMPLETED'", Integer.class) == 0) {
            jdbcTemplate.update("INSERT INTO payment_statuses (id, name, display_name, created_at, updated_at, removed) VALUES (?::uuid, 'COMPLETED', 'Оплачен', NOW(), NOW(), false)", UUID.randomUUID());
            jdbcTemplate.update("INSERT INTO payment_statuses (id, name, display_name, created_at, updated_at, removed) VALUES (?::uuid, 'PENDING', 'Ожидает оплаты', NOW(), NOW(), false)", UUID.randomUUID());
            jdbcTemplate.update("INSERT INTO payment_statuses (id, name, display_name, created_at, updated_at, removed) VALUES (?::uuid, 'PROCESSING', 'Обрабатывается', NOW(), NOW(), false)", UUID.randomUUID());
            jdbcTemplate.update("INSERT INTO payment_statuses (id, name, display_name, created_at, updated_at, removed) VALUES (?::uuid, 'FAILED', 'Ошибка оплаты', NOW(), NOW(), false)", UUID.randomUUID());
            jdbcTemplate.update("INSERT INTO payment_statuses (id, name, display_name, created_at, updated_at, removed) VALUES (?::uuid, 'REFUNDED', 'Возврат', NOW(), NOW(), false)", UUID.randomUUID());
        }

        if (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM order_statuses WHERE name = 'CREATED'", Integer.class) == 0) {
            jdbcTemplate.update("INSERT INTO order_statuses (id, name, display_name, created_at, updated_at, removed) VALUES (?::uuid, 'CREATED', 'Оформлен', NOW(), NOW(), false)", UUID.randomUUID());
            jdbcTemplate.update("INSERT INTO order_statuses (id, name, display_name, created_at, updated_at, removed) VALUES (?::uuid, 'MANAGER_APPROVED', 'Согласован менеджером', NOW(), NOW(), false)", UUID.randomUUID());
            jdbcTemplate.update("INSERT INTO order_statuses (id, name, display_name, created_at, updated_at, removed) VALUES (?::uuid, 'AWAITING_PAYMENT', 'Ожидает оплаты', NOW(), NOW(), false)", UUID.randomUUID());
            jdbcTemplate.update("INSERT INTO order_statuses (id, name, display_name, created_at, updated_at, removed) VALUES (?::uuid, 'PAID', 'Оплачен', NOW(), NOW(), false)", UUID.randomUUID());
            jdbcTemplate.update("INSERT INTO order_statuses (id, name, display_name, created_at, updated_at, removed) VALUES (?::uuid, 'CANCELLED', 'Отменён', NOW(), NOW(), false)", UUID.randomUUID());
            jdbcTemplate.update("INSERT INTO order_statuses (id, name, display_name, created_at, updated_at, removed) VALUES (?::uuid, 'COMPLETED', 'Завершён', NOW(), NOW(), false)", UUID.randomUUID());
            jdbcTemplate.update("INSERT INTO order_statuses (id, name, display_name, created_at, updated_at, removed) VALUES (?::uuid, 'STOCK_CONFIRMED', 'Согласован складом', NOW(), NOW(), false)", UUID.randomUUID());
            jdbcTemplate.update("INSERT INTO order_statuses (id, name, display_name, created_at, updated_at, removed) VALUES (?::uuid, 'READY_FOR_PICKUP', 'Готов к выдаче', NOW(), NOW(), false)", UUID.randomUUID());
        }

        if (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM order_types WHERE name = 'IN_STOCK'", Integer.class) == 0) {
            jdbcTemplate.update("INSERT INTO order_types (id, name, display_name, created_at, updated_at, removed) VALUES (?::uuid, 'IN_STOCK', 'Заказ на автомобиль в наличии', NOW(), NOW(), false)", UUID.randomUUID());
            jdbcTemplate.update("INSERT INTO order_types (id, name, display_name, created_at, updated_at, removed) VALUES (?::uuid, 'CUSTOM', 'Заказ с конфигурацией', NOW(), NOW(), false)", UUID.randomUUID());
        }
    }

    private void createUsers() {
        UUID statusId = jdbcTemplate.queryForObject("SELECT id FROM user_statuses WHERE name = 'ACTIVE'", UUID.class);
        UUID clientTypeId = jdbcTemplate.queryForObject("SELECT id FROM user_types WHERE name = 'CLIENT'", UUID.class);
        UUID managerTypeId = jdbcTemplate.queryForObject("SELECT id FROM user_types WHERE name = 'MANAGER'", UUID.class);
        UUID adminTypeId = jdbcTemplate.queryForObject("SELECT id FROM user_types WHERE name = 'SYSTEM_ADMIN'", UUID.class);
        UUID adminLevelId = jdbcTemplate.queryForObject("SELECT id FROM admin_levels WHERE name = 'SUPER_ADMIN'", UUID.class);
        UUID positionId = jdbcTemplate.queryForObject("SELECT id FROM manager_positions WHERE name = 'SALES_MANAGER'", UUID.class);

        clientId = UUID.randomUUID().toString();
        managerId = UUID.randomUUID().toString();
        adminId = UUID.randomUUID().toString();

        jdbcTemplate.update(
                "INSERT INTO users (id, first_name, last_name, email, phone, password_hash, status_id, user_type_id, created_at, updated_at, removed) " +
                        "VALUES (?::uuid, 'Client', 'User', ?, '+71234567890', 'hashed', ?::uuid, ?::uuid, NOW(), NOW(), false)",
                UUID.fromString(clientId), "client_" + clientId + "@test.com", statusId, clientTypeId
        );
        jdbcTemplate.update(
                "INSERT INTO clients (user_id, preferred_contact_method, newsletter_subscribed) VALUES (?::uuid, 'EMAIL', false)",
                UUID.fromString(clientId)
        );

        jdbcTemplate.update(
                "INSERT INTO users (id, first_name, last_name, email, phone, password_hash, status_id, user_type_id, created_at, updated_at, removed) " +
                        "VALUES (?::uuid, 'Manager', 'User', ?, '+71234567890', 'hashed', ?::uuid, ?::uuid, NOW(), NOW(), false)",
                UUID.fromString(managerId), "manager_" + managerId + "@test.com", statusId, managerTypeId
        );
        jdbcTemplate.update(
                "INSERT INTO managers (user_id, position_id, max_concurrent_orders, max_concurrent_test_drives, available) VALUES (?::uuid, ?::uuid, 10, 5, true)",
                UUID.fromString(managerId), positionId
        );

        jdbcTemplate.update(
                "INSERT INTO users (id, first_name, last_name, email, phone, password_hash, status_id, user_type_id, created_at, updated_at, removed) " +
                        "VALUES (?::uuid, 'Admin', 'User', ?, '+71234567890', 'hashed', ?::uuid, ?::uuid, NOW(), NOW(), false)",
                UUID.fromString(adminId), "admin_" + adminId + "@test.com", statusId, adminTypeId
        );
        jdbcTemplate.update(
                "INSERT INTO system_admins (user_id, admin_level_id, last_login_at) VALUES (?::uuid, ?::uuid, NOW())",
                UUID.fromString(adminId), adminLevelId
        );
    }

    private String createCarViaApi() throws Exception {
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

        String id = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(put("/api/admin/cars/{id}", id)
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"AVAILABLE\"}"))
                .andExpect(status().isOk());

        return id;
    }

    private String createOrderViaApi() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("carId", carId);
        requestBody.put("orderType", "IN_STOCK");

        String response = mockMvc.perform(post("/api/client/orders")
                        .header("X-User-Id", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(post("/api/manager/orders/{id}/assign", id)
                        .header("X-User-Id", managerId))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/admin/orders/{id}", id)
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"AWAITING_PAYMENT\"}"))
                .andExpect(status().isOk());

        return id;
    }

    private String createAndProcessPayment() throws Exception {
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
        processRequest.put("transactionId", "TXN-" + UUID.randomUUID().toString().substring(0, 8));
        processRequest.put("success", true);

        mockMvc.perform(post("/api/client/payments/{id}/process", paymentId)
                        .header("X-User-Id", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(processRequest)))
                .andExpect(status().isOk());

        entityManager.flush();
        entityManager.clear();

        return paymentId;
    }

    @Test
    @Transactional
    @Rollback
    void shouldGetPaymentsByStatus() throws Exception {
        createAndProcessPayment();

        mockMvc.perform(get("/api/admin/payments/status/COMPLETED")
                        .header("X-User-Id", adminId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payments.length()").value(1));
    }

    @Test
    @Transactional
    @Rollback
    void shouldGetEmptyListForStatusWithNoPayments() throws Exception {
        mockMvc.perform(get("/api/admin/payments/status/COMPLETED")
                .header("X-User-Id", adminId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payments.length()").value(0));
    }

    @Test
    @Transactional
    @Rollback
    void shouldGetPaymentsByDateRange() throws Exception {
        createAndProcessPayment();

        String from = LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String to = LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        mockMvc.perform(get("/api/admin/payments/date-range")
                        .param("from", from)
                        .param("to", to)
                .header("X-User-Id", adminId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payments.length()").value(1));
    }

    @Test
    @Transactional
    @Rollback
    void shouldGetEmptyListWhenDateRangeDoesNotMatch() throws Exception {
        createAndProcessPayment();

        String from = LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String to = LocalDateTime.now().plusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        mockMvc.perform(get("/api/admin/payments/date-range")
                        .param("from", from)
                        .param("to", to)
                .header("X-User-Id", adminId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payments.length()").value(0));
    }

    @Test
    @Transactional
    @Rollback
    void shouldRefundPayment() throws Exception {
        String paymentId = createAndProcessPayment();

        Map<String, Object> refundRequest = new HashMap<>();
        refundRequest.put("reason", "Customer request");

        mockMvc.perform(post("/api/admin/payments/{id}/refund", paymentId)
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refundRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REFUNDED"));
    }

    @Test
    @Transactional
    @Rollback
    void shouldFailRefundNonExistentPayment() throws Exception {
        Map<String, Object> refundRequest = new HashMap<>();
        refundRequest.put("reason", "Customer request");

        mockMvc.perform(post("/api/admin/payments/{id}/refund", UUID.randomUUID().toString())
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refundRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    @Rollback
    void shouldFailRefundPendingPayment() throws Exception {
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

        Map<String, Object> refundRequest = new HashMap<>();
        refundRequest.put("reason", "Customer request");

        mockMvc.perform(post("/api/admin/payments/{id}/refund", paymentId)
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refundRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    @Rollback
    void shouldFailRefundWithoutAdminHeader() throws Exception {
        String paymentId = createAndProcessPayment();

        Map<String, Object> refundRequest = new HashMap<>();
        refundRequest.put("reason", "Customer request");

        mockMvc.perform(post("/api/admin/payments/{id}/refund", paymentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refundRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    @Rollback
    void shouldFailRefundWithNonAdminUser() throws Exception {
        String paymentId = createAndProcessPayment();

        Map<String, Object> refundRequest = new HashMap<>();
        refundRequest.put("reason", "Customer request");

        mockMvc.perform(post("/api/admin/payments/{id}/refund", paymentId)
                        .header("X-User-Id", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refundRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    @Rollback
    void shouldFailGetPaymentsByInvalidStatus() throws Exception {
        mockMvc.perform(get("/api/admin/payments/status/INVALID_STATUS"))
                .andExpect(status().isBadRequest());
    }
}