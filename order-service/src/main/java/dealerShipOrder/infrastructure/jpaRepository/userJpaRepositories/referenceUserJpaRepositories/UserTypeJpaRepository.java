package dealerShipOrder.infrastructure.jpaRepository.userJpaRepositories.referenceUserJpaRepositories;

import dealerShipOrder.infrastructure.entities.userEntities.referenceUserEntities.UserTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserTypeJpaRepository extends JpaRepository<UserTypeEntity, String> {

    Optional<UserTypeEntity> findByName(String name);
}