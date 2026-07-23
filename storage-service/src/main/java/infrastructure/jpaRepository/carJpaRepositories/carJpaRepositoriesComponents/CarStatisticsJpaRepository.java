package infrastructure.jpaRepository.carJpaRepositories.carJpaRepositoriesComponents;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CarStatisticsJpaRepository {
    @Query("SELECT COUNT(c) FROM CarEntity c WHERE c.status.name = :status AND c.removed = false")
    long countByStatus(@Param("status") String status);

    @Query("SELECT COUNT(c) FROM CarEntity c WHERE c.brand.name = :brand AND c.removed = false")
    long countByBrand(@Param("brand") String brand);

    @Query("SELECT c.brand.name, COUNT(c) FROM CarEntity c WHERE c.removed = false GROUP BY c.brand.name")
    List<Object[]> countCarsByBrand();
}