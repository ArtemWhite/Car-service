package dealerShipOrder.infrastructure.jpaRepository.userJpaRepositories.warehouseAdminJpaRepositories;

import dealerShipOrder.infrastructure.entities.userEntities.warehouseAdminEntities.WarehousePositionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WarehousePositionJpaRepository extends JpaRepository<WarehousePositionEntity, String> {

    Optional<WarehousePositionEntity> findByName(String name);
}