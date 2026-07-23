package dealerShipOrder.infrastructure.jpaRepository.testDriveRequestJpaRepositories.testDriveJpaRepositoriesComponents;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface TestDriveStatisticsJpaRepository {

    @Query("SELECT COUNT(t) FROM TestDriveRequestEntity t WHERE t.status.name = :status AND t.removed = false")
    long countByStatus(@Param("status") String status);

    @Query("SELECT COUNT(t) FROM TestDriveRequestEntity t WHERE t.clientId = :clientId AND t.removed = false")
    long countByClientId(@Param("clientId") String clientId);

    @Query("SELECT COUNT(t) FROM TestDriveRequestEntity t WHERE t.carId = :carId AND t.removed = false")
    long countByCarId(@Param("carId") String carId);

    @Query("SELECT COUNT(t) FROM TestDriveRequestEntity t WHERE t.managerId = :managerId AND t.removed = false")
    long countByManagerId(@Param("managerId") String managerId);

    @Query("SELECT CAST(t.requestedTime AS date), COUNT(t) FROM TestDriveRequestEntity t " +
            "WHERE t.requestedTime BETWEEN :start AND :end AND t.removed = false " +
            "GROUP BY CAST(t.requestedTime AS date) ORDER BY CAST(t.requestedTime AS date)")
    List<Object[]> countTestDrivesByDay(@Param("start") Instant start, @Param("end") Instant end);

    @Query("SELECT t.status.name, COUNT(t) FROM TestDriveRequestEntity t WHERE t.removed = false GROUP BY t.status.name")
    List<Object[]> countTestDrivesByStatus();

    @Query("SELECT AVG(t.requestedTime - t.createdAt) FROM TestDriveRequestEntity t WHERE t.status.name = 'COMPLETED'")
    Double getAverageConfirmationTime();
}