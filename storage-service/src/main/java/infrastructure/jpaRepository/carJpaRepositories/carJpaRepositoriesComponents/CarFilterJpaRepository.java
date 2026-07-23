package infrastructure.jpaRepository.carJpaRepositories.carJpaRepositoriesComponents;

import infrastructure.entities.carEntities.CarEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface CarFilterJpaRepository {
    @Query("SELECT c FROM CarEntity c WHERE " +
            "(:brand IS NULL OR c.brand.name = :brand) AND " +
            "(:model IS NULL OR c.model.name = :model) AND " +
            "(:body IS NULL OR c.body.name = :body) AND " +
            "(:color IS NULL OR c.color.name = :color) AND " +
            "(:driveType IS NULL OR c.driveType.name = :driveType) AND " +
            "(:minPrice IS NULL OR c.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR c.price <= :maxPrice) AND " +
            "c.removed = false")
    List<CarEntity> findByFilters(@Param("brand") String brand,
                                  @Param("model") String model,
                                  @Param("body") String body,
                                  @Param("color") String color,
                                  @Param("driveType") String driveType,
                                  @Param("minPrice") BigDecimal minPrice,
                                  @Param("maxPrice") BigDecimal maxPrice);
}
