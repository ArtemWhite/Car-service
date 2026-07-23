package infrastructure.jpaRepository.carJpaRepositories.carJpaRepositoriesComponents;

import infrastructure.entities.carEntities.CarEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface CarPriceJpaRepository {
    @Query("SELECT c FROM CarEntity c WHERE c.price BETWEEN :minPrice AND :maxPrice AND c.removed = false")
    List<CarEntity> findByPriceRange(@Param("minPrice") BigDecimal minPrice,
                                     @Param("maxPrice") BigDecimal maxPrice);

    @Query("SELECT c FROM CarEntity c WHERE c.price < :maxPrice AND c.removed = false")
    List<CarEntity> findByPriceLessThan(@Param("maxPrice") BigDecimal maxPrice);

    @Query("SELECT c FROM CarEntity c WHERE c.price > :minPrice AND c.removed = false")
    List<CarEntity> findByPriceGreaterThan(@Param("minPrice") BigDecimal minPrice);
}