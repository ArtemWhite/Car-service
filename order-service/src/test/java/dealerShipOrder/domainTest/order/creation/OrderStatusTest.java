package dealerShipOrder.domainTest.order.creation;


import dealerShipOrder.domain.models.expection.DomainValidationException;
import dealerShipOrder.domain.models.order.Order;
import dealerShipOrder.domain.models.order.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.*;

class OrderStatusTest {

    private Order inStockOrder;
    private Order customOrder;

    @BeforeEach
    void setUp() {
        inStockOrder = Order.createInStockOrder("order1","client123", "car456");
        customOrder = Order.createCustomOrder("order2","client123", "config789", "model321");
    }

    @Test
    @DisplayName("Should assign manager to in-stock order")
    void shouldAssignManagerToInStockOrder() {
        assertEquals(OrderStatus.CREATED, inStockOrder.getStatus());

        inStockOrder.assignManager("manager789");

        assertEquals(OrderStatus.MANAGER_APPROVED, inStockOrder.getStatus());
        assertEquals("manager789", inStockOrder.getManagerId());
        assertEquals(2, inStockOrder.getHistory().size());
    }

    @Test
    @DisplayName("Should assign manager to custom order")
    void shouldAssignManagerToCustomOrder() {
        assertEquals(OrderStatus.CREATED, customOrder.getStatus());

        customOrder.assignManager("manager789");

        assertEquals(OrderStatus.MANAGER_APPROVED, customOrder.getStatus());
        assertEquals("manager789", customOrder.getManagerId());
    }

    @Test
    @DisplayName("Should not assign manager twice")
    void shouldNotAssignManagerTwice() {
        inStockOrder.assignManager("manager789");

        assertThrows(DomainValidationException.class,
                () -> inStockOrder.assignManager("manager456"));
    }

    @Test
    @DisplayName("Should confirm custom order by stock")
    void shouldConfirmCustomOrderByStock() {
        customOrder.assignManager("manager789");
        assertEquals(OrderStatus.MANAGER_APPROVED, customOrder.getStatus());

        customOrder.confirmByStock();

        assertEquals(OrderStatus.STOCK_CONFIRMED, customOrder.getStatus());
    }

    @Test
    @DisplayName("Should not confirm in-stock order by stock")
    void shouldNotConfirmInStockOrderByStock() {
        assertThrows(DomainValidationException.class,
                () -> inStockOrder.confirmByStock());
    }

    @Test
    @DisplayName("Should move to awaiting payment from manager approved")
    void shouldMoveToAwaitingPaymentFromManagerApproved() {
        inStockOrder.assignManager("manager789");
        assertEquals(OrderStatus.MANAGER_APPROVED, inStockOrder.getStatus());

        inStockOrder.awaitPayment();

        assertEquals(OrderStatus.AWAITING_PAYMENT, inStockOrder.getStatus());
    }

    @Test
    @DisplayName("Should move to awaiting payment from stock confirmed")
    void shouldMoveToAwaitingPaymentFromStockConfirmed() {
        Order customOrder = Order.createCustomOrder("order123", "client123", "config456", "model789");

        customOrder.confirmByStock();
        assertEquals(OrderStatus.STOCK_CONFIRMED, customOrder.getStatus());

        customOrder.assignManager("manager789");
        assertEquals(OrderStatus.MANAGER_APPROVED, customOrder.getStatus());

        customOrder.awaitPayment();
        assertEquals(OrderStatus.AWAITING_PAYMENT, customOrder.getStatus());
    }

    @Test
    @DisplayName("Should mark order as paid")
    void shouldMarkOrderAsPaid() {
        inStockOrder.assignManager("manager789");
        inStockOrder.awaitPayment();
        assertEquals(OrderStatus.AWAITING_PAYMENT, inStockOrder.getStatus());

        inStockOrder.markAsPaid();

        assertEquals(OrderStatus.PAID, inStockOrder.getStatus());
    }

    @Test
    @DisplayName("Should mark order as ready for pickup")
    void shouldMarkOrderAsReadyForPickup() {
        inStockOrder.assignManager("manager789");
        inStockOrder.awaitPayment();
        inStockOrder.markAsPaid();
        assertEquals(OrderStatus.PAID, inStockOrder.getStatus());

        inStockOrder.markAsReadyForPickup();

        assertEquals(OrderStatus.READY_FOR_PICKUP, inStockOrder.getStatus());
    }

    @Test
    @DisplayName("Should mark order as completed")
    void shouldMarkOrderAsCompleted() {
        inStockOrder.assignManager("manager789");
        inStockOrder.awaitPayment();
        inStockOrder.markAsPaid();
        inStockOrder.markAsReadyForPickup();
        assertEquals(OrderStatus.READY_FOR_PICKUP, inStockOrder.getStatus());

        inStockOrder.markAsCompleted();

        assertEquals(OrderStatus.COMPLETED, inStockOrder.getStatus());
        assertNotNull(inStockOrder.getCompletedAt());
    }

    @Test
    @DisplayName("Should have all required status values")
    void shouldHaveAllRequiredValues() {
        OrderStatus[] statuses = OrderStatus.values();

        assertEquals(9, statuses.length);

        assertEquals("Оформлен", OrderStatus.CREATED.getDisplayName());
        assertEquals("Согласован менеджером", OrderStatus.MANAGER_APPROVED.getDisplayName());
        assertEquals("Ожидает оплаты", OrderStatus.AWAITING_PAYMENT.getDisplayName());
        assertEquals("Оплачен", OrderStatus.PAID.getDisplayName());
        assertEquals("Автомобиль готов к выдаче", OrderStatus.READY_FOR_PICKUP.getDisplayName());
        assertEquals("Завершён", OrderStatus.COMPLETED.getDisplayName());
        assertEquals("Отменён", OrderStatus.CANCELLED.getDisplayName());
        assertEquals("Согласован складом", OrderStatus.STOCK_CONFIRMED.getDisplayName());
        assertEquals("Ожидает доставки автомобиля", OrderStatus.AWAITING_DELIVERY.getDisplayName());
    }

    @Test
    @DisplayName("Should have correct display names")
    void shouldHaveCorrectDisplayNames() {
        assertEquals("Оформлен", OrderStatus.CREATED.getDisplayName());
        assertEquals("Завершён", OrderStatus.COMPLETED.getDisplayName());
        assertEquals("Отменён", OrderStatus.CANCELLED.getDisplayName());
    }
}

