package dealerShipOrder.infrastructure.jpaRepository.userJpaRepositories.systemAdminJpaRepositories;

import dealerShipOrder.infrastructure.entities.userEntities.systemAdminEntities.AdminLevelEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminLevelJpaRepository extends JpaRepository<AdminLevelEntity, String> {

    Optional<AdminLevelEntity> findByName(String name);
}