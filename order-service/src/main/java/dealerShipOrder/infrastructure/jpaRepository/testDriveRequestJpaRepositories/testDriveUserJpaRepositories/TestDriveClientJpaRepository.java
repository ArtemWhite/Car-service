package dealerShipOrder.infrastructure.jpaRepository.testDriveRequestJpaRepositories.testDriveUserJpaRepositories;

import dealerShipOrder.infrastructure.entities.testDriveRequestEntities.TestDriveRequestEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TestDriveClientJpaRepository {

    @Query("SELECT t FROM TestDriveRequestEntity t WHERE t.clientId = :clientId AND t.removed = false")
    List<TestDriveRequestEntity> findByClientId(@Param("clientId") String clientId);

    @Query("SELECT t FROM TestDriveRequestEntity t WHERE t.clientId = :clientId AND t.status.name = :status AND t.removed = false")
    List<TestDriveRequestEntity> findByClientIdAndStatus(@Param("clientId") String clientId,
                                                         @Param("status") String status);

    @Query("SELECT t FROM TestDriveRequestEntity t WHERE t.clientId = :clientId AND t.requestedTime > CURRENT_TIMESTAMP AND t.removed = false")
    List<TestDriveRequestEntity> findUpcomingByClientId(@Param("clientId") String clientId);

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM TestDriveRequestEntity t " +
            "WHERE t.clientId = :clientId AND t.status.name IN ('PENDING', 'CONFIRMED') AND t.removed = false")
    boolean hasActiveRequestForClient(@Param("clientId") String clientId);

    @Query("SELECT t FROM TestDriveRequestEntity t WHERE t.clientId = :clientId ORDER BY t.requestedTime DESC")
    List<TestDriveRequestEntity> findClientTestDrivesByDateDesc(@Param("clientId") String clientId);
}