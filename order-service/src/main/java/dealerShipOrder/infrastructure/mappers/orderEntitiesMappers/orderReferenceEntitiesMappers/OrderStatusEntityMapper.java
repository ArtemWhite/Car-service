package dealerShipOrder.infrastructure.mappers.orderEntitiesMappers.orderReferenceEntitiesMappers;

import dealerShipOrder.domain.models.order.OrderStatus;
import dealerShipOrder.infrastructure.entities.orderEntities.referenceOrderEntities.OrderStatusEntity;
import dealerShipOrder.infrastructure.jpaRepository.orderJpaRepository.referenceOrderJpaRepositories.OrderStatusReferenceJpaRepository;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class OrderStatusEntityMapper
{
    @Autowired
    protected OrderStatusReferenceJpaRepository orderStatusRepository;

    public OrderStatusEntity toEntity(OrderStatus status)
    {
        if (status == null)
            return null;

        return orderStatusRepository.findByName(status.name())
                .orElseThrow(() -> new RuntimeException("Order status not found: " + status.name()));
    }

    public OrderStatus toDomain(OrderStatusEntity entity)
    {
        if (entity == null)
            return null;

        return OrderStatus.valueOf(entity.getName());
    }
}
