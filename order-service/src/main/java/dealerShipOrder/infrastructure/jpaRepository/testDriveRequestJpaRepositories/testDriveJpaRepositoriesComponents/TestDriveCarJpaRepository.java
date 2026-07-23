package dealerShipOrder.infrastructure.jpaRepository.testDriveRequestJpaRepositories.testDriveJpaRepositoriesComponents;

import dealerShipOrder.infrastructure.entities.testDriveRequestEntities.TestDriveRequestEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface TestDriveCarJpaRepository {

    @Query("SELECT t FROM TestDriveRequestEntity t WHERE t.carId = :carId AND t.removed = false")
    List<TestDriveRequestEntity> findByCarId(@Param("carId") String carId);

    @Query("SELECT t FROM TestDriveRequestEntity t WHERE t.carId = :carId AND t.status.name = :status AND t.removed = false")
    List<TestDriveRequestEntity> findByCarIdAndStatus(@Param("carId") String carId,
                                                      @Param("status") String status);

    @Query("SELECT t FROM TestDriveRequestEntity t WHERE t.carId = :carId AND t.requestedTime > CURRENT_TIMESTAMP AND t.removed = false")
    List<TestDriveRequestEntity> findUpcomingByCarId(@Param("carId") String carId);

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM TestDriveRequestEntity t " +
            "WHERE t.carId = :carId AND t.requestedTime = :time AND t.status.name != 'CANCELLED' AND t.removed = false")
    boolean isCarBookedForTestDrive(@Param("carId") String carId, @Param("time") Instant time);
}