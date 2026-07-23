package infrastructure.mappers.carEntitiesMappers.carReferenceMappers;

import domain.models.car.types.CarColor;
import infrastructure.entities.carEntities.referenceCarEntities.CarColorEntity;
import infrastructure.jpaRepository.carJpaRepositories.referenceCarJpaRepositories.CarColorJpaRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class CarColorEntityMapper {

    @Autowired
    protected CarColorJpaRepository colorRepository;

    public CarColorEntity toEntity(CarColor color) {
        if (color == null) return null;
        return colorRepository.findByNameAndRemovedFalse(color.name())
                .orElseThrow(() -> new RuntimeException("Color not found: " + color.name()));
    }

    public CarColor toDomain(CarColorEntity entity) {
        if (entity == null) return null;
        return CarColor.valueOf(entity.getName());
    }
}