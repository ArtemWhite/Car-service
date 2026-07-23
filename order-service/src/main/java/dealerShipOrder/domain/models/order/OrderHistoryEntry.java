package dealerShipOrder.domain.models.order;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class OrderHistoryEntry {
    private final String orderId;
    private final String action;
    private final String description;
    private final LocalDateTime timestamp;

    public OrderHistoryEntry(String orderId, String action, String description, LocalDateTime timestamp) {
        this.orderId = orderId;
        this.action = action;
        this.description = description;
        this.timestamp = timestamp;
    }

    public OrderHistoryEntry(String action, String description, LocalDateTime timestamp) {
        this.orderId = null;
        this.action = action;
        this.description = description;
        this.timestamp = timestamp;
    }

}