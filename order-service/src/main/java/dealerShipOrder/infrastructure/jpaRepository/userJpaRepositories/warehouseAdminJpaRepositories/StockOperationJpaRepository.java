package dealerShipOrder.infrastructure.jpaRepository.userJpaRepositories.warehouseAdminJpaRepositories;

import dealerShipOrder.infrastructure.entities.userEntities.warehouseAdminEntities.StockOperationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface StockOperationJpaRepository extends JpaRepository<StockOperationEntity, UUID> {

    @Query("SELECT s FROM StockOperationEntity s WHERE s.admin.id = :adminId AND s.removed = false")
    List<StockOperationEntity> findByAdminId(@Param("adminId") UUID adminId);

    @Query("SELECT s FROM StockOperationEntity s WHERE s.type = :type AND s.removed = false")
    List<StockOperationEntity> findByOperationType(@Param("type") String type);

    @Query("SELECT s FROM StockOperationEntity s WHERE s.timestamp BETWEEN :start AND :end AND s.removed = false")
    List<StockOperationEntity> findByDateRange(@Param("start") Instant start, @Param("end") Instant end);

    @Query("SELECT s FROM StockOperationEntity s WHERE s.itemId = :itemId AND s.removed = false")
    List<StockOperationEntity> findByItemId(@Param("itemId") String itemId);
}