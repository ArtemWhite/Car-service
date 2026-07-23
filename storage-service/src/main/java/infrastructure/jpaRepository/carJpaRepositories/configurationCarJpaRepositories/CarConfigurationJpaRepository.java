package infrastructure.jpaRepository.carJpaRepositories.configurationCarJpaRepositories;

import infrastructure.entities.carEntities.configurationCarEntities.CarConfigurationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CarConfigurationJpaRepository extends JpaRepository<CarConfigurationEntity, UUID>,
        JpaSpecificationExecutor<CarConfigurationEntity> {

    Optional<CarConfigurationEntity> findByIdAndRemovedFalse(UUID id);
    List<CarConfigurationEntity> findAllByRemovedFalse();

    @Query("SELECT c FROM CarConfigurationEntity c WHERE c.model.id = :modelId AND c.removed = false")
    List<CarConfigurationEntity> findByModelId(@Param("modelId") UUID modelId);

    @Query("SELECT c FROM CarConfigurationEntity c WHERE c.model.brand.name = :brand AND c.removed = false")
    List<CarConfigurationEntity> findByBrand(@Param("brand") String brand);

    @Query("SELECT c FROM CarConfigurationEntity c WHERE c.basePrice BETWEEN :minPrice AND :maxPrice AND c.removed = false")
    List<CarConfigurationEntity> findByPriceRange(@Param("minPrice") BigDecimal minPrice,
                                                  @Param("maxPrice") BigDecimal maxPrice);
}