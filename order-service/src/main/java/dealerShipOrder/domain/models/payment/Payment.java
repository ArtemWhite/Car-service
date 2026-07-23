package dealerShipOrder.domain.models.payment;

import dealerShipOrder.domain.models.expection.DomainValidationException;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class Payment {
    private final String id;
    private final String orderId;
    private final String clientId;
    private final double amount;
    private final PaymentMethod method;
    private PaymentStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime processedAt;
    @Setter
    private String transactionId;
    private String failureReason;

    public Payment(String id, String orderId, String clientId, double amount, PaymentMethod method) {
        this.id = id;
        this.orderId = orderId;
        this.clientId = clientId;
        this.amount = amount;
        this.method = method;
        this.status = PaymentStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public void process() {
        if (status != PaymentStatus.PENDING) {
            throw new DomainValidationException("Payment already processed");
        }
        this.status = PaymentStatus.COMPLETED;
        this.processedAt = LocalDateTime.now();
        if (this.transactionId == null || this.transactionId.isEmpty()) {
            this.transactionId = "TXN-" + UUID.randomUUID();
        }
    }

    public void fail(String reason) {
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
        this.processedAt = LocalDateTime.now();
    }

    public void refund() {
        if (status != PaymentStatus.COMPLETED) {
            throw new DomainValidationException("Only completed payments can be refunded");
        }
        this.status = PaymentStatus.REFUNDED;
        this.processedAt = LocalDateTime.now();
    }

}

