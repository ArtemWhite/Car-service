package infrastructure.jpaRepository.sparePartJpaRepositories.sparePartJpaRepositoriesComponents;

import infrastructure.entities.sparePartEntities.SparePartEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SparePartStockJpaRepository {

    @Query("SELECT s FROM SparePartEntity s WHERE s.stockQuantity >= :minQuantity AND s.removed = false")
    List<SparePartEntity> findByStockQuantity(@Param("minQuantity") int minQuantity);

    @Query("SELECT s FROM SparePartEntity s WHERE s.stockQuantity = 0 AND s.removed = false")
    List<SparePartEntity> findOutOfStock();

    @Query("SELECT s FROM SparePartEntity s WHERE s.stockQuantity > 0 AND s.stockQuantity < :threshold AND s.removed = false")
    List<SparePartEntity> findLowStock(@Param("threshold") int threshold);

    @Query("SELECT s FROM SparePartEntity s WHERE s.sectionId = :sectionId AND s.removed = false")
    List<SparePartEntity> findBySection(@Param("sectionId") String sectionId);

    @Query("SELECT s FROM SparePartEntity s WHERE s.location = :location AND s.removed = false")
    List<SparePartEntity> findByLocation(@Param("location") String location);
}