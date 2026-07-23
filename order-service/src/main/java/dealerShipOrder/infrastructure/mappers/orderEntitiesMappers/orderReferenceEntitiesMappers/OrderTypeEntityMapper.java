package dealerShipOrder.infrastructure.mappers.orderEntitiesMappers.orderReferenceEntitiesMappers;

import dealerShipOrder.domain.models.order.OrderType;
import dealerShipOrder.infrastructure.entities.orderEntities.referenceOrderEntities.OrderTypeEntity;
import dealerShipOrder.infrastructure.jpaRepository.orderJpaRepository.referenceOrderJpaRepositories.OrderTypeReferenceJpaRepository;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class OrderTypeEntityMapper
{
    @Autowired
    protected OrderTypeReferenceJpaRepository orderTypeRepository;

    public OrderTypeEntity toEntity(OrderType orderType)
    {
        if (orderType == null)
            return null;

        return orderTypeRepository.findByName(orderType.name())
                .orElseThrow(() -> new RuntimeException("Order type not found: " + orderType.name()));
    }

    public OrderType toDomain(OrderTypeEntity entity)
    {
        if (entity == null)
            return null;

        return OrderType.valueOf(entity.getName());
    }
}
