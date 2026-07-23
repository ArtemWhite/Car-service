package dealerShipOrder.infrastructure.jpaRepository.orderJpaRepository.orderJpaRepositoriesComponents;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface OrderStatisticsJpaRepository {

    @Query("SELECT COUNT(o) FROM OrderEntity o WHERE o.status.name = :status AND o.removed = false")
    long countByStatus(@Param("status") String status);

    @Query("SELECT COUNT(o) FROM OrderEntity o WHERE o.type.name = :type AND o.removed = false")
    long countByOrderType(@Param("type") String type);

    @Query("SELECT COUNT(o) FROM OrderEntity o WHERE o.clientId = :clientId AND o.removed = false")
    long countByClientId(@Param("clientId") String clientId);

    @Query("SELECT CAST(o.createdAt AS date), COUNT(o) FROM OrderEntity o " +
            "WHERE o.createdAt BETWEEN :start AND :end AND o.removed = false " +
            "GROUP BY CAST(o.createdAt AS date) ORDER BY CAST(o.createdAt AS date)")
    List<Object[]> countOrdersByDay(@Param("start") Instant start, @Param("end") Instant end);

    @Query("SELECT o.status.name, COUNT(o) FROM OrderEntity o WHERE o.removed = false GROUP BY o.status.name")
    List<Object[]> countOrdersByStatus();

    @Query("SELECT AVG(o.updatedAt - o.createdAt) FROM OrderEntity o WHERE o.status.name = 'COMPLETED'")
    Double getAverageCompletionTime();
}