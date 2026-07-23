package dealerShipOrder.infrastructure.jpaRepository.userJpaRepositories.managerJpaRepositories;

import dealerShipOrder.infrastructure.entities.userEntities.managerEntities.ManagerPositionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ManagerPositionJpaRepository extends JpaRepository<ManagerPositionEntity, String> {

    Optional<ManagerPositionEntity> findByName(String name);
}