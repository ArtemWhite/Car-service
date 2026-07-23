package dealerShipOrder.infrastructure.jpaRepository.testDriveRequestJpaRepositories.testDriveUserJpaRepositories;

import dealerShipOrder.infrastructure.entities.testDriveRequestEntities.TestDriveRequestEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TestDriveManagerJpaRepository {

    @Query("SELECT t FROM TestDriveRequestEntity t WHERE t.managerId = :managerId AND t.removed = false")
    List<TestDriveRequestEntity> findByManagerId(@Param("managerId") String managerId);

    @Query("SELECT t FROM TestDriveRequestEntity t WHERE t.managerId = :managerId AND t.status.name = :status AND t.removed = false")
    List<TestDriveRequestEntity> findByManagerIdAndStatus(@Param("managerId") String managerId,
                                                          @Param("status") String status);

    @Query("SELECT t FROM TestDriveRequestEntity t WHERE t.managerId = :managerId AND t.requestedTime > CURRENT_TIMESTAMP AND t.removed = false")
    List<TestDriveRequestEntity> findUpcomingByManagerId(@Param("managerId") String managerId);

    @Query("SELECT COUNT(t) FROM TestDriveRequestEntity t WHERE t.managerId = :managerId AND t.removed = false")
    long countAssignedToManager(@Param("managerId") String managerId);
}