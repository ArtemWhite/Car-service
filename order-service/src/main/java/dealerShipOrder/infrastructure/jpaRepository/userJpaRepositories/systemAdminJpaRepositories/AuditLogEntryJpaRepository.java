package dealerShipOrder.infrastructure.jpaRepository.userJpaRepositories.systemAdminJpaRepositories;

import dealerShipOrder.infrastructure.entities.userEntities.systemAdminEntities.AuditLogEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AuditLogEntryJpaRepository extends JpaRepository<AuditLogEntryEntity, UUID> {

    @Query("SELECT a FROM AuditLogEntryEntity a WHERE a.admin.id = :adminId AND a.removed = false")
    List<AuditLogEntryEntity> findByAdminId(@Param("adminId") UUID adminId);

    @Query("SELECT a FROM AuditLogEntryEntity a WHERE a.admin.id = :adminId AND a.timestamp BETWEEN :start AND :end AND a.removed = false")
    List<AuditLogEntryEntity> findByAdminIdAndDateRange(@Param("adminId") UUID adminId,
                                                        @Param("start") Instant start,
                                                        @Param("end") Instant end);

    @Query("SELECT a FROM AuditLogEntryEntity a WHERE a.action = :action AND a.removed = false")
    List<AuditLogEntryEntity> findByAction(@Param("action") String action);
}