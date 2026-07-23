package infrastructure.mappers.carEntitiesMappers;

import domain.models.car.types.CarBrand;
import infrastructure.entities.carEntities.referenceCarEntities.CarBrandEntity;
import infrastructure.jpaRepository.carJpaRepositories.referenceCarJpaRepositories.CarBrandJpaRepository;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class CarBrandMapper {

    @Autowired
    protected CarBrandJpaRepository carBrandRepository;

    public CarBrandEntity toEntity(CarBrand brand) {
        if (brand == null) return null;
        return carBrandRepository.findByNameAndRemovedFalse(brand.name())
                .orElseThrow(() -> new RuntimeException("Car brand not found: " + brand.name()));
    }

    public CarBrand toDomain(CarBrandEntity entity) {
        if (entity == null) return null;
        return CarBrand.valueOf(entity.getName());
    }
}