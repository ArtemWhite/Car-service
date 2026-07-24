package orderIntegrationTests.orderReferencesIntegrationTests;

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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class OrderFilterIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private dealerShipOrder.domain.repository.orderRepository.OrderRepository orderRepository;

    @Autowired
    private dealerShipOrder.application.mapper.OrderMapper orderMapper;

    private String clientId;
    private String adminId;
    private String carId;

    @BeforeEach
    void setUp() throws Exception {
        jdbcTemplate.execute("DELETE FROM order_history_entries");
        jdbcTemplate.execute("DELETE FROM orders");
        jdbcTemplate.execute("DELETE FROM client_orders");
        jdbcTemplate.execute("DELETE FROM manager_orders");
        jdbcTemplate.execute("DELETE FROM users WHERE email LIKE '%@test.com'");

        createReferenceDataIfNotExists();

        UUID statusId = jdbcTemplate.queryForObject(
                "SELECT id FROM user_statuses WHERE name = 'ACTIVE'", UUID.class);
        UUID userTypeIdClient = jdbcTemplate.queryForObject(
                "SELECT id FROM user_types WHERE name = 'CLIENT'", UUID.class);
        UUID userTypeIdAdmin = jdbcTemplate.queryForObject(
                "SELECT id FROM user_types WHERE name = 'SYSTEM_ADMIN'", UUID.class);
        UUID adminLevelId = jdbcTemplate.queryForObject(
                "SELECT id FROM admin_levels WHERE name = 'SUPER_ADMIN'", UUID.class);

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

        carId = UUID.randomUUID().toString();
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
    void testRepositoryDirectly() throws Exception {
        createOrder();

        List<dealerShipOrder.domain.models.order.Order> orders = orderRepository.findAll();
        System.out.println("=== REPOSITORY RETURNED: " + orders.size() + " orders ===");

        for (dealerShipOrder.domain.models.order.Order o : orders) {
            System.out.println("Order ID: " + o.getId());
            System.out.println("  - status: " + (o.getStatus() != null ? o.getStatus().name() : "NULL"));
            System.out.println("  - type: " + (o.getType() != null ? o.getType().name() : "NULL"));
            System.out.println("  - clientId: " + o.getClientId());
            System.out.println("  - carId: " + o.getCarId());
        }
    }

    @Test
    @Transactional
    @Rollback
    void testMapperDirectly() throws Exception {
        createOrder();

        List<dealerShipOrder.domain.models.order.Order> orders = orderRepository.findAll();
        System.out.println("=== TESTING MAPPER ===");

        for (dealerShipOrder.domain.models.order.Order o : orders) {
            try {
                dealerShipOrder.application.dtos.response.orderResponse.OrderResponse resp = orderMapper.toResponse(o);
                System.out.println("Mapper SUCCESS for order " + o.getId());
            } catch (Exception e) {
                System.out.println("Mapper FAILED for order " + o.getId() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Test
    @Transactional
    @Rollback
    void testDatabaseDirectly() throws Exception {
        createOrder();

        List<Map<String, Object>> results = jdbcTemplate.queryForList(
                "SELECT o.id, o.client_id, s.name as status_name, t.name as type_name " +
                        "FROM orders o " +
                        "LEFT JOIN order_statuses s ON o.status_id = s.id " +
                        "LEFT JOIN order_types t ON o.type_id = t.id"
        );

        System.out.println("=== DATABASE ROWS: " + results.size() + " ===");
        for (Map<String, Object> row : results) {
            System.out.println("  id: " + row.get("id"));
            System.out.println("  client_id: " + row.get("client_id"));
            System.out.println("  status_name: " + row.get("status_name"));
            System.out.println("  type_name: " + row.get("type_name"));
        }
    }

    @Test
    @Transactional
    @Rollback
    void shouldFilterOrdersByClientId() throws Exception {
        createOrder();

        mockMvc.perform(get("/api/orders")
                        .param("clientId", clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders.length()").value(1));
    }

    @Test
    @Transactional
    @Rollback
    void shouldFilterOrdersByStatus() throws Exception {
        createOrder();

        mockMvc.perform(get("/api/orders")
                        .param("status", "CREATED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders.length()").value(1));
    }

    @Test
    @Transactional
    @Rollback
    void shouldFilterOrdersByOrderType() throws Exception {
        createOrder();

        mockMvc.perform(get("/api/orders")
                        .param("orderType", "IN_STOCK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders.length()").value(1));
    }

    @Test
    @Transactional
    @Rollback
    void shouldFilterOrdersByDateRange() throws Exception {
        createOrder();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

        String dateFrom = LocalDateTime.now().minusDays(1)
                .withNano(0)
                .format(formatter);
        String dateTo = LocalDateTime.now().plusDays(1)
                .withNano(0)
                .format(formatter);

        mockMvc.perform(get("/api/orders")
                        .param("dateFrom", dateFrom)
                        .param("dateTo", dateTo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders.length()").value(1));
    }

    @Test
    @Transactional
    @Rollback
    void shouldFilterOrdersByMultipleCriteria() throws Exception {
        createOrder();

        mockMvc.perform(get("/api/orders")
                        .param("clientId", clientId)
                        .param("status", "CREATED")
                        .param("orderType", "IN_STOCK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders.length()").value(1));
    }

    @Test
    @Transactional
    @Rollback
    void shouldReturnEmptyListWhenNoOrdersMatchFilters() throws Exception {
        createOrder();

        mockMvc.perform(get("/api/orders")
                        .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders.length()").value(0));
    }

    @Test
    @Transactional
    @Rollback
    void shouldFilterOrdersWithPagination() throws Exception {
        createOrder();

        mockMvc.perform(get("/api/orders")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders.length()").value(1));
    }

    @Test
    @Transactional
    @Rollback
    void shouldSortOrdersByCreatedAtDesc() throws Exception {
        createOrder();

        mockMvc.perform(get("/api/orders")
                        .param("sortBy", "createdAt")
                        .param("sortDirection", "DESC"))
                .andExpect(status().isOk());
    }
}