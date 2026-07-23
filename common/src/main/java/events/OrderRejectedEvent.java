package events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderRejectedEvent extends BaseEvent {

    public static final String TYPE = "ORDER_REJECTED";

    private String assemblyOrderId;

    private String reason;

    private String missingComponents;

    public OrderRejectedEvent(String orderId, String traceId, String assemblyOrderId, String reason) {
        super(null, TYPE, orderId, traceId, null, 1);
        this.assemblyOrderId = assemblyOrderId;
        this.reason = reason;
    }

    public OrderRejectedEvent(String orderId, String traceId, String assemblyOrderId,
                              String reason, String missingComponents) {
        super(null, TYPE, orderId, traceId, null, 1);
        this.assemblyOrderId = assemblyOrderId;
        this.reason = reason;
        this.missingComponents = missingComponents;
    }
}