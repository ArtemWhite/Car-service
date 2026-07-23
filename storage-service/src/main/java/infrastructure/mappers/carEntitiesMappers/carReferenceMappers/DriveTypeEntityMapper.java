package infrastructure.mappers.carEntitiesMappers.carReferenceMappers;

import domain.models.car.types.DriveType;
import infrastructure.entities.carEntities.referenceCarEntities.DriveTypeEntity;
import infrastructure.jpaRepository.carJpaRepositories.referenceCarJpaRepositories.DriveTypeJpaRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class DriveTypeEntityMapper {

    @Autowired
    protected DriveTypeJpaRepository driveTypeRepository;

    public DriveTypeEntity toEntity(DriveType driveType) {
        if (driveType == null) return null;
        return driveTypeRepository.findByNameAndRemovedFalse(driveType.name())
                .orElseThrow(() -> new RuntimeException("Drive type not found: " + driveType.name()));
    }

    public DriveType toDomain(DriveTypeEntity entity) {
        if (entity == null) return null;
        return DriveType.valueOf(entity.getName());
    }
}