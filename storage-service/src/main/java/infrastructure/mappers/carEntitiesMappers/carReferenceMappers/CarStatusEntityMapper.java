package infrastructure.mappers.carEntitiesMappers.carReferenceMappers;

import domain.models.car.types.CarStatus;
import infrastructure.entities.carEntities.referenceCarEntities.CarStatusEntity;
import infrastructure.jpaRepository.carJpaRepositories.referenceCarJpaRepositories.CarStatusJpaRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class CarStatusEntityMapper {

    @Autowired
    protected CarStatusJpaRepository statusRepository;

    public CarStatusEntity toEntity(CarStatus status) {
        if (status == null) {
            return statusRepository.findByNameAndRemovedFalse("UNAVAILABLE")
                    .orElseThrow(() -> new RuntimeException("Default status not found"));
        }
        return statusRepository.findByNameAndRemovedFalse(status.name())
                .orElseThrow(() -> new RuntimeException("Status not found: " + status.name()));
    }

    public CarStatus toDomain(CarStatusEntity entity) {
        if (entity == null) return null;
        return CarStatus.valueOf(entity.getName());
    }
}