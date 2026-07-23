package dealerShipOrder.infrastructure.jpaRepository.orderJpaRepository.referenceOrderJpaRepositories;

import dealerShipOrder.infrastructure.entities.orderEntities.referenceOrderEntities.OrderStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderStatusReferenceJpaRepository extends JpaRepository<OrderStatusEntity, String> {

    Optional<OrderStatusEntity> findByName(String name);
}