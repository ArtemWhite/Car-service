package events;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderSentForApprovalEvent extends BaseEvent {

    public static final String TYPE = "ORDER_SENT_FOR_APPROVAL";

    private String orderType;

    private String carId;

    private String configurationId;

    private String carModelId;

    private String clientId;

    private Double amount;

    @Builder.Default
    private String currency = "RUB";

    public OrderSentForApprovalEvent(String orderId, String traceId,
                                     String orderType, String carId,
                                     String configurationId, String carModelId,
                                     String clientId, Double amount) {
        super(null, TYPE, orderId, traceId, null, 1);
        this.orderType = orderType;
        this.carId = carId;
        this.configurationId = configurationId;
        this.carModelId = carModelId;
        this.clientId = clientId;
        this.amount = amount;
        this.currency = "RUB";
    }
}