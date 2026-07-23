package dealerShipOrder.infrastructure.jpaRepository.orderJpaRepository.orderJpaRepositoriesComponents;

import dealerShipOrder.infrastructure.entities.orderEntities.OrderEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderStatusJpaRepository {

    @Query("SELECT o FROM OrderEntity o LEFT JOIN FETCH o.status LEFT JOIN FETCH o.type WHERE o.status.name = :status AND o.removed = false")
    List<OrderEntity> findByStatus(@Param("status") String status);

    @Query("SELECT o FROM OrderEntity o LEFT JOIN FETCH o.status LEFT JOIN FETCH o.type WHERE o.status.name IN (:statuses) AND o.removed = false")
    List<OrderEntity> findByStatusIn(@Param("statuses") List<String> statuses);

    @Query("SELECT o FROM OrderEntity o WHERE o.status.name = 'CREATED' AND o.removed = false")
    List<OrderEntity> findPendingOrders();

    @Query("SELECT o FROM OrderEntity o LEFT JOIN FETCH o.status LEFT JOIN FETCH o.type " +
            "WHERE o.status.name NOT IN ('COMPLETED', 'CANCELLED') AND o.removed = false")
    List<OrderEntity> findActiveOrders();

    @Query("SELECT o FROM OrderEntity o WHERE o.status.name = 'COMPLETED' AND o.removed = false")
    List<OrderEntity> findCompletedOrders();

    @Query("SELECT o FROM OrderEntity o WHERE o.status.name = 'CANCELLED' AND o.removed = false")
    List<OrderEntity> findCancelledOrders();

    @Query("SELECT o FROM OrderEntity o WHERE o.status.name IN ('AWAITING_PAYMENT', 'PAID') AND o.removed = false")
    List<OrderEntity> findOrdersAwaitingProcessing();
}