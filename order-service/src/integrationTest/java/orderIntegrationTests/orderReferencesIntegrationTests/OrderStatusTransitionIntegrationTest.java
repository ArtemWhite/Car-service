package orderIntegrationTests.orderReferencesIntegrationTests;

import dealerShipOrder.BaseIntegrationTest;
import dealerShipOrder.domain.models.expection.DomainValidationException;
import dealerShipOrder.domain.models.order.Order;
import dealerShipOrder.domain.models.order.OrderStatus;
import dealerShipOrder.domain.repository.orderRepository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
class OrderStatusTransitionIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String clientId;
    private String carId;
    private String configurationId;
    private String carModelId;
    private String managerId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM orders");
        jdbcTemplate.execute("DELETE FROM order_history_entries");

        clientId = UUID.randomUUID().toString();
        carId = UUID.randomUUID().toString();
        configurationId = UUID.randomUUID().toString();
        carModelId = UUID.randomUUID().toString();
        managerId = UUID.randomUUID().toString();
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
    void shouldTransitionFromCreatedToManagerApproved() {
        Order order = createInStockOrder();
        Order saved = orderRepository.save(order);
        assertThat(saved.getStatus()).isEqualTo(OrderStatus.CREATED);

        saved.assignManager(managerId);
        Order updated = orderRepository.save(saved);
        assertThat(updated.getStatus()).isEqualTo(OrderStatus.MANAGER_APPROVED);
        assertThat(updated.getManagerId()).isEqualTo(managerId);
    }

    @Test
    @Transactional
    @Rollback
    void shouldNotAssignManagerTwice() {
        Order order = createInStockOrder();
        Order saved = orderRepository.save(order);
        saved.assignManager(managerId);
        assertThatThrownBy(() -> saved.assignManager(managerId))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("Manager already assigned");
    }

    @Test
    @Transactional
    @Rollback
    void shouldTransitionFromCreatedToStockConfirmedForCustomOrder() {
        Order order = createCustomOrder();
        Order saved = orderRepository.save(order);
        assertThat(saved.getStatus()).isEqualTo(OrderStatus.CREATED);

        saved.confirmByStock();
        Order updated = orderRepository.save(saved);
        assertThat(updated.getStatus()).isEqualTo(OrderStatus.STOCK_CONFIRMED);
    }

    @Test
    @Transactional
    @Rollback
    void shouldNotConfirmStockForInStockOrder() {
        Order order = createInStockOrder();
        Order saved = orderRepository.save(order);
        assertThatThrownBy(saved::confirmByStock)
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("Only custom orders need stock confirmation");
    }

    @Test
    @Transactional
    @Rollback
    void shouldTransitionFromManagerApprovedToAwaitingPayment() {
        Order order = createInStockOrder();
        Order saved = orderRepository.save(order);
        saved.assignManager(managerId);
        saved.awaitPayment();
        Order updated = orderRepository.save(saved);
        assertThat(updated.getStatus()).isEqualTo(OrderStatus.AWAITING_PAYMENT);
    }

    @Test
    @Transactional
    @Rollback
    void shouldTransitionFromStockConfirmedToAwaitingPayment() {
        Order order = createCustomOrder();
        Order saved = orderRepository.save(order);
        saved.confirmByStock();
        saved.awaitPayment();
        Order updated = orderRepository.save(saved);
        assertThat(updated.getStatus()).isEqualTo(OrderStatus.AWAITING_PAYMENT);
    }

    @Test
    @Transactional
    @Rollback
    void shouldNotAwaitPaymentFromInvalidStatus() {
        Order order = createInStockOrder();
        Order saved = orderRepository.save(order);
        assertThatThrownBy(saved::awaitPayment)
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    @Transactional
    @Rollback
    void shouldTransitionFromAwaitingPaymentToPaid() {
        Order order = createInStockOrder();
        Order saved = orderRepository.save(order);
        saved.assignManager(managerId);
        saved.awaitPayment();
        saved.markAsPaid();
        Order updated = orderRepository.save(saved);
        assertThat(updated.getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    @Transactional
    @Rollback
    void shouldNotMarkAsPaidFromInvalidStatus() {
        Order order = createInStockOrder();
        Order saved = orderRepository.save(order);
        assertThatThrownBy(saved::markAsPaid)
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("Order is not awaiting payment");
    }

    @Test
    @Transactional
    @Rollback
    void shouldTransitionFromPaidToReadyForPickupForInStockOrder() {
        Order order = createInStockOrder();
        Order saved = orderRepository.save(order);
        saved.assignManager(managerId);
        saved.awaitPayment();
        saved.markAsPaid();
        saved.markAsReadyForPickup();
        Order updated = orderRepository.save(saved);
        assertThat(updated.getStatus()).isEqualTo(OrderStatus.READY_FOR_PICKUP);
    }

    @Test
    @Transactional
    @Rollback
    void shouldTransitionFromPaidToAwaitingDeliveryForCustomOrder() {
        Order order = createCustomOrder();
        Order saved = orderRepository.save(order);
        saved.confirmByStock();
        saved.awaitPayment();
        saved.markAsPaid();
        saved.waitForDelivery();
        Order updated = orderRepository.save(saved);
        assertThat(updated.getStatus()).isEqualTo(OrderStatus.AWAITING_DELIVERY);
    }

    @Test
    @Transactional
    @Rollback
    void shouldTransitionFromAwaitingDeliveryToReadyForPickup() {
        Order order = createCustomOrder();
        Order saved = orderRepository.save(order);
        saved.confirmByStock();
        saved.awaitPayment();
        saved.markAsPaid();
        saved.waitForDelivery();
        saved.markAsDelivered();
        Order updated = orderRepository.save(saved);
        assertThat(updated.getStatus()).isEqualTo(OrderStatus.READY_FOR_PICKUP);
    }

    @Test
    @Transactional
    @Rollback
    void shouldTransitionFromReadyForPickupToCompleted() {
        Order order = createInStockOrder();
        Order saved = orderRepository.save(order);
        saved.assignManager(managerId);
        saved.awaitPayment();
        saved.markAsPaid();
        saved.markAsReadyForPickup();
        saved.markAsCompleted();
        Order updated = orderRepository.save(saved);
        assertThat(updated.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(updated.getCompletedAt()).isNotNull();
    }

    @Test
    @Transactional
    @Rollback
    void shouldCancelOrderFromAnyStatusExceptCompleted() {
        Order order = createInStockOrder();
        Order saved = orderRepository.save(order);
        saved.cancel("Customer changed mind");
        Order updated = orderRepository.save(saved);
        assertThat(updated.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @Transactional
    @Rollback
    void shouldNotCancelCompletedOrder() {
        Order order = createInStockOrder();
        Order saved = orderRepository.save(order);
        saved.assignManager(managerId);
        saved.awaitPayment();
        saved.markAsPaid();
        saved.markAsReadyForPickup();
        saved.markAsCompleted();
        assertThatThrownBy(() -> saved.cancel("Too late"))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("Cannot cancel completed order");
    }

    @Test
    @Transactional
    @Rollback
    void shouldNotCancelAlreadyCancelledOrder() {
        Order order = createInStockOrder();
        Order saved = orderRepository.save(order);
        saved.cancel("First reason");
        assertThatThrownBy(() -> saved.cancel("Second reason"))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("Order already cancelled");
    }

    @Test
    @Transactional
    @Rollback
    void shouldCheckIfOrderIsActive() {
        Order order = createInStockOrder();
        Order saved = orderRepository.save(order);
        assertThat(saved.isActive()).isTrue();

        saved.cancel("Cancelled");
        Order cancelled = orderRepository.save(saved);
        assertThat(cancelled.isActive()).isFalse();
    }

    @Test
    @Transactional
    @Rollback
    void shouldCheckIfOrderIsInStockOrCustom() {
        Order inStock = createInStockOrder();
        Order custom = createCustomOrder();

        assertThat(inStock.isInStockOrder()).isTrue();
        assertThat(inStock.isCustomOrder()).isFalse();
        assertThat(custom.isInStockOrder()).isFalse();
        assertThat(custom.isCustomOrder()).isTrue();
    }
}