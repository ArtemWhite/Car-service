package dealerShipOrder.domainTest.order.creation;

import dealerShipOrder.domain.models.expection.DomainValidationException;
import dealerShipOrder.domain.models.order.Order;
import dealerShipOrder.domain.models.order.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

class OrderCancellationTest {

    private Order order;

    @BeforeEach
    void setUp() {
        order = Order.createInStockOrder("order66","client123", "car456");
    }

    @Test
    @DisplayName("Should cancel order at CREATED status")
    void shouldCancelOrderAtCreatedStatus() {
        assertEquals(OrderStatus.CREATED, order.getStatus());

        order.cancel("Client changed mind");

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertEquals(2, order.getHistory().size());
    }

    @Test
    @DisplayName("Should cancel order at MANAGER_APPROVED status")
    void shouldCancelOrderAtManagerApprovedStatus() {
        order.assignManager("manager789");
        assertEquals(OrderStatus.MANAGER_APPROVED, order.getStatus());

        order.cancel("Found better deal");

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    @DisplayName("Should cancel order at AWAITING_PAYMENT status")
    void shouldCancelOrderAtAwaitingPaymentStatus() {
        order.assignManager("manager789");
        order.awaitPayment();
        assertEquals(OrderStatus.AWAITING_PAYMENT, order.getStatus());

        order.cancel("Payment issues");

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    @DisplayName("Should cancel order at PAID status")
    void shouldCancelOrderAtPaidStatus() {
        order.assignManager("manager789");
        order.awaitPayment();
        order.markAsPaid();
        assertEquals(OrderStatus.PAID, order.getStatus());

        order.cancel("Requested refund");

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    @DisplayName("Should not cancel completed order")
    void shouldNotCancelCompletedOrder() {
        order.assignManager("manager789");
        order.awaitPayment();
        order.markAsPaid();
        order.markAsReadyForPickup();
        order.markAsCompleted();
        assertEquals(OrderStatus.COMPLETED, order.getStatus());

        assertThrows(DomainValidationException.class,
                () -> order.cancel("Too late"));
    }

    @Test
    @DisplayName("Should not cancel already cancelled order")
    void shouldNotCancelAlreadyCancelledOrder() {
        order.cancel("First reason");

        assertThrows(DomainValidationException.class,
                () -> order.cancel("Second reason"));
    }
}