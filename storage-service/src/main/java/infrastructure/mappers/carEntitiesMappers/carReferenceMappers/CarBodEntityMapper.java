package infrastructure.mappers.carEntitiesMappers.carReferenceMappers;

import domain.models.car.types.CarBody;
import infrastructure.entities.carEntities.referenceCarEntities.CarBodyEntity;
import infrastructure.jpaRepository.carJpaRepositories.referenceCarJpaRepositories.CarBodyJpaRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class CarBodEntityMapper {

    @Autowired
    protected CarBodyJpaRepository bodyRepository;

    public CarBodyEntity toEntity(CarBody body) {
        if (body == null) return null;
        return bodyRepository.findByNameAndRemovedFalse(body.name())
                .orElseThrow(() -> new RuntimeException("Body type not found: " + body.name()));
    }

    public CarBody toDomain(CarBodyEntity entity) {
        if (entity == null) return null;
        return CarBody.valueOf(entity.getName());
    }
}