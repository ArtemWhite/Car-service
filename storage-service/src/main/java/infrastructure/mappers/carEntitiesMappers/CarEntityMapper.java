package infrastructure.mappers.carEntitiesMappers;

import domain.models.car.Car;
import domain.models.car.CarConfiguration;
import domain.models.car.types.*;
import infrastructure.entities.carEntities.CarEntity;
import infrastructure.mappers.carEntitiesMappers.carConfigurationMappers.CarConfigurationEntityMapper;
import infrastructure.mappers.carEntitiesMappers.carReferenceMappers.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class CarEntityMapper {

    @Autowired
    protected CarBrandMapper brandMapper;

    @Autowired
    protected CarModelEntityMapper modelMapper;

    @Autowired
    protected CarBodEntityMapper bodyMapper;

    @Autowired
    protected CarColorEntityMapper colorMapper;

    @Autowired
    protected DriveTypeEntityMapper driveTypeMapper;

    @Autowired
    protected CarStatusEntityMapper statusMapper;

    @Autowired
    protected EngineEntityMapper engineMapper;

    @Autowired
    protected TransmissionEntityMapper transmissionMapper;

    @Autowired
    protected PriceMapper priceMapper;

    @Autowired
    protected CarConfigurationEntityMapper configurationMapper;

    @Mapping(target = "id", source = "carId")
    @Mapping(target = "brand", expression = "java(brandMapper.toEntity(car.getBrand()))")
    @Mapping(target = "model", expression = "java(modelMapper.toEntity(car.getModel()))")
    @Mapping(target = "body", expression = "java(bodyMapper.toEntity(car.getBody()))")
    @Mapping(target = "color", expression = "java(colorMapper.toEntity(car.getColor()))")
    @Mapping(target = "driveType", expression = "java(driveTypeMapper.toEntity(car.getDriveType()))")
    @Mapping(target = "status", expression = "java(statusMapper.toEntity(car.getCarStatus()))")
    @Mapping(target = "engine", expression = "java(engineMapper.toEntity(car.getEngine()))")
    @Mapping(target = "transmission", expression = "java(transmissionMapper.toEntity(car.getTransmission()))")
    @Mapping(target = "price", expression = "java(priceMapper.toBigDecimal(car.getPrice()))")
    @Mapping(target = "configuration", expression = "java(configurationMapper.toEntity(car.getCarConfiguration()))")
    @Mapping(target = "carInfo", source = "carInfo")
    @Mapping(target = "removed", constant = "false")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract CarEntity toEntity(Car car);

    public Car toDomain(CarEntity entity) {
        if (entity == null) return null;

        Car car = new Car(
                entity.getId().toString(),
                CarBrand.valueOf(entity.getBrand().getName()),
                modelMapper.toDomain(entity.getModel()),
                bodyMapper.toDomain(entity.getBody()),
                colorMapper.toDomain(entity.getColor()),
                driveTypeMapper.toDomain(entity.getDriveType()),
                engineMapper.toDomain(entity.getEngine()),
                transmissionMapper.toDomain(entity.getTransmission()),
                priceMapper.toDomain(entity.getPrice())
        );

        if (entity.getStatus() != null) {
            CarStatus status = CarStatus.valueOf(entity.getStatus().getName());
            car.restoreStatus(status);
        }

        if (entity.getConfiguration() != null) {
            CarConfiguration config = configurationMapper.toDomain(entity.getConfiguration());
            car.applyConfiguration(config);
        }

        return car;
    }
}