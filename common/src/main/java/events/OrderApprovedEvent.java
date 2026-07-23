package events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderApprovedEvent extends BaseEvent {

    public static final String TYPE = "ORDER_APPROVED";

    private String assemblyOrderId;

    private String message;

    private String approvedAt;

    public OrderApprovedEvent(String orderId, String traceId, String assemblyOrderId, String message) {
        super(null, TYPE, orderId, traceId, null, 1);
        this.assemblyOrderId = assemblyOrderId;
        this.message = message;
        this.approvedAt = Instant.now().toString();
    }

    public OrderApprovedEvent(String orderId, String traceId, String assemblyOrderId, String message, String approvedAt) {
        super(null, TYPE, orderId, traceId, null, 1);
        this.assemblyOrderId = assemblyOrderId;
        this.message = message;
        this.approvedAt = approvedAt;
    }
}