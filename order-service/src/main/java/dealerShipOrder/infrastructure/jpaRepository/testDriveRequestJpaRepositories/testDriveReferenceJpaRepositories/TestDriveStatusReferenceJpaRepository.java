package dealerShipOrder.infrastructure.jpaRepository.testDriveRequestJpaRepositories.testDriveReferenceJpaRepositories;

import dealerShipOrder.infrastructure.entities.testDriveRequestEntities.TestDriveStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TestDriveStatusReferenceJpaRepository extends JpaRepository<TestDriveStatusEntity, String> {
    Optional<TestDriveStatusEntity> findByName(String name);
}