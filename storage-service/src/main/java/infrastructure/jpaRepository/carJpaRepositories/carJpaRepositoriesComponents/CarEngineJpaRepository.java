package infrastructure.jpaRepository.carJpaRepositories.carJpaRepositoriesComponents;

import infrastructure.entities.carEntities.CarEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CarEngineJpaRepository {

    @Query("SELECT c FROM CarEntity c WHERE c.engine.fuelType.name = :fuelType AND c.removed = false")
    List<CarEntity> findByEngineFuelType(@Param("fuelType") String fuelType);

    @Query("SELECT c FROM CarEntity c WHERE c.engine.horsePower BETWEEN :minPower AND :maxPower AND c.removed = false")
    List<CarEntity> findByEnginePowerRange(@Param("minPower") Double minPower,
                                           @Param("maxPower") Double maxPower);

    @Query("SELECT c FROM CarEntity c WHERE c.engine.displacement BETWEEN :minVolume AND :maxVolume AND c.removed = false")
    List<CarEntity> findByEngineDisplacementRange(@Param("minVolume") Double minVolume,
                                                  @Param("maxVolume") Double maxVolume);
}