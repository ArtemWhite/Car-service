package dealerShipOrder.infrastructure.jpaRepository.testDriveRequestJpaRepositories.testDriveJpaRepositoriesComponents;

import dealerShipOrder.infrastructure.entities.testDriveRequestEntities.TestDriveRequestEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TestDriveBaseJpaRepository {

    @Query("SELECT t FROM TestDriveRequestEntity t WHERE t.id = :id AND t.removed = false")
    Optional<TestDriveRequestEntity> findTestDriveByIdAndRemovedFalse(@Param("id") UUID id);

    @Query("SELECT t FROM TestDriveRequestEntity t WHERE t.removed = false")
    List<TestDriveRequestEntity> findAllTestDrivesByRemovedFalse();

    @Query("SELECT t FROM TestDriveRequestEntity t WHERE t.removed = false")
    Page<TestDriveRequestEntity> findAllTestDrivesByRemovedFalse(Pageable pageable);

    @Query("SELECT t FROM TestDriveRequestEntity t WHERE t.removed = false ORDER BY t.requestedTime DESC")
    List<TestDriveRequestEntity> findAllTestDrivesOrderByDateDesc();
}