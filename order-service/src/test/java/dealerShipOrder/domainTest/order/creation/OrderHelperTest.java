package dealerShipOrder.domainTest.order.creation;

import dealerShipOrder.domain.models.order.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class OrderHelperTest {


    private Order inStockOrder;
    private Order customOrder;

    @BeforeEach
    void setUp() {
        inStockOrder = Order.createInStockOrder("order221","client123", "car456");
        customOrder = Order.createCustomOrder("order444","client123", "config789", "model321");
    }

    @Test
    @DisplayName("Should correctly identify in-stock order")
    void shouldIdentifyInStockOrder() {
        assertTrue(inStockOrder.isInStockOrder());
        assertFalse(customOrder.isInStockOrder());
    }

    @Test
    @DisplayName("Should correctly identify custom order")
    void shouldIdentifyCustomOrder() {
        assertTrue(customOrder.isCustomOrder());
        assertFalse(inStockOrder.isCustomOrder());
    }

    @Test
    @DisplayName("Should identify active orders")
    void shouldIdentifyActiveOrders() {
        assertTrue(inStockOrder.isActive());

        inStockOrder.cancel("Test");
        assertFalse(inStockOrder.isActive());

        Order completedOrder = Order.createInStockOrder("orderff","client123", "car456");
        completedOrder.assignManager("manager789");
        completedOrder.awaitPayment();
        completedOrder.markAsPaid();
        completedOrder.markAsReadyForPickup();
        completedOrder.markAsCompleted();
        assertFalse(completedOrder.isActive());
    }

    @Test
    @DisplayName("Should set and get notes")
    void shouldSetAndGetNotes() {
        assertNull(inStockOrder.getNotes());

        inStockOrder.setNotes("Important note");

        assertEquals("Important note", inStockOrder.getNotes());
    }

    @Test
    @DisplayName("Should update updatedAt when notes change")
    void shouldUpdateTimestampWhenNotesChange() throws InterruptedException {
        LocalDateTime initialTime = inStockOrder.getUpdatedAt();

        Thread.sleep(10);
        inStockOrder.setNotes("New note");

        assertTrue(inStockOrder.getUpdatedAt().isAfter(initialTime));
    }
}