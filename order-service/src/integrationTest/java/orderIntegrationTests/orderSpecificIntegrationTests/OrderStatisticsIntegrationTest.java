package orderIntegrationTests.orderSpecificIntegrationTests;

import dealerShipOrder.BaseIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import dealerShipOrder.domain.models.order.OrderStatus;
import dealerShipOrder.domain.repository.orderRepository.OrderRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class OrderStatisticsIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private OrderRepository orderRepository;

    private String clientId;
    private String adminId;
    private String managerId;
    private String carId;

    @BeforeEach
    void setUp() throws Exception {
        createReferenceDataIfNotExists();

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

        clientId = UUID.randomUUID().toString();
        managerId = UUID.randomUUID().toString();
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

        carId = createCarAndGetId();
    }

    private void createReferenceDataIfNotExists() {
        if (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_statuses WHERE name = 'ACTIVE'", Integer.class) == 0) {
            jdbcTemplate.update("INSERT INTO user_statuses (id, name, display_name, can_authenticate, created_at, updated_at, removed) VALUES (?::uuid, 'ACTIVE', 'Активен', true, NOW(), NOW(), false)", UUID.randomUUID());
        }
        if (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_types WHERE name = 'CLIENT'", Integer.class) == 0) {
            jdbcTemplate.update("INSERT INTO user_types (id, name, display_name, created_at, updated_at, removed) VALUES (?::uuid, 'CLIENT', 'Клиент', NOW(), NOW(), false)", UUID.randomUUID());
            jdbcTemplate.update("INSERT INTO user_types (id, name, display_name, created_at, updated_at, removed) VALUES (?::uuid, 'MANAGER', 'Менеджер', NOW(), NOW(), false)", UUID.randomUUID());
            jdbcTemplate.update("INSERT INTO user_types (id, name, display_name, created_at, updated_at, removed) VALUES (?::uuid, 'SYSTEM_ADMIN', 'Системный администратор', NOW(), NOW(), false)", UUID.randomUUID());
        }
        if (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM manager_positions WHERE name = 'SALES_MANAGER'", Integer.class) == 0) {
            jdbcTemplate.update("INSERT INTO manager_positions (id, name, display_name, max_concurrent_orders, max_concurrent_test_drives, created_at, updated_at, removed) VALUES (?::uuid, 'SALES_MANAGER', 'Менеджер по продажам', 10, 5, NOW(), NOW(), false)", UUID.randomUUID());
        }
        if (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM admin_levels WHERE name = 'SUPER_ADMIN'", Integer.class) == 0) {
            jdbcTemplate.update("INSERT INTO admin_levels (id, name, display_name, level, created_at, updated_at, removed) VALUES (?::uuid, 'SUPER_ADMIN', 'Супер администратор', 100, NOW(), NOW(), false)", UUID.randomUUID());
        }
    }

    private String createCarAndGetId() throws Exception {
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

        String updateRequest = "{\"status\": \"AVAILABLE\"}";
        mockMvc.perform(put("/api/admin/cars/{id}", id)
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().isOk());

        return id;
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
    void shouldCountOrdersByStatus() throws Exception {
        createOrder();

        long createdCount = orderRepository.countByStatus(OrderStatus.CREATED);
        assertThat(createdCount).isGreaterThan(0);
    }

    @Test
    @Transactional
    @Rollback
    void shouldFindActiveOrders() throws Exception {
        createOrder();

        var activeOrders = orderRepository.findActiveOrders();
        assertThat(activeOrders).isNotEmpty();
    }

    @Test
    @Transactional
    @Rollback
    void shouldFindPendingOrders() throws Exception {
        createOrder();

        mockMvc.perform(get("/api/manager/orders/pending")
                        .header("X-User-Id", managerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders.length()").value(1));
    }

    @Test
    @Transactional
    @Rollback
    void shouldFindUnassignedOrders() throws Exception {
        createOrder();

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM orders WHERE manager_id IS NULL AND status_id = " +
                        "(SELECT id FROM order_statuses WHERE name = 'CREATED')",
                Integer.class
        );
        assertThat(count).isGreaterThan(0);
    }

    @Test
    @Transactional
    @Rollback
    void shouldCountOrdersByClient() throws Exception {
        createOrder();

        long count = orderRepository.countByClientId(clientId);
        assertThat(count).isEqualTo(1);
    }

    @Test
    @Transactional
    @Rollback
    void shouldGetAverageCompletionTime() throws Exception {
        String orderId = createOrder();

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

        updateRequest.put("status", "PAID");
        mockMvc.perform(put("/api/admin/orders/{id}", orderId)
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        updateRequest.put("status", "READY_FOR_PICKUP");
        mockMvc.perform(put("/api/admin/orders/{id}", orderId)
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        updateRequest.put("status", "COMPLETED");
        mockMvc.perform(put("/api/admin/orders/{id}", orderId)
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        entityManager.flush();
        entityManager.clear();

        Double avgTime = jdbcTemplate.queryForObject(
                "SELECT AVG(EXTRACT(EPOCH FROM (completed_at - created_at))) FROM orders WHERE status_id = " +
                        "(SELECT id FROM order_statuses WHERE name = 'COMPLETED')",
                Double.class
        );
        assertThat(avgTime).isNotNull();
    }

    @Test
    @Transactional
    @Rollback
    void shouldCountOrdersByDay() throws Exception {
        createOrder();

        String today = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE);
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM orders WHERE DATE(created_at) = DATE(?)",
                Integer.class, LocalDateTime.now()
        );
        assertThat(count).isGreaterThan(0);
    }

    @Test
    @Transactional
    @Rollback
    void shouldGetOrdersByStatusDistribution() throws Exception {
        createOrder();

        var distribution = jdbcTemplate.queryForList(
                "SELECT status_id, COUNT(*) FROM orders GROUP BY status_id"
        );
        assertThat(distribution).isNotEmpty();
    }
}