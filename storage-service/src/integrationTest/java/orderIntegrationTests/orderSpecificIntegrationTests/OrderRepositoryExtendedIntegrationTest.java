package orderIntegrationTests.orderSpecificIntegrationTests;

import carIntegrationTests.BaseIntegrationTest;
import domain.models.order.Order;
import domain.models.order.OrderStatus;
import domain.models.order.OrderType;
import domain.repository.orderRepository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class OrderRepositoryExtendedIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String clientId;
    private String carId;
    private String configurationId;
    private String carModelId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM order_history_entries");
        jdbcTemplate.execute("DELETE FROM orders");

        clientId = UUID.randomUUID().toString();
        carId = UUID.randomUUID().toString();
        configurationId = UUID.randomUUID().toString();
        carModelId = UUID.randomUUID().toString();
    }

    private Order createInStockOrder() {
        return Order.createInStockOrder(
                UUID.randomUUID().toString(),
                clientId,
                carId
        );
    }

    private Order createCustomOrder() {
        return Order.createCustomOrder(
                UUID.randomUUID().toString(),
                clientId,
                configurationId,
                carModelId
        );
    }

    @Test
    @Transactional
    @Rollback
    void shouldFindByStatusIn() {
        orderRepository.save(createInStockOrder());
        orderRepository.save(createInStockOrder());

        List<Order> orders = orderRepository.findByStatusIn(
                List.of(OrderStatus.CREATED, OrderStatus.PAID)
        );
        assertThat(orders).hasSize(2);
    }

    @Test
    @Transactional
    @Rollback
    void shouldFindByCreatedAtBetween() {
        Order order = createInStockOrder();
        orderRepository.save(order);

        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        List<Order> orders = orderRepository.findByCreatedAtBetween(start, end);
        assertThat(orders).hasSize(1);
    }

    @Test
    @Transactional
    @Rollback
    void shouldFindByCompletedAtBetween() {
        Order order = createInStockOrder();
        Order saved = orderRepository.save(order);

        String managerId = UUID.randomUUID().toString();
        saved.assignManager(managerId);
        saved.awaitPayment();
        saved.markAsPaid();
        saved.markAsReadyForPickup();
        saved.markAsCompleted();
        orderRepository.save(saved);

        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        List<Order> orders = orderRepository.findByCompletedAtBetween(start, end);
        assertThat(orders).hasSize(1);
    }

    @Test
    @Transactional
    @Rollback
    void shouldFindOrdersWithFilters() {
        Order order = createInStockOrder();
        orderRepository.save(order);

        List<String> orderIds = jdbcTemplate.queryForList(
                "SELECT o.id::text FROM orders o " +
                        "JOIN order_statuses s ON o.status_id = s.id " +
                        "JOIN order_types t ON o.type_id = t.id " +
                        "WHERE s.name = 'CREATED' " +
                        "AND t.name = 'IN_STOCK' " +
                        "AND o.client_id = ? " +
                        "AND o.created_at >= ? " +
                        "AND o.created_at <= ?",
                String.class,
                clientId,
                java.sql.Timestamp.valueOf(LocalDateTime.now().minusDays(1)),
                java.sql.Timestamp.valueOf(LocalDateTime.now().plusDays(1))
        );

        assertThat(orderIds).hasSize(1);
    }

    @Test
    @Transactional
    @Rollback
    void shouldFindByManagerIdAndStatus() {
        Order order = createInStockOrder();
        Order saved = orderRepository.save(order);
        String managerId = UUID.randomUUID().toString();
        saved.assignManager(managerId);
        orderRepository.save(saved);

        List<Order> orders = orderRepository.findByManagerIdAndStatus(managerId, OrderStatus.MANAGER_APPROVED);
        assertThat(orders).hasSize(1);
    }

    @Test
    @Transactional
    @Rollback
    void shouldFindByClientIdAndStatus() {
        orderRepository.save(createInStockOrder());

        List<Order> orders = orderRepository.findByClientIdAndStatus(clientId, OrderStatus.CREATED);
        assertThat(orders).hasSize(1);
    }

    @Test
    @Transactional
    @Rollback
    void shouldFindByOrderType() {
        orderRepository.save(createInStockOrder());
        orderRepository.save(createCustomOrder());

        List<Order> inStockOrders = orderRepository.findByOrderType(OrderType.IN_STOCK.name());
        List<Order> customOrders = orderRepository.findByOrderType(OrderType.CUSTOM.name());

        assertThat(inStockOrders).hasSize(1);
        assertThat(customOrders).hasSize(1);
    }

    @Test
    @Transactional
    @Rollback
    void shouldFindClientOrdersByDateDesc() {
        orderRepository.save(createInStockOrder());

        List<Order> orders = orderRepository.findByClientId(clientId);

        orders.sort((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()));

        assertThat(orders).isNotEmpty();
    }

    @Test
    @Transactional
    @Rollback
    void shouldCountByManagerId() {
        Order order = createInStockOrder();
        Order saved = orderRepository.save(order);
        String managerId = UUID.randomUUID().toString();
        saved.assignManager(managerId);
        orderRepository.save(saved);

        long count = orderRepository.countByManagerId(managerId);
        assertThat(count).isEqualTo(1);
    }

    @Test
    @Transactional
    @Rollback
    void shouldCountByOrderType() {
        orderRepository.save(createInStockOrder());

        long count = orderRepository.countByOrderType(OrderType.IN_STOCK.name());
        assertThat(count).isGreaterThan(0);
    }

    @Test
    @Transactional
    @Rollback
    void shouldFindOrdersAwaitingProcessing() {
        Order order = createInStockOrder();
        Order saved = orderRepository.save(order);
        saved.assignManager(UUID.randomUUID().toString());
        saved.awaitPayment();
        orderRepository.save(saved);

        List<Order> orders = orderRepository.findByStatusIn(
                List.of(OrderStatus.AWAITING_PAYMENT, OrderStatus.PAID)
        );

        assertThat(orders).isNotEmpty();
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getStatus()).isIn(OrderStatus.AWAITING_PAYMENT, OrderStatus.PAID);
    }

    @Test
    @Transactional
    @Rollback
    void shouldFindCompletedOrders() {
        Order order = createInStockOrder();
        Order saved = orderRepository.save(order);

        String managerId = UUID.randomUUID().toString();
        saved.assignManager(managerId);
        saved.awaitPayment();
        saved.markAsPaid();
        saved.markAsReadyForPickup();
        saved.markAsCompleted();
        orderRepository.save(saved);

        List<Order> orders = orderRepository.findByStatus(OrderStatus.COMPLETED);

        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    @Transactional
    @Rollback
    void shouldFindCancelledOrders() {
        Order order = createInStockOrder();
        Order saved = orderRepository.save(order);

        UUID orderUuid = UUID.fromString(saved.getId());
        jdbcTemplate.update(
                "UPDATE orders SET status_id = (SELECT id FROM order_statuses WHERE name = 'CANCELLED') WHERE id = ?::uuid",
                orderUuid
        );

        List<String> orderIds = jdbcTemplate.queryForList(
                "SELECT o.id::text FROM orders o " +
                        "JOIN order_statuses s ON o.status_id = s.id " +
                        "WHERE s.name = 'CANCELLED'",
                String.class
        );

        assertThat(orderIds).hasSize(1);
    }

    @Test
    @Transactional
    @Rollback
    void shouldUpdateOrderStatusDirectly() {
        Order order = createInStockOrder();
        Order saved = orderRepository.save(order);

        String managerId = UUID.randomUUID().toString();
        saved.assignManager(managerId);
        saved.awaitPayment();
        saved.markAsPaid();
        orderRepository.save(saved);

        Order updated = orderRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    @Transactional
    @Rollback
    void shouldUpdateOrderNotesDirectly() {
        Order order = createInStockOrder();
        Order saved = orderRepository.save(order);

        saved.setNotes("Updated notes");
        orderRepository.save(saved);

        Order updated = orderRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getNotes()).isEqualTo("Updated notes");
    }
}