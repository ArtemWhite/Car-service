package dealerShipOrder.presentation.dtos.response.paymentResponsePresentationDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Payment response")
public class PaymentPresentationResponse {

    @Schema(description = "Payment ID", example = "pay_123e4567")
    private String id;

    @Schema(description = "Order ID", example = "ord_123e4567")
    private String orderId;

    @Schema(description = "Client ID", example = "user_123")
    private String clientId;

    @Schema(description = "Payment amount", example = "2500000.0")
    private Double amount;

    @Schema(description = "Formatted amount", example = "2,500,000 ₽")
    private String amountFormatted;

    @Schema(description = "Payment method code", example = "CARD")
    private String method;

    @Schema(description = "Payment method display name", example = "Bank Card")
    private String methodDisplayName;

    @Schema(description = "Payment status code", example = "COMPLETED")
    private String status;

    @Schema(description = "Payment status display name", example = "Completed")
    private String statusDisplayName;

    @Schema(description = "Created date", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Processed date", example = "2024-01-15T10:31:00")
    private LocalDateTime processedAt;

    @Schema(description = "Transaction ID from payment gateway", example = "txn_123e4567")
    private String transactionId;

    @Schema(description = "Failure reason (if failed)", example = "Insufficient funds")
    private String failureReason;

    @Schema(description = "Whether payment is pending", example = "false")
    private boolean pending;

    @Schema(description = "Whether payment is processing", example = "false")
    private boolean processing;

    @Schema(description = "Whether payment is completed", example = "true")
    private boolean completed;

    @Schema(description = "Whether payment failed", example = "false")
    private boolean failed;

    @Schema(description = "Whether payment is refunded", example = "false")
    private boolean refunded;

    @Schema(description = "Last 4 digits of card", example = "1111")
    private String cardLastFour;

    @Schema(description = "Payment system", example = "Visa")
    private String paymentSystem;
}