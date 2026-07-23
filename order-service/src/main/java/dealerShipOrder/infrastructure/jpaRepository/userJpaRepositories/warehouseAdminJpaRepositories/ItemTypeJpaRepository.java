package dealerShipOrder.infrastructure.jpaRepository.userJpaRepositories.warehouseAdminJpaRepositories;

import dealerShipOrder.infrastructure.entities.userEntities.systemAdminEntities.ItemTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItemTypeJpaRepository extends JpaRepository<ItemTypeEntity, String> {

    Optional<ItemTypeEntity> findByName(String name);
}