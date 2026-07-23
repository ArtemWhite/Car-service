package infrastructure.jpaRepository.sparePartJpaRepositories.sparePartJpaRepositoriesComponents;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SparePartStatisticsJpaRepository {

    @Query("SELECT COUNT(s) FROM SparePartEntity s WHERE s.removed = false")
    long getTotalSparePartsCount();

    @Query("SELECT COUNT(s) FROM SparePartEntity s WHERE s.stockQuantity = 0 AND s.removed = false")
    long getOutOfStockCount();

    @Query("SELECT COUNT(s) FROM SparePartEntity s WHERE s.stockQuantity > 0 AND s.stockQuantity < :threshold AND s.removed = false")
    long getLowStockCount(@Param("threshold") int threshold);

    @Query("SELECT SUM(s.stockQuantity) FROM SparePartEntity s WHERE s.removed = false")
    long getTotalStockQuantity();

    @Query("SELECT AVG(s.price) FROM SparePartEntity s WHERE s.removed = false")
    Double getAveragePrice();

    @Query("SELECT s.manufacturer, COUNT(s), AVG(s.price) FROM SparePartEntity s WHERE s.removed = false GROUP BY s.manufacturer")
    List<Object[]> getStatisticsByManufacturer();

    @Query("SELECT s.type.name, COUNT(s), SUM(s.stockQuantity) FROM SparePartEntity s WHERE s.removed = false GROUP BY s.type.name")
    List<Object[]> getStatisticsByType();
}