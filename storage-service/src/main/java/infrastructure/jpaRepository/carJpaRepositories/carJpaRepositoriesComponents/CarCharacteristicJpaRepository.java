package infrastructure.jpaRepository.carJpaRepositories.carJpaRepositoriesComponents;

import infrastructure.entities.carEntities.CarEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CarCharacteristicJpaRepository {
    @Query("SELECT c FROM CarEntity c WHERE c.brand.name = :brand AND c.removed = false")
    List<CarEntity> findByBrand(@Param("brand") String brand);

    @Query("SELECT c FROM CarEntity c WHERE c.model.name = :model AND c.removed = false")
    List<CarEntity> findByModel(@Param("model") String model);

    @Query("SELECT c FROM CarEntity c WHERE c.brand.name = :brand AND c.model.name = :model AND c.removed = false")
    List<CarEntity> findByBrandAndModel(@Param("brand") String brand, @Param("model") String model);

    @Query("SELECT c FROM CarEntity c WHERE c.body.name = :body AND c.removed = false")
    List<CarEntity> findByBody(@Param("body") String body);

    @Query("SELECT c FROM CarEntity c WHERE c.color.name = :color AND c.removed = false")
    List<CarEntity> findByColor(@Param("color") String color);

    @Query("SELECT c FROM CarEntity c WHERE c.driveType.name = :driveType AND c.removed = false")
    List<CarEntity> findByDriveType(@Param("driveType") String driveType);
}
