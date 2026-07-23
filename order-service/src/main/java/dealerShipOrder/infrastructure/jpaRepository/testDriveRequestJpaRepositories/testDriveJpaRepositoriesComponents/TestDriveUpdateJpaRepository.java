package dealerShipOrder.infrastructure.jpaRepository.testDriveRequestJpaRepositories.testDriveJpaRepositoriesComponents;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

public interface TestDriveUpdateJpaRepository {

    @Modifying
    @Transactional
    @Query("UPDATE TestDriveRequestEntity t SET t.status = :status WHERE t.id = :id")
    int updateStatus(@Param("id") UUID id, @Param("status") String status);

    @Modifying
    @Transactional
    @Query("UPDATE TestDriveRequestEntity t SET t.managerId = :managerId WHERE t.id = :id")
    int assignManager(@Param("id") UUID id, @Param("managerId") String managerId);

    @Modifying
    @Transactional
    @Query("UPDATE TestDriveRequestEntity t SET t.confirmedTime = :confirmedTime WHERE t.id = :id")
    int confirmTime(@Param("id") UUID id, @Param("confirmedTime") Instant confirmedTime);

    @Modifying
    @Transactional
    @Query("UPDATE TestDriveRequestEntity t SET t.requestedTime = :newTime WHERE t.id = :id")
    int reschedule(@Param("id") UUID id, @Param("newTime") Instant newTime);

    @Modifying
    @Transactional
    @Query("UPDATE TestDriveRequestEntity t SET t.notes = :notes WHERE t.id = :id")
    int updateNotes(@Param("id") UUID id, @Param("notes") String notes);

    @Modifying
    @Transactional
    @Query("UPDATE TestDriveRequestEntity t SET t.removed = true WHERE t.id = :id")
    int softDelete(@Param("id") UUID id);
}