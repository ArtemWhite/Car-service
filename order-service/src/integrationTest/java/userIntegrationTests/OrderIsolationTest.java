package userIntegrationTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import dealerShipOrder.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@Transactional
public class OrderIsolationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String client1Id;
    private String client2Id;
    private String carId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM client_orders");
        jdbcTemplate.execute("DELETE FROM clients");
        jdbcTemplate.execute("DELETE FROM users WHERE email LIKE '%@test.com'");

        ensureReferenceData();

        client1Id = UUID.randomUUID().toString();
        createUser(client1Id, "CLIENT", "client1@test.com", "ACTIVE");
        createClient(client1Id);

        client2Id = UUID.randomUUID().toString();
        createUser(client2Id, "CLIENT", "client2@test.com", "ACTIVE");
        createClient(client2Id);

        carId = "dc6f5830-7e0d-43e0-9823-535a9f0dc567";
    }

    private void ensureReferenceData() {
        ensureStatuses();
        ensureTypes();
        ensurePaymentMethods();
        ensurePaymentStatuses();
        ensureOrderStatuses();
        ensureOrderTypes();
    }

    private void ensureStatuses() {
        String[][] statuses = {{"ACTIVE", "Активен", "true"}, {"INACTIVE", "Неактивен", "false"}, {"BLOCKED", "Заблокирован", "false"}};
        for (String[] s : statuses) {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_statuses WHERE name = ?", Integer.class, s[0]);
            if (count != null && count == 0) {
                jdbcTemplate.update("INSERT INTO user_statuses (id, name, display_name, can_authenticate, created_at, updated_at, removed) VALUES (gen_random_uuid(), ?, ?, ?, NOW(), NOW(), false)", s[0], s[1], Boolean.parseBoolean(s[2]));
            }
        }
    }

    private void ensureTypes() {
        for (String t : new String[]{"CLIENT", "MANAGER", "SYSTEM_ADMIN", "WAREHOUSE_ADMIN"}) {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_types WHERE name = ?", Integer.class, t);
            if (count != null && count == 0) {
                jdbcTemplate.update("INSERT INTO user_types (id, name, display_name, created_at, updated_at, removed) VALUES (gen_random_uuid(), ?, ?, NOW(), NOW(), false)", t, t);
            }
        }
    }

    private void ensurePaymentMethods() {
        String[][] methods = {{"CASH", "Наличные"}, {"CARD", "Банковская карта"}, {"ONLINE", "Онлайн-оплата"}, {"INSTALLMENT", "Рассрочка"}};
        for (String[] m : methods) {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM payment_methods WHERE name = ?", Integer.class, m[0]);
            if (count != null && count == 0) {
                jdbcTemplate.update("INSERT INTO payment_methods (id, name, display_name, created_at, updated_at, removed) VALUES (gen_random_uuid(), ?, ?, NOW(), NOW(), false)", m[0], m[1]);
            }
        }
    }

    private void ensurePaymentStatuses() {
        String[][] statuses = {{"PENDING", "Ожидает оплаты"}, {"PROCESSING", "Обрабатывается"}, {"COMPLETED", "Оплачен"}, {"FAILED", "Ошибка оплаты"}, {"REFUNDED", "Возврат"}};
        for (String[] s : statuses) {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM payment_statuses WHERE name = ?", Integer.class, s[0]);
            if (count != null && count == 0) {
                jdbcTemplate.update("INSERT INTO payment_statuses (id, name, display_name, created_at, updated_at, removed) VALUES (gen_random_uuid(), ?, ?, NOW(), NOW(), false)", s[0], s[1]);
            }
        }
    }

    private void ensureOrderStatuses() {
        String[][] statuses = {{"CREATED", "Оформлен"}, {"MANAGER_APPROVED", "Согласован менеджером"}, {"AWAITING_PAYMENT", "Ожидает оплаты"}, {"PAID", "Оплачен"}, {"READY_FOR_PICKUP", "Автомобиль готов к выдаче"}, {"COMPLETED", "Завершён"}, {"CANCELLED", "Отменён"}, {"STOCK_CONFIRMED", "Согласован складом"}, {"AWAITING_DELIVERY", "Ожидает доставки"}};
        for (String[] s : statuses) {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM order_statuses WHERE name = ?", Integer.class, s[0]);
            if (count != null && count == 0) {
                jdbcTemplate.update("INSERT INTO order_statuses (id, name, display_name, created_at, updated_at, removed) VALUES (gen_random_uuid(), ?, ?, NOW(), NOW(), false)", s[0], s[1]);
            }
        }
    }

    private void ensureOrderTypes() {
        String[][] types = {{"IN_STOCK", "Заказ на автомобиль в наличии"}, {"CUSTOM", "Заказ на автомобиль с конфигурацией"}};
        for (String[] t : types) {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM order_types WHERE name = ?", Integer.class, t[0]);
            if (count != null && count == 0) {
                jdbcTemplate.update("INSERT INTO order_types (id, name, display_name, created_at, updated_at, removed) VALUES (gen_random_uuid(), ?, ?, NOW(), NOW(), false)", t[0], t[1]);
            }
        }
    }

    private void createUser(String id, String userType, String email, String status) {
        jdbcTemplate.update(
                "INSERT INTO users (id, first_name, last_name, middle_name, email, phone, password_hash, status_id, user_type_id, last_active_at, last_password_change_at, created_at, updated_at, removed) " +
                        "VALUES (?::uuid, ?, ?, ?, ?, ?, ?, (SELECT id FROM user_statuses WHERE name = ?), (SELECT id FROM user_types WHERE name = ?), NOW(), NOW(), NOW(), NOW(), false)",
                UUID.fromString(id), "Test", "User", "Testovich", email, "+71234567890",
                "hashed_" + id.substring(0, 8), status, userType
        );
    }

    private void createClient(String id) {
        jdbcTemplate.update(
                "INSERT INTO clients (user_id, preferred_contact_method, newsletter_subscribed) VALUES (?::uuid, 'EMAIL', false)",
                UUID.fromString(id)
        );
    }

    @Test
    void testClientSeesOnlyHisOwnOrders() throws Exception {
        String client1OrderId = createOrder(client1Id, carId);
        String client2OrderId = createOrder(client2Id, carId);

        mockMvc.perform(get("/api/client/orders/my")
                        .header("X-User-Id", client1Id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders[*].id").value(org.hamcrest.Matchers.hasItem(client1OrderId)))
                .andExpect(jsonPath("$.orders[*].id").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.hasItem(client2OrderId))));

        mockMvc.perform(get("/api/client/orders/my")
                        .header("X-User-Id", client2Id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders[*].id").value(org.hamcrest.Matchers.hasItem(client2OrderId)))
                .andExpect(jsonPath("$.orders[*].id").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.hasItem(client1OrderId))));
    }

    @Test
    void testClientCannotAccessOtherClientsOrderById() throws Exception {
        String client1OrderId = createOrder(client1Id, carId);

        mockMvc.perform(get("/api/client/orders/{id}", client1OrderId)
                        .header("X-User-Id", client2Id))
                .andExpect(status().isForbidden());
    }

    @Test
    void testClientCannotCancelOtherClientsOrder() throws Exception {
        String client1OrderId = createOrder(client1Id, carId);

        mockMvc.perform(post("/api/client/orders/{id}/cancel", client1OrderId)
                        .header("X-User-Id", client2Id)
                        .param("reason", "Не хочу"))
                .andExpect(status().isForbidden());
    }

    private String createOrder(String userId, String carId) throws Exception {
        String response = mockMvc.perform(post("/api/client/orders")
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"carId\": \"" + carId + "\", \"deliveryAddress\": \"Test Address\", \"deliveryType\": \"DELIVERY\", \"paymentMethod\": \"CARD\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        int start = response.indexOf("\"id\":\"") + 6;
        int end = response.indexOf("\"", start);
        return response.substring(start, end);
    }
}
