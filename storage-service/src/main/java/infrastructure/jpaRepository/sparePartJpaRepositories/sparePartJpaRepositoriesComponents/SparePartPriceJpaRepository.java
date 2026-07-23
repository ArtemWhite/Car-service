package infrastructure.jpaRepository.sparePartJpaRepositories.sparePartJpaRepositoriesComponents;

import infrastructure.entities.sparePartEntities.SparePartEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface SparePartPriceJpaRepository {

    @Query("SELECT s FROM SparePartEntity s WHERE s.price BETWEEN :minPrice AND :maxPrice AND s.removed = false")
    List<SparePartEntity> findByPriceRange(@Param("minPrice") BigDecimal minPrice,
                                           @Param("maxPrice") BigDecimal maxPrice);

    @Query("SELECT s FROM SparePartEntity s WHERE s.price < :maxPrice AND s.removed = false")
    List<SparePartEntity> findByPriceLessThan(@Param("maxPrice") BigDecimal maxPrice);

    @Query("SELECT s FROM SparePartEntity s WHERE s.price > :minPrice AND s.removed = false")
    List<SparePartEntity> findByPriceGreaterThan(@Param("minPrice") BigDecimal minPrice);
}