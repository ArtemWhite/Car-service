package dealerShipOrder.infrastructure.jpaRepository;

import dealerShipOrder.infrastructure.entities.OutboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxJpaRepository extends JpaRepository<OutboxEventEntity, UUID> {

    @Query("SELECT e FROM OutboxEventEntity e WHERE e.status = 'PENDING' ORDER BY e.createdAt ASC")
    List<OutboxEventEntity> findPendingEvents();

    @Query("SELECT e FROM OutboxEventEntity e WHERE e.status = 'PENDING' AND e.createdAt <= :maxAge ORDER BY e.createdAt ASC")
    List<OutboxEventEntity> findPendingEventsOlderThan(@Param("maxAge") Instant maxAge);

    @Modifying
    @Transactional
    @Query("UPDATE OutboxEventEntity e SET e.status = 'PROCESSED', e.processedAt = :processedAt WHERE e.id = :id")
    int markAsProcessed(@Param("id") UUID id, @Param("processedAt") Instant processedAt);

    @Modifying
    @Transactional
    @Query("UPDATE OutboxEventEntity e SET e.status = 'FAILED', e.retryCount = e.retryCount + 1, e.lastError = :error WHERE e.id = :id")
    int markAsFailed(@Param("id") UUID id, @Param("error") String error);

    @Modifying
    @Transactional
    @Query("DELETE FROM OutboxEventEntity e WHERE e.status = 'PROCESSED' AND e.processedAt <= :olderThan")
    int deleteProcessedEventsOlderThan(@Param("olderThan") Instant olderThan);

    @Query("SELECT COUNT(e) FROM OutboxEventEntity e WHERE e.status = 'PENDING'")
    long countPendingEvents();

    @Query("SELECT COUNT(e) FROM OutboxEventEntity e WHERE e.status = 'FAILED'")
    long countFailedEvents();
}