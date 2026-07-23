package infrastructure.adapters.carAdapters.carReferencesAdapters;

import domain.models.car.Car;
import domain.models.car.CarModel;
import domain.models.car.Price;
import domain.models.car.engine.Engine;
import domain.models.car.engine.EngineFuelType;
import domain.models.car.transmission.Transmission;
import domain.models.car.transmission.TransmissionType;
import domain.models.car.types.*;
import infrastructure.entities.carEntities.technicalCarEntities.engineEntities.EngineEntity;
import infrastructure.entities.carEntities.technicalCarEntities.transmissionEntity.TransmissionEntity;
import infrastructure.jpaRepository.carJpaRepositories.CarJpaRepository;
import infrastructure.jpaRepository.carJpaRepositories.referenceCarJpaRepositories.CarModelJpaRepository;
import infrastructure.jpaRepository.carJpaRepositories.technicalCarJpaRepositories.EngineJpaRepositories.EngineJpaRepository;
import infrastructure.jpaRepository.carJpaRepositories.technicalCarJpaRepositories.TransmissionJpaRepositories.TransmissionJpaRepository;
import infrastructure.mappers.carEntitiesMappers.CarEntityMapper;
import infrastructure.mappers.carEntitiesMappers.carReferenceMappers.CarModelEntityMapper;
import infrastructure.mappers.carEntitiesMappers.carReferenceMappers.EngineEntityMapper;
import infrastructure.mappers.carEntitiesMappers.carReferenceMappers.TransmissionEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CarFilterAdapter {

    private final CarJpaRepository jpaRepository;
    private final CarModelJpaRepository carModelJpaRepository;
    private final EngineJpaRepository engineJpaRepository;
    private final TransmissionJpaRepository transmissionJpaRepository;
    private final CarEntityMapper mapper;
    private final CarModelEntityMapper carModelMapper;
    private final EngineEntityMapper engineMapper;
    private final TransmissionEntityMapper transmissionMapper;

    public List<Car> findCarsByFilters(CarBrand brand, CarModel model,
                                       CarBody body, CarColor color,
                                       DriveType driveType, Price minPrice, Price maxPrice) {
        String brandName = brand != null ? brand.name() : null;
        String modelName = model != null ? model.getName() : null;
        String bodyName = body != null ? body.name() : null;
        String colorName = color != null ? color.name() : null;
        String driveTypeName = driveType != null ? driveType.name() : null;
        BigDecimal min = minPrice != null ? BigDecimal.valueOf(minPrice.getAmount().doubleValue()) : null;
        BigDecimal max = maxPrice != null ? BigDecimal.valueOf(maxPrice.getAmount().doubleValue()) : null;

        return jpaRepository.findByFilters(brandName, modelName, bodyName, colorName, driveTypeName, min, max).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public Optional<CarModel> findModelById(String modelId) {
        try {
            UUID uuid = UUID.fromString(modelId);
            return carModelJpaRepository.findByIdAndRemovedFalse(uuid)
                    .map(carModelMapper::toDomain);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public Optional<CarModel> findModelByNameAndBrand(String modelName, CarBrand brand) {
        return carModelJpaRepository.findByNameAndBrand(modelName, brand.name())
                .map(carModelMapper::toDomain);
    }

    public Optional<Engine> findEngineByFuelTypePowerAndDisplacement(EngineFuelType fuelType, double power, double displacement) {
        return engineJpaRepository.findByFuelTypeAndHorsePowerAndDisplacement(fuelType.name(), power, displacement)
                .map(engineMapper::toDomain);
    }

    public Optional<Transmission> findTransmissionByTypeAndGears(TransmissionType type, int gears) {
        return transmissionJpaRepository.findByTypeAndGears(type.name(), gears)
                .map(transmissionMapper::toDomain);
    }

    public Engine saveEngine(Engine engine) {
        EngineEntity entity = engineMapper.toEntity(engine);
        EngineEntity saved = engineJpaRepository.save(entity);
        return engineMapper.toDomain(saved);
    }

    public Transmission saveTransmission(Transmission transmission) {
        TransmissionEntity entity = transmissionMapper.toEntity(transmission);

        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID());
        }

        TransmissionEntity saved = transmissionJpaRepository.save(entity);
        Transmission result = transmissionMapper.toDomain(saved);

        return result;
    }
}