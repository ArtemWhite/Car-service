package infrastructure.jpaRepository.carJpaRepositories.referenceCarJpaRepositories;

import infrastructure.entities.carEntities.referenceCarEntities.CarModelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CarModelJpaRepository extends JpaRepository<CarModelEntity, UUID> {

    Optional<CarModelEntity> findByIdAndRemovedFalse(UUID id);

    List<CarModelEntity> findAllByRemovedFalse();

    @Query("SELECT m FROM CarModelEntity m WHERE m.brand.id = :brandId AND m.removed = false")
    List<CarModelEntity> findByBrandId(@Param("brandId") String brandId);

    @Query("SELECT m FROM CarModelEntity m WHERE m.name = :name AND m.brand.name = :brandName AND m.removed = false")
    Optional<CarModelEntity> findByNameAndBrand(@Param("name") String name, @Param("brandName") String brandName);

    @Query("SELECT m FROM CarModelEntity m WHERE m.generation = :generation AND m.removed = false")
    List<CarModelEntity> findByGeneration(@Param("generation") String generation);
}