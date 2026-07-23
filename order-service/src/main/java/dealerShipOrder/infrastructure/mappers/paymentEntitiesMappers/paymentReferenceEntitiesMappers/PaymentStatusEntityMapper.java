package dealerShipOrder.infrastructure.mappers.paymentEntitiesMappers.paymentReferenceEntitiesMappers;

import dealerShipOrder.domain.models.payment.PaymentStatus;
import dealerShipOrder.infrastructure.entities.paymentEntities.referencePaymentEntities.PaymentStatusEntity;
import dealerShipOrder.infrastructure.jpaRepository.paymentJpaRepositories.referencePaymentJpaRepositories.PaymentStatusReferenceJpaRepository;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class PaymentStatusEntityMapper
{
    @Autowired
    protected PaymentStatusReferenceJpaRepository paymentStatusRepository;

    public PaymentStatusEntity toEntity(PaymentStatus paymentStatus) {
        if (paymentStatus == null) return null;
        return paymentStatusRepository.findByName(paymentStatus.name())
                .orElseThrow(() -> new RuntimeException("Payment status not found: " + paymentStatus.name()));
    }

    public PaymentStatus toDomain(PaymentStatusEntity entity) {
        if (entity == null) return null;
        return PaymentStatus.valueOf(entity.getName());
    }
}
