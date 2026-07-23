package infrastructure.mappers.carEntitiesMappers.carReferenceMappers;

import domain.models.car.engine.Engine;
import domain.models.car.engine.EngineDisplacement;
import domain.models.car.engine.EngineFuelType;
import domain.models.car.engine.EnginePower;
import infrastructure.entities.carEntities.technicalCarEntities.engineEntities.EngineEntity;
import infrastructure.entities.carEntities.technicalCarEntities.engineEntities.EngineFuelTypeEntity;
import infrastructure.jpaRepository.carJpaRepositories.technicalCarJpaRepositories.EngineJpaRepositories.EngineFuelTypeJpaRepository;
import infrastructure.jpaRepository.carJpaRepositories.technicalCarJpaRepositories.EngineJpaRepositories.EngineJpaRepository;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

@Mapper(componentModel = "spring")
public abstract class EngineEntityMapper {

    @Autowired
    protected EngineFuelTypeJpaRepository fuelTypeRepository;

    @Autowired
    protected EngineJpaRepository engineJpaRepository;

    public EngineEntity toEntity(Engine engine) {
        if (engine == null) return null;

        return engineJpaRepository.findByFuelTypeAndHorsePowerAndDisplacement(
                engine.getEngineFuelType().name(),
                engine.getEnginePower().getHorsePower(),
                engine.getEngineDisplacement().getLiters()
        ).orElseGet(() -> {
            EngineEntity entity = new EngineEntity();
            entity.setId(UUID.randomUUID());
            entity.setFuelType(toFuelTypeEntity(engine.getEngineFuelType()));
            entity.setDisplacement(engine.getEngineDisplacement().getLiters());
            entity.setHorsePower(engine.getEnginePower().getHorsePower());
            entity.setDescription(engine.getDescription());
            entity.setRemoved(false);
            return entity;
        });
    }

    public Engine toDomain(EngineEntity entity) {
        if (entity == null) return null;
        return new Engine(
                entity.getId().toString(),
                EngineFuelType.valueOf(entity.getFuelType().getName()),
                EngineDisplacement.of(entity.getDisplacement()),
                EnginePower.of(entity.getHorsePower())
        );
    }

    protected EngineFuelTypeEntity toFuelTypeEntity(EngineFuelType fuelType) {
        if (fuelType == null) return null;
        return fuelTypeRepository.findByNameAndRemovedFalse(fuelType.name())
                .orElseThrow(() -> new RuntimeException("Fuel type not found: " + fuelType.name()));
    }
}