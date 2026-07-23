package dealerShipOrder.infrastructure.entities.paymentEntities;

import javax.persistence.*;

import dealerShipOrder.infrastructure.entities.paymentEntities.referencePaymentEntities.PaymentMethodEntity;
import dealerShipOrder.infrastructure.entities.paymentEntities.referencePaymentEntities.PaymentStatusEntity;
import dealerShipOrder.infrastructure.entities.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payments")
@Where(clause = "removed = false")
@Getter
@Setter
public class PaymentEntity extends BaseEntity {

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "client_id", nullable = false)
    private String clientId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "method_id", nullable = false)
    private PaymentMethodEntity method;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", nullable = false)
    private PaymentStatusEntity status;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;
}