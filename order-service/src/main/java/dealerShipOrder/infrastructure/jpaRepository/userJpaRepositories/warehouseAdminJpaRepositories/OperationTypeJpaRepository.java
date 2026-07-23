package dealerShipOrder.infrastructure.jpaRepository.userJpaRepositories.warehouseAdminJpaRepositories;

import dealerShipOrder.infrastructure.entities.userEntities.warehouseAdminEntities.OperationTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OperationTypeJpaRepository extends JpaRepository<OperationTypeEntity, String> {

    Optional<OperationTypeEntity> findByName(String name);
}