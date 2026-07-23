package dealerShipOrder.infrastructure.jpaRepository.userJpaRepositories.systemAdminJpaRepositories;

import dealerShipOrder.infrastructure.entities.userEntities.systemAdminEntities.SystemPermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SystemPermissionJpaRepository extends JpaRepository<SystemPermissionEntity, String> {

    Optional<SystemPermissionEntity> findByName(String name);
}