package dealerShipOrder.infrastructure.jpaRepository.orderJpaRepository.orderJpaRepositoriesComponents;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

public interface OrderUpdateJpaRepository {

    @Modifying
    @Transactional
    @Query("UPDATE OrderEntity o SET o.status = :status WHERE o.id = :id")
    int updateStatus(@Param("id") UUID id, @Param("status") String status);

    @Modifying
    @Transactional
    @Query("UPDATE OrderEntity o SET o.managerId = :managerId WHERE o.id = :id")
    int assignManager(@Param("id") UUID id, @Param("managerId") String managerId);

    @Modifying
    @Transactional
    @Query("UPDATE OrderEntity o SET o.notes = :notes WHERE o.id = :id")
    int updateNotes(@Param("id") UUID id, @Param("notes") String notes);

    @Modifying
    @Transactional
    @Query("UPDATE OrderEntity o SET o.completedAt = :completedAt WHERE o.id = :id")
    int markCompleted(@Param("id") UUID id, @Param("completedAt") Instant completedAt);

    @Modifying
    @Transactional
    @Query("UPDATE OrderEntity o SET o.removed = true WHERE o.id = :id")
    int softDelete(@Param("id") UUID id);
}