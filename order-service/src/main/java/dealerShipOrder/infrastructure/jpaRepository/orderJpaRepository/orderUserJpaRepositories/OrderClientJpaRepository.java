package dealerShipOrder.infrastructure.jpaRepository.orderJpaRepository.orderUserJpaRepositories;

import dealerShipOrder.infrastructure.entities.orderEntities.OrderEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderClientJpaRepository {

    @Query("SELECT o FROM OrderEntity o WHERE o.clientId = :clientId AND o.removed = false")
    List<OrderEntity> findByClientId(@Param("clientId") String clientId);

    @Query("SELECT o FROM OrderEntity o WHERE o.clientId = :clientId AND o.status.name = :status AND o.removed = false")
    List<OrderEntity> findByClientIdAndStatus(@Param("clientId") String clientId,
                                              @Param("status") String status);

    @Query("SELECT COUNT(o) FROM OrderEntity o WHERE o.clientId = :clientId AND o.removed = false")
    long countByClientId(@Param("clientId") String clientId);

    @Query("SELECT o FROM OrderEntity o WHERE o.clientId = :clientId ORDER BY o.createdAt DESC")
    List<OrderEntity> findClientOrdersByDateDesc(@Param("clientId") String clientId);
}