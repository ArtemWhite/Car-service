package infrastructure.mappers.carEntitiesMappers.carReferenceMappers;

import domain.models.car.CarModel;
import domain.models.car.types.CarBrand;

import infrastructure.entities.carEntities.referenceCarEntities.CarModelEntity;
import infrastructure.mappers.carEntitiesMappers.CarBrandMapper;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", uses = {CarBrandMapper.class})
public abstract class CarModelEntityMapper {

    @Autowired
    protected CarBrandMapper brandMapper;

    @Mapping(target = "brand", expression = "java(brandMapper.toEntity(model.getCarBrand()))")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "generation", source = "generation")
    @Mapping(target = "fullName", source = "fullName")
    @Mapping(target = "removed", constant = "false")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract CarModelEntity toEntity(CarModel model);

    public CarModel toDomain(CarModelEntity entity) {
        if (entity == null) return null;
        return new CarModel(
                entity.getId().toString(),
                entity.getName(),
                CarBrand.valueOf(entity.getBrand().getName()),
                entity.getGeneration()
        );
    }
}