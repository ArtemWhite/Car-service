package infrastructure.jpaRepository.carJpaRepositories.technicalCarJpaRepositories.EngineJpaRepositories;

import infrastructure.entities.carEntities.technicalCarEntities.engineEntities.EngineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EngineJpaRepository extends JpaRepository<EngineEntity, String> {

    Optional<EngineEntity> findByIdAndRemovedFalse(String id);

    List<EngineEntity> findAllByRemovedFalse();

    @Query("SELECT e FROM EngineEntity e WHERE e.fuelType.name = :fuelType AND e.removed = false")
    List<EngineEntity> findByFuelType(@Param("fuelType") String fuelType);

    @Query("SELECT e FROM EngineEntity e WHERE e.horsePower BETWEEN :minPower AND :maxPower AND e.removed = false")
    List<EngineEntity> findByPowerRange(@Param("minPower") Double minPower, @Param("maxPower") Double maxPower);

    @Query("SELECT e FROM EngineEntity e WHERE e.displacement BETWEEN :minVolume AND :maxVolume AND e.removed = false")
    List<EngineEntity> findByDisplacementRange(@Param("minVolume") Double minVolume, @Param("maxVolume") Double maxVolume);

    @Query("SELECT e FROM EngineEntity e WHERE e.fuelType.name = :fuelType AND e.horsePower = :power AND e.displacement = :displacement AND e.removed = false")
    Optional<EngineEntity> findByFuelTypeAndHorsePowerAndDisplacement(@Param("fuelType") String fuelType,
                                                                      @Param("power") double power,
                                                                      @Param("displacement") double displacement);
}