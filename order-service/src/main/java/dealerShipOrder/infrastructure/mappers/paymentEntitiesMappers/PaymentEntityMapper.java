package dealerShipOrder.infrastructure.mappers.paymentEntitiesMappers;

import dealerShipOrder.domain.models.payment.*;
import dealerShipOrder.infrastructure.entities.paymentEntities.PaymentEntity;
import dealerShipOrder.infrastructure.entities.paymentEntities.referencePaymentEntities.PaymentMethodEntity;
import dealerShipOrder.infrastructure.entities.paymentEntities.referencePaymentEntities.PaymentStatusEntity;
import dealerShipOrder.infrastructure.jpaRepository.paymentJpaRepositories.referencePaymentJpaRepositories.PaymentMethodReferenceJpaRepository;
import dealerShipOrder.infrastructure.jpaRepository.paymentJpaRepositories.referencePaymentJpaRepositories.PaymentStatusReferenceJpaRepository;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Mapper(componentModel = "spring")
public abstract class PaymentEntityMapper {

    @Autowired
    protected PaymentMethodReferenceJpaRepository paymentMethodRepository;

    @Autowired
    protected PaymentStatusReferenceJpaRepository paymentStatusRepository;

    public PaymentEntity toEntity(Payment payment) {
        if (payment == null) return null;

        PaymentEntity entity = new PaymentEntity();
        entity.setId(toUuid(payment.getId()));
        entity.setOrderId(payment.getOrderId());
        entity.setClientId(payment.getClientId());
        entity.setAmount(BigDecimal.valueOf(payment.getAmount()));
        entity.setMethod(toPaymentMethodEntity(payment.getMethod()));
        entity.setStatus(toPaymentStatusEntity(payment.getStatus()));
        entity.setProcessedAt(toInstant(payment.getProcessedAt()));
        entity.setTransactionId(payment.getTransactionId());
        entity.setFailureReason(payment.getFailureReason());
        entity.setCreatedAt(toInstant(payment.getCreatedAt()));
        entity.setUpdatedAt(Instant.now());
        entity.setRemoved(false);

        return entity;
    }

    public Payment toDomain(PaymentEntity entity) {
        if (entity == null) return null;

        Payment payment = new Payment(
                entity.getId().toString(),
                entity.getOrderId(),
                entity.getClientId(),
                entity.getAmount().doubleValue(),
                toPaymentMethod(entity.getMethod())
        );

        restoreStatus(payment, toPaymentStatus(entity.getStatus()));
        restoreProcessedAt(payment, toLocalDateTime(entity.getProcessedAt()));
        restoreTransactionId(payment, entity.getTransactionId());
        restoreFailureReason(payment, entity.getFailureReason());

        return payment;
    }

    protected String toUuid(UUID uuid) {
        return uuid == null ? null : uuid.toString();
    }

    protected UUID toUuid(String id) {
        if (id == null) return null;
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    protected Instant toInstant(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.atZone(ZoneId.systemDefault()).toInstant();
    }

    protected LocalDateTime toLocalDateTime(Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    protected PaymentMethodEntity toPaymentMethodEntity(PaymentMethod method) {
        if (method == null) return null;
        return paymentMethodRepository.findByName(method.name())
                .orElseThrow(() -> new RuntimeException("Payment method not found: " + method.name()));
    }

    protected PaymentMethod toPaymentMethod(PaymentMethodEntity entity) {
        if (entity == null) return null;
        return PaymentMethod.valueOf(entity.getName());
    }

    protected PaymentStatusEntity toPaymentStatusEntity(PaymentStatus status) {
        if (status == null) return null;
        return paymentStatusRepository.findByName(status.name())
                .orElseThrow(() -> new RuntimeException("Payment status not found: " + status.name()));
    }

    protected PaymentStatus toPaymentStatus(PaymentStatusEntity entity) {
        if (entity == null) return null;
        return PaymentStatus.valueOf(entity.getName());
    }

    private void restoreStatus(Payment payment, PaymentStatus status) {
        try {
            java.lang.reflect.Field field = Payment.class.getDeclaredField("status");
            field.setAccessible(true);
            field.set(payment, status);
        } catch (Exception e) {
            throw new RuntimeException("Failed to restore payment status", e);
        }
    }

    private void restoreProcessedAt(Payment payment, LocalDateTime processedAt) {
        try {
            java.lang.reflect.Field field = Payment.class.getDeclaredField("processedAt");
            field.setAccessible(true);
            field.set(payment, processedAt);
        } catch (Exception e) {
            throw new RuntimeException("Failed to restore processedAt", e);
        }
    }

    private void restoreTransactionId(Payment payment, String transactionId) {
        try {
            java.lang.reflect.Field field = Payment.class.getDeclaredField("transactionId");
            field.setAccessible(true);
            field.set(payment, transactionId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to restore transactionId", e);
        }
    }

    private void restoreFailureReason(Payment payment, String failureReason) {
        try {
            java.lang.reflect.Field field = Payment.class.getDeclaredField("failureReason");
            field.setAccessible(true);
            field.set(payment, failureReason);
        } catch (Exception e) {
            throw new RuntimeException("Failed to restore failureReason", e);
        }
    }
}