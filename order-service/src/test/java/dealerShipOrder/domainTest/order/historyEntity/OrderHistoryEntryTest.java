package dealerShipOrder.domainTest.order.historyEntity;

import dealerShipOrder.domain.models.order.OrderHistoryEntry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OrderHistoryEntry Tests")
class OrderHistoryEntryTest {

    @Test
    @DisplayName("Should create history entry")
    void shouldCreateHistoryEntry() {
        String action = "ORDER_CREATED";
        String description = "Order was created";
        LocalDateTime now = LocalDateTime.now();

        OrderHistoryEntry entry = new OrderHistoryEntry(action, description, now);

        assertEquals(action, entry.getAction());
        assertEquals(description, entry.getDescription());
        assertEquals(now, entry.getTimestamp());
    }

    @Test
    @DisplayName("Should create entries with different timestamps")
    void shouldCreateEntriesWithDifferentTimestamps() {
        LocalDateTime time1 = LocalDateTime.now().minusDays(1);
        LocalDateTime time2 = LocalDateTime.now();

        OrderHistoryEntry entry1 = new OrderHistoryEntry(null,"ACTION1", "Desc1", time1);
        OrderHistoryEntry entry2 = new OrderHistoryEntry(null,"ACTION2", "Desc2", time2);

        assertEquals(time1, entry1.getTimestamp());
        assertEquals(time2, entry2.getTimestamp());
        assertTrue(entry2.getTimestamp().isAfter(entry1.getTimestamp()));
    }
}