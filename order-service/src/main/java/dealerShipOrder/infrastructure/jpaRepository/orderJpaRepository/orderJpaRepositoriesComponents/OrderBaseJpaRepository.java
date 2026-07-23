package dealerShipOrder.infrastructure.jpaRepository.orderJpaRepository.orderJpaRepositoriesComponents;

import dealerShipOrder.infrastructure.entities.orderEntities.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderBaseJpaRepository {
    @Query("SELECT o FROM OrderEntity o " +
            "LEFT JOIN FETCH o.status " +
            "LEFT JOIN FETCH o.type " +
            "LEFT JOIN FETCH o.history " +
            "WHERE o.id = :id AND o.removed = false")
    Optional<OrderEntity> findOrderByIdAndRemovedFalse(@Param("id") UUID id);

    @Query("SELECT o FROM OrderEntity o " +
            "LEFT JOIN FETCH o.status " +
            "LEFT JOIN FETCH o.type " +
            "WHERE o.removed = false")
    List<OrderEntity> findAllOrdersByRemovedFalse();

    @Query("SELECT o FROM OrderEntity o WHERE o.removed = false")
    Page<OrderEntity> findAllOrdersByRemovedFalse(Pageable pageable);

    @Query("SELECT o FROM OrderEntity o WHERE o.removed = false ORDER BY o.createdAt DESC")
    List<OrderEntity> findAllOrdersOrderByCreatedAtDesc();
}