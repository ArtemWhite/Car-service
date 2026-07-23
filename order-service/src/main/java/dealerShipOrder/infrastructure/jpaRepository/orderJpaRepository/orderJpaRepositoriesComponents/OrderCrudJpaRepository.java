package dealerShipOrder.infrastructure.jpaRepository.orderJpaRepository.orderJpaRepositoriesComponents;

import dealerShipOrder.infrastructure.entities.orderEntities.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderCrudJpaRepository extends JpaRepository<OrderEntity, UUID>
{
}