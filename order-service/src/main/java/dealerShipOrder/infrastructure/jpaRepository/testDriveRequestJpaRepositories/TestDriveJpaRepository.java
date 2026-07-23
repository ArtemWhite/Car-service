package dealerShipOrder.infrastructure.jpaRepository.testDriveRequestJpaRepositories;

import dealerShipOrder.infrastructure.jpaRepository.testDriveRequestJpaRepositories.testDriveJpaRepositoriesComponents.*;
import dealerShipOrder.infrastructure.jpaRepository.testDriveRequestJpaRepositories.testDriveUserJpaRepositories.TestDriveClientJpaRepository;
import dealerShipOrder.infrastructure.jpaRepository.testDriveRequestJpaRepositories.testDriveUserJpaRepositories.TestDriveManagerJpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestDriveJpaRepository extends
        TestDriveCrudJpaRepository,
        TestDriveBaseJpaRepository,
        TestDriveStatusJpaRepository,
        TestDriveClientJpaRepository,
        TestDriveCarJpaRepository,
        TestDriveManagerJpaRepository,
        TestDriveDateJpaRepository,
        TestDriveConflictJpaRepository,
        TestDriveStatisticsJpaRepository,
        TestDriveUpdateJpaRepository {
}