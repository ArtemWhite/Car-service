package dealerShipOrder.infrastructure.jpaRepository.testDriveRequestJpaRepositories.testDriveJpaRepositoriesComponents;

import dealerShipOrder.infrastructure.entities.testDriveRequestEntities.TestDriveRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TestDriveCrudJpaRepository extends JpaRepository<TestDriveRequestEntity, UUID> {
}