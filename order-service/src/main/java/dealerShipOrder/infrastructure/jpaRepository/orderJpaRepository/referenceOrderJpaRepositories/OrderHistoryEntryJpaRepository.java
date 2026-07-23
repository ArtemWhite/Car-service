package dealerShipOrder.infrastructure.jpaRepository.orderJpaRepository.referenceOrderJpaRepositories;

import dealerShipOrder.infrastructure.entities.orderEntities.OrderHistoryEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OrderHistoryEntryJpaRepository extends JpaRepository<OrderHistoryEntryEntity, UUID> {

    @Query("SELECT h FROM OrderHistoryEntryEntity h WHERE h.order.id = :orderId AND h.removed = false ORDER BY h.timestamp ASC")
    List<OrderHistoryEntryEntity> findByOrderId(@Param("orderId") UUID orderId);

    @Query("SELECT h FROM OrderHistoryEntryEntity h WHERE h.order.id = :orderId AND h.action = :action AND h.removed = false")
    List<OrderHistoryEntryEntity> findByOrderIdAndAction(@Param("orderId") UUID orderId,
                                                         @Param("action") String action);

    @Query("SELECT h FROM OrderHistoryEntryEntity h WHERE h.timestamp BETWEEN :start AND :end AND h.removed = false")
    List<OrderHistoryEntryEntity> findByDateRange(@Param("start") Instant start,
                                                  @Param("end") Instant end);
}