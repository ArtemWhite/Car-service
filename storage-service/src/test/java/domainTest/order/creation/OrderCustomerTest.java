package domainTest.order.creation;

import domain.exception.DomainValidationException;
import domain.models.order.Order;
import domain.models.order.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

class OrderCustomerTest {

    private Order customOrder;

    @BeforeEach
    void setUp() {
        customOrder = Order.createCustomOrder("order228","client123", "config789", "model321");
    }

    @Test
    @DisplayName("Should wait for delivery after payment")
    void shouldWaitForDeliveryAfterPayment() {
        customOrder.assignManager("manager789");

        customOrder.confirmByStock();
        customOrder.awaitPayment();
        customOrder.markAsPaid();
        assertEquals(OrderStatus.PAID, customOrder.getStatus());

        customOrder.waitForDelivery();

        assertEquals(OrderStatus.AWAITING_DELIVERY, customOrder.getStatus());
    }

    @Test
    @DisplayName("Should mark custom order as delivered")
    void shouldMarkCustomOrderAsDelivered() {
        customOrder.assignManager("manager789");
        customOrder.confirmByStock();
        customOrder.awaitPayment();
        customOrder.markAsPaid();
        customOrder.waitForDelivery();
        assertEquals(OrderStatus.AWAITING_DELIVERY, customOrder.getStatus());

        customOrder.markAsDelivered();

        assertEquals(OrderStatus.READY_FOR_PICKUP, customOrder.getStatus());
    }

    @Test
    @DisplayName("Should not wait for delivery for in-stock order")
    void shouldNotWaitForDeliveryForInStockOrder() {
        Order inStockOrder = Order.createInStockOrder("dddd","client123", "car456");

        assertThrows(DomainValidationException.class,
                () -> inStockOrder.waitForDelivery());
    }

    @Test
    @DisplayName("Should complete custom order lifecycle")
    void shouldCompleteCustomOrderLifecycle() {
        customOrder.assignManager("manager789");
        customOrder.confirmByStock();
        customOrder.awaitPayment();
        customOrder.markAsPaid();
        customOrder.waitForDelivery();
        customOrder.markAsDelivered();
        customOrder.markAsCompleted();

        assertEquals(OrderStatus.COMPLETED, customOrder.getStatus());
    }
}
