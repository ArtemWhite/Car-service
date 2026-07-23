package dealerShipOrder.infrastructure.jpaRepository.testDriveRequestJpaRepositories.testDriveJpaRepositoriesComponents;

import dealerShipOrder.infrastructure.entities.testDriveRequestEntities.TestDriveRequestEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface TestDriveDateJpaRepository {

    @Query("SELECT t FROM TestDriveRequestEntity t WHERE t.requestedTime BETWEEN :start AND :end AND t.removed = false")
    List<TestDriveRequestEntity> findByDateRange(@Param("start") Instant start,
                                                 @Param("end") Instant end);

    @Query("SELECT t FROM TestDriveRequestEntity t WHERE t.requestedTime > CURRENT_TIMESTAMP AND t.removed = false")
    List<TestDriveRequestEntity> findUpcomingTestDrives();

    @Query("SELECT t FROM TestDriveRequestEntity t WHERE t.requestedTime < CURRENT_TIMESTAMP AND t.removed = false")
    List<TestDriveRequestEntity> findPastTestDrives();

    @Query("SELECT t FROM TestDriveRequestEntity t WHERE DATE(t.requestedTime) = :date AND t.status.name = :status AND t.removed = false")
    List<TestDriveRequestEntity> findByDateAndStatus(@Param("date") Instant date,
                                                     @Param("status") String status);

    @Query("SELECT t FROM TestDriveRequestEntity t WHERE t.requestedTime BETWEEN :start AND :end AND t.removed = false ORDER BY t.requestedTime")
    List<TestDriveRequestEntity> findByDateTimeBetween(@Param("start") Instant start,
                                                       @Param("end") Instant end);
}