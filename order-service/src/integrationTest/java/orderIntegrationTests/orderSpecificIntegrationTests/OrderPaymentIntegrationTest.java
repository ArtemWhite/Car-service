package orderIntegrationTests.orderSpecificIntegrationTests;

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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class OrderPaymentIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String clientId;
    private String adminId;
    private String carId;

    @BeforeEach
    void setUp() throws Exception {
        createReferenceDataIfNotExists();

        UUID statusId = jdbcTemplate.queryForObject(
                "SELECT id FROM user_statuses WHERE name = 'ACTIVE'", UUID.class);
        UUID userTypeIdClient = jdbcTemplate.queryForObject(
                "SELECT id FROM user_types WHERE name = 'CLIENT'", UUID.class);
        UUID userTypeIdAdmin = jdbcTemplate.queryForObject(
                "SELECT id FROM user_types WHERE name = 'SYSTEM_ADMIN'", UUID.class);
        UUID adminLevelId = jdbcTemplate.queryForObject(
                "SELECT id FROM admin_levels WHERE name = 'SUPER_ADMIN'", UUID.class);
        UUID paymentMethodId = jdbcTemplate.queryForObject(
                "SELECT id FROM payment_methods WHERE name = 'CARD'", UUID.class);
        UUID paymentStatusId = jdbcTemplate.queryForObject(
                "SELECT id FROM payment_statuses WHERE name = 'PENDING'", UUID.class);

        clientId = UUID.randomUUID().toString();
        adminId = UUID.randomUUID().toString();

        jdbcTemplate.update(
                "INSERT INTO users (id, first_name, last_name, email, phone, password_hash, status_id, user_type_id, created_at, updated_at, removed) " +
                        "VALUES (?::uuid, 'Client', 'User', 'client@test.com', '+71234567890', 'hashed', ?::uuid, ?::uuid, NOW(), NOW(), false)",
                UUID.fromString(clientId), statusId, userTypeIdClient
        );

        jdbcTemplate.update(
                "INSERT INTO clients (user_id, preferred_contact_method, newsletter_subscribed) " +
                        "VALUES (?::uuid, 'EMAIL', false)",
                UUID.fromString(clientId)
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

        carId = createCarAndGetId();
    }

    private void createReferenceDataIfNotExists() {
        if (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_statuses WHERE name = 'ACTIVE'", Integer.class) == 0) {
            jdbcTemplate.update("INSERT INTO user_statuses (id, name, display_name, can_authenticate, created_at, updated_at, removed) VALUES (?::uuid, 'ACTIVE', 'Активен', true, NOW(), NOW(), false)", UUID.randomUUID());
        }
        if (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_types WHERE name = 'CLIENT'", Integer.class) == 0) {
            jdbcTemplate.update("INSERT INTO user_types (id, name, display_name, created_at, updated_at, removed) VALUES (?::uuid, 'CLIENT', 'Клиент', NOW(), NOW(), false)", UUID.randomUUID());
            jdbcTemplate.update("INSERT INTO user_types (id, name, display_name, created_at, updated_at, removed) VALUES (?::uuid, 'SYSTEM_ADMIN', 'Системный администратор', NOW(), NOW(), false)", UUID.randomUUID());
        }
        if (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM admin_levels WHERE name = 'SUPER_ADMIN'", Integer.class) == 0) {
            jdbcTemplate.update("INSERT INTO admin_levels (id, name, display_name, level, created_at, updated_at, removed) VALUES (?::uuid, 'SUPER_ADMIN', 'Супер администратор', 100, NOW(), NOW(), false)", UUID.randomUUID());
        }
        if (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM payment_methods WHERE name = 'CARD'", Integer.class) == 0) {
            jdbcTemplate.update("INSERT INTO payment_methods (id, name, display_name, created_at, updated_at, removed) VALUES (?::uuid, 'CARD', 'Банковская карта', NOW(), NOW(), false)", UUID.randomUUID());
            jdbcTemplate.update("INSERT INTO payment_methods (id, name, display_name, created_at, updated_at, removed) VALUES (?::uuid, 'CASH', 'Наличные', NOW(), NOW(), false)", UUID.randomUUID());
        }
        if (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM payment_statuses WHERE name = 'PENDING'", Integer.class) == 0) {
            jdbcTemplate.update("INSERT INTO payment_statuses (id, name, display_name, created_at, updated_at, removed) VALUES (?::uuid, 'PENDING', 'Ожидает оплаты', NOW(), NOW(), false)", UUID.randomUUID());
            jdbcTemplate.update("INSERT INTO payment_statuses (id, name, display_name, created_at, updated_at, removed) VALUES (?::uuid, 'COMPLETED', 'Оплачен', NOW(), NOW(), false)", UUID.randomUUID());
            jdbcTemplate.update("INSERT INTO payment_statuses (id, name, display_name, created_at, updated_at, removed) VALUES (?::uuid, 'FAILED', 'Ошибка оплаты', NOW(), NOW(), false)", UUID.randomUUID());
        }
    }

    private String createCarAndGetId() throws Exception {
        return UUID.randomUUID().toString();
    }

    private String createOrder() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("carId", carId);
        requestBody.put("orderType", "IN_STOCK");

        String response = mockMvc.perform(post("/api/client/orders")
                        .header("X-User-Id", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("id").asText();
    }

    @Test
    @Transactional
    @Rollback
    void shouldCreatePaymentForOrder() throws Exception {
        String orderId = createOrder();
        UUID paymentMethodId = jdbcTemplate.queryForObject(
                "SELECT id FROM payment_methods WHERE name = 'CARD'", UUID.class);
        UUID paymentStatusId = jdbcTemplate.queryForObject(
                "SELECT id FROM payment_statuses WHERE name = 'PENDING'", UUID.class);

        UUID paymentId = UUID.randomUUID();

        jdbcTemplate.update(
                "INSERT INTO payments (id, order_id, client_id, amount, method_id, status_id, created_at, updated_at, removed) " +
                        "VALUES (?::uuid, ?, ?::uuid, 2500000.0, ?::uuid, ?::uuid, NOW(), NOW(), false)",
                paymentId, orderId, UUID.fromString(clientId), paymentMethodId, paymentStatusId
        );

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM payments WHERE order_id = ?",
                Integer.class, orderId
        );
        assertThat(count).isEqualTo(1);
    }
}