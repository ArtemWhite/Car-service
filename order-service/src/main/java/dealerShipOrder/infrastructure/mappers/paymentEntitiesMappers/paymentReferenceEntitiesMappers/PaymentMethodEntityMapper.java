package dealerShipOrder.infrastructure.mappers.paymentEntitiesMappers.paymentReferenceEntitiesMappers;

import dealerShipOrder.domain.models.payment.PaymentMethod;
import dealerShipOrder.infrastructure.entities.paymentEntities.referencePaymentEntities.PaymentMethodEntity;
import dealerShipOrder.infrastructure.jpaRepository.paymentJpaRepositories.referencePaymentJpaRepositories.PaymentMethodReferenceJpaRepository;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class PaymentMethodEntityMapper
{
    @Autowired
    protected PaymentMethodReferenceJpaRepository paymentMethodRepository;

    public PaymentMethodEntity toEntity(PaymentMethod paymentMethod)
    {
        if (paymentMethod == null)
            return null;

        return paymentMethodRepository.findByName(paymentMethod.name())
                .orElseThrow(() -> new RuntimeException("Payment method not found: " + paymentMethod.name()));
    }

    public PaymentMethod toDomain(PaymentMethodEntity entity)
    {
        if (entity == null)
            return null;

        return PaymentMethod.valueOf(entity.getName());
    }
}
