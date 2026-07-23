package dealerShipOrder.application.dtos.response.paymentResponse;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private String id;
    private String orderId;
    private String clientId;
    private Double amount;
    private String amountFormatted;
    private String method;
    private String methodDisplayName;
    private String status;
    private String statusDisplayName;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private String transactionId;
    private String failureReason;

    private boolean pending;
    private boolean processing;
    private boolean completed;
    private boolean failed;
    private boolean refunded;

    private String cardLastFour;
    private String paymentSystem;
}