package orderIntegrationTests.orderReferencesIntegrationTests;

import dealerShipOrder.BaseIntegrationTest;
import dealerShipOrder.domain.models.order.Order;
import dealerShipOrder.domain.models.order.OrderHistoryEntry;
import dealerShipOrder.domain.repository.orderRepository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class OrderHistoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String clientId;
    private String carId;
    private String managerId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM order_history_entries");
        jdbcTemplate.execute("DELETE FROM orders");

        clientId = UUID.randomUUID().toString();
        carId = UUID.randomUUID().toString();
        managerId = UUID.randomUUID().toString();
    }

    private Order createOrder() {
        return Order.createInStockOrder(
                UUID.randomUUID().toString(),
                clientId,
                carId
        );
    }

    @Test
    @Transactional
    @Rollback
    void shouldCreateHistoryEntryOnOrderCreation() {
        Order order = createOrder();
        Order saved = orderRepository.save(order);

        assertThat(saved.getHistory()).isNotEmpty();
        assertThat(saved.getHistory().get(0).getAction()).isEqualTo("ORDER_CREATED");
        assertThat(saved.getHistory().get(0).getDescription()).isEqualTo("Заказ создан");
    }

    @Test
    @Transactional
    @Rollback
    void shouldAddHistoryEntryOnManagerAssignment() {
        Order order = createOrder();
        order.assignManager(managerId);
        Order saved = orderRepository.save(order);

        assertThat(saved.getHistory()).hasSize(2);
        assertThat(saved.getHistory().get(1).getAction()).isEqualTo("MANAGER_ASSIGNED");
        assertThat(saved.getHistory().get(1).getDescription()).contains(managerId);
    }

    @Test
    @Transactional
    @Rollback
    void shouldAddHistoryEntryOnStockConfirmation() {
        Order order = Order.createCustomOrder(
                UUID.randomUUID().toString(),
                clientId,
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString()
        );

        order.confirmByStock();
        Order saved = orderRepository.save(order);

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM order_history_entries WHERE order_id = ?::uuid",
                Integer.class,
                UUID.fromString(saved.getId())
        );
        System.out.println("=== ACTUAL COUNT IN DB: " + count);

        assertThat(saved.getHistory()).hasSize(2);
    }

    @Test
    @Transactional
    @Rollback
    void shouldAddHistoryEntryOnPaymentAwaiting() {
        Order order = createOrder();
        order.assignManager(managerId);
        order.awaitPayment();
        Order saved = orderRepository.save(order);

        assertThat(saved.getHistory()).hasSize(3);
        assertThat(saved.getHistory().get(0).getAction()).isEqualTo("ORDER_CREATED");
        assertThat(saved.getHistory().get(1).getAction()).isEqualTo("MANAGER_ASSIGNED");
        assertThat(saved.getHistory().get(2).getAction()).isEqualTo("AWAITING_PAYMENT");
    }

    @Test
    @Transactional
    @Rollback
    void shouldAddHistoryEntryOnPayment() {
        Order order = createOrder();
        order.assignManager(managerId);
        order.awaitPayment();
        order.markAsPaid();
        Order saved = orderRepository.save(order);

        assertThat(saved.getHistory()).hasSize(4);
        assertThat(saved.getHistory().get(0).getAction()).isEqualTo("ORDER_CREATED");
        assertThat(saved.getHistory().get(1).getAction()).isEqualTo("MANAGER_ASSIGNED");
        assertThat(saved.getHistory().get(2).getAction()).isEqualTo("AWAITING_PAYMENT");
        assertThat(saved.getHistory().get(3).getAction()).isEqualTo("PAID");
    }

    @Test
    @Transactional
    @Rollback
    void shouldAddHistoryEntryOnOrderCancellation() {
        Order order = createOrder();
        order.cancel("Customer request");
        Order saved = orderRepository.save(order);

        assertThat(saved.getHistory()).hasSize(2);
        assertThat(saved.getHistory().get(0).getAction()).isEqualTo("ORDER_CREATED");
        assertThat(saved.getHistory().get(1).getAction()).isEqualTo("CANCELLED");
        assertThat(saved.getHistory().get(1).getDescription()).contains("Customer request");
    }

    @Test
    @Transactional
    @Rollback
    void shouldAddHistoryEntryOnOrderCompletion() {
        Order order = createOrder();
        order.assignManager(managerId);
        order.awaitPayment();
        order.markAsPaid();
        order.markAsReadyForPickup();
        order.markAsCompleted();
        Order saved = orderRepository.save(order);

        assertThat(saved.getHistory()).hasSize(6);
        assertThat(saved.getHistory().get(0).getAction()).isEqualTo("ORDER_CREATED");
        assertThat(saved.getHistory().get(1).getAction()).isEqualTo("MANAGER_ASSIGNED");
        assertThat(saved.getHistory().get(2).getAction()).isEqualTo("AWAITING_PAYMENT");
        assertThat(saved.getHistory().get(3).getAction()).isEqualTo("PAID");
        assertThat(saved.getHistory().get(4).getAction()).isEqualTo("READY_FOR_PICKUP");
        assertThat(saved.getHistory().get(5).getAction()).isEqualTo("COMPLETED");
    }

    @Test
    @Transactional
    @Rollback
    void shouldPreserveHistoryOrder() {
        Order order = createOrder();
        order.assignManager(managerId);
        order.awaitPayment();
        order.markAsPaid();
        Order saved = orderRepository.save(order);

        assertThat(saved.getHistory()).hasSize(4);

        assertThat(saved.getHistory().get(0).getAction()).isEqualTo("ORDER_CREATED");
        assertThat(saved.getHistory().get(0).getTimestamp()).isNotNull();
    }
}