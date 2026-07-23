package dealerShipOrder.infrastructure.jpaRepository.orderJpaRepository.orderUserJpaRepositories;

import dealerShipOrder.infrastructure.entities.orderEntities.OrderEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderManagerJpaRepository {
    @Query("SELECT o FROM OrderEntity o WHERE o.managerId = :managerId AND o.removed = false")
    List<OrderEntity> findByManagerId(@Param("managerId") String managerId);

    @Query("SELECT o FROM OrderEntity o WHERE o.managerId = :managerId AND o.status.name = :status AND o.removed = false")
    List<OrderEntity> findByManagerIdAndStatus(@Param("managerId") String managerId,
                                               @Param("status") String status);

    @Query("SELECT o FROM OrderEntity o WHERE o.managerId IS NULL AND o.status.name = 'CREATED' AND o.removed = false")
    List<OrderEntity> findUnassignedOrders();

    @Query("SELECT COUNT(o) FROM OrderEntity o WHERE o.managerId = :managerId AND o.removed = false")
    long countByManagerId(@Param("managerId") String managerId);
}