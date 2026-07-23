package dealerShipOrder.infrastructure.jpaRepository.testDriveRequestJpaRepositories.testDriveJpaRepositoriesComponents;


import dealerShipOrder.infrastructure.entities.testDriveRequestEntities.TestDriveRequestEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TestDriveStatusJpaRepository {

    @Query("SELECT t FROM TestDriveRequestEntity t WHERE t.status.name = :status AND t.removed = false")
    List<TestDriveRequestEntity> findByStatus(@Param("status") String status);

    @Query("SELECT t FROM TestDriveRequestEntity t WHERE t.status.name IN (:statuses) AND t.removed = false")
    List<TestDriveRequestEntity> findByStatusIn(@Param("statuses") List<String> statuses);

    @Query("SELECT t FROM TestDriveRequestEntity t WHERE t.status.name = 'PENDING' AND t.removed = false")
    List<TestDriveRequestEntity> findPendingTestDrives();

    @Query("SELECT t FROM TestDriveRequestEntity t WHERE t.status.name = 'CONFIRMED' AND t.removed = false")
    List<TestDriveRequestEntity> findConfirmedTestDrives();

    @Query("SELECT t FROM TestDriveRequestEntity t WHERE t.status.name = 'COMPLETED' AND t.removed = false")
    List<TestDriveRequestEntity> findCompletedTestDrives();

    @Query("SELECT t FROM TestDriveRequestEntity t WHERE t.status.name = 'CANCELLED' AND t.removed = false")
    List<TestDriveRequestEntity> findCancelledTestDrives();

    @Query("SELECT t FROM TestDriveRequestEntity t WHERE t.status.name = 'NO_SHOW' AND t.removed = false")
    List<TestDriveRequestEntity> findNoShowTestDrives();
}