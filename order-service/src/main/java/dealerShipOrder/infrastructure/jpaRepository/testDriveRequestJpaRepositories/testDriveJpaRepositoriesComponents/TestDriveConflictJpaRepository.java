package dealerShipOrder.infrastructure.jpaRepository.testDriveRequestJpaRepositories.testDriveJpaRepositoriesComponents;

import dealerShipOrder.infrastructure.entities.testDriveRequestEntities.TestDriveRequestEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface TestDriveConflictJpaRepository {

    @Query("SELECT t FROM TestDriveRequestEntity t WHERE t.carId = :carId " +
            "AND t.requestedTime BETWEEN :timeStart AND :timeEnd " +
            "AND t.status.name NOT IN ('CANCELLED', 'COMPLETED') " +
            "AND t.removed = false")
    List<TestDriveRequestEntity> findConflictingRequests(@Param("carId") String carId,
                                                         @Param("timeStart") Instant timeStart,
                                                         @Param("timeEnd") Instant timeEnd);

    @Query("SELECT t FROM TestDriveRequestEntity t WHERE t.managerId = :managerId " +
            "AND t.requestedTime BETWEEN :timeStart AND :timeEnd " +
            "AND t.status.name NOT IN ('CANCELLED', 'COMPLETED') " +
            "AND t.removed = false")
    List<TestDriveRequestEntity> findConflictingForManager(@Param("managerId") String managerId,
                                                           @Param("timeStart") Instant timeStart,
                                                           @Param("timeEnd") Instant timeEnd);

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM TestDriveRequestEntity t " +
            "WHERE t.carId = :carId AND t.requestedTime = :time " +
            "AND t.status.name NOT IN ('CANCELLED', 'COMPLETED') " +
            "AND t.removed = false")
    boolean hasConflict(@Param("carId") String carId, @Param("time") Instant time);
}