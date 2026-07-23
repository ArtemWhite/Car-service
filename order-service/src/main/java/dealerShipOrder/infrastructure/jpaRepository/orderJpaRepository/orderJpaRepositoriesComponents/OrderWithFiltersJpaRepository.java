package dealerShipOrder.infrastructure.jpaRepository.orderJpaRepository.orderJpaRepositoriesComponents;

import dealerShipOrder.infrastructure.entities.orderEntities.OrderEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface OrderWithFiltersJpaRepository
{
    @Query("SELECT DISTINCT o FROM OrderEntity o " +
            "LEFT JOIN FETCH o.status " +
            "LEFT JOIN FETCH o.type " +
            "WHERE " +
            "(:status IS NULL OR o.status.name = :status) AND " +
            "(:type IS NULL OR o.type.name = :type) AND " +
            "(:dateFrom IS NULL OR o.createdAt >= :dateFrom) AND " +
            "(:dateTo IS NULL OR o.createdAt <= :dateTo) AND " +
            "(:clientId IS NULL OR o.clientId = :clientId) AND " +
            "(:managerId IS NULL OR o.managerId = :managerId) AND " +
            "o.removed = false")
    List<OrderEntity> findOrdersWithFilters(
            @Param("status") String status,
            @Param("type") String type,
            @Param("dateFrom") Instant dateFrom,
            @Param("dateTo") Instant dateTo,
            @Param("clientId") String clientId,
            @Param("managerId") String managerId);
}