package dealerShipOrder.infrastructure.jpaRepository.userJpaRepositories.referenceUserJpaRepositories;

import dealerShipOrder.infrastructure.entities.userEntities.referenceUserEntities.UserStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserStatusJpaRepository extends JpaRepository<UserStatusEntity, String> {

    Optional<UserStatusEntity> findByName(String name);
}