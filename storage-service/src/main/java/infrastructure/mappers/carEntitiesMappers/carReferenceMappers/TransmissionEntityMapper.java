package infrastructure.mappers.carEntitiesMappers.carReferenceMappers;

import domain.models.car.transmission.Transmission;
import domain.models.car.transmission.TransmissionType;
import infrastructure.entities.carEntities.technicalCarEntities.transmissionEntity.TransmissionEntity;
import infrastructure.entities.carEntities.technicalCarEntities.transmissionEntity.TransmissionTypeEntity;
import infrastructure.jpaRepository.carJpaRepositories.technicalCarJpaRepositories.TransmissionJpaRepositories.TransmissionJpaRepository;
import infrastructure.jpaRepository.carJpaRepositories.technicalCarJpaRepositories.TransmissionJpaRepositories.TransmissionTypeJpaRepository;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

@Mapper(componentModel = "spring")
public abstract class TransmissionEntityMapper {

    @Autowired
    protected TransmissionTypeJpaRepository transmissionTypeRepository;

    @Autowired
    protected TransmissionJpaRepository transmissionJpaRepository;

    public TransmissionEntity toEntity(Transmission transmission) {
        if (transmission == null) return null;

        TransmissionEntity entity = new TransmissionEntity();

        if (transmission.getId() != null) {
            entity.setId(UUID.fromString(transmission.getId()));
        }

        entity.setType(toTransmissionTypeEntity(transmission.getTransmissionType()));
        entity.setGears(transmission.getGears());
        entity.setFullName(transmission.getFullName());
        entity.setRemoved(false);

        return entity;
    }

    public Transmission toDomain(TransmissionEntity entity) {
        if (entity == null) return null;

        Transmission transmission = new Transmission(
                TransmissionType.valueOf(entity.getType().getName()),
                entity.getGears()
        );

        if (entity.getId() != null) {
            transmission.setId(entity.getId().toString());
        }

        return transmission;
    }

    protected TransmissionTypeEntity toTransmissionTypeEntity(TransmissionType type) {
        if (type == null) return null;
        return transmissionTypeRepository.findByNameAndRemovedFalse(type.name())
                .orElseThrow(() -> new RuntimeException("Transmission type not found: " + type.name()));
    }
}