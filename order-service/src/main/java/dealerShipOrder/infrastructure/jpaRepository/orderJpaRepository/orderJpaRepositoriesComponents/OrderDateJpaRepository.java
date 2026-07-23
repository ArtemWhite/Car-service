package dealerShipOrder.infrastructure.jpaRepository.orderJpaRepository.orderJpaRepositoriesComponents;

import dealerShipOrder.infrastructure.entities.orderEntities.OrderEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface OrderDateJpaRepository {

    @Query("SELECT o FROM OrderEntity o WHERE o.createdAt BETWEEN :start AND :end AND o.removed = false")
    List<OrderEntity> findByCreatedAtBetween(@Param("start") Instant start,
                                             @Param("end") Instant end);

    @Query("SELECT o FROM OrderEntity o WHERE o.completedAt BETWEEN :start AND :end AND o.removed = false")
    List<OrderEntity> findByCompletedAtBetween(@Param("start") Instant start,
                                               @Param("end") Instant end);

    @Query("SELECT o FROM OrderEntity o WHERE DATE(o.createdAt) = :date AND o.removed = false")
    List<OrderEntity> findByCreatedAtDate(@Param("date") Instant date);

    @Query("SELECT o FROM OrderEntity o WHERE o.createdAt < :date AND o.removed = false")
    List<OrderEntity> findByCreatedAtBefore(@Param("date") Instant date);

    @Query("SELECT o FROM OrderEntity o WHERE o.createdAt > :date AND o.removed = false")
    List<OrderEntity> findByCreatedAtAfter(@Param("date") Instant date);
}