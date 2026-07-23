package dealerShipOrder.infrastructure.jpaRepository.orderJpaRepository.referenceOrderJpaRepositories;


import dealerShipOrder.infrastructure.entities.orderEntities.referenceOrderEntities.OrderTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderTypeReferenceJpaRepository extends JpaRepository<OrderTypeEntity, String> {

    Optional<OrderTypeEntity> findByName(String name);
}