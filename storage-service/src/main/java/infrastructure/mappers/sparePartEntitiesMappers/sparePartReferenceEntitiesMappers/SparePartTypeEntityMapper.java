package infrastructure.mappers.sparePartEntitiesMappers.sparePartReferenceEntitiesMappers;

import domain.models.sparePart.SpareType;
import infrastructure.entities.sparePartEntities.referenceSparePartEntities.SparePartTypeEntity;
import infrastructure.jpaRepository.sparePartJpaRepositories.referenceSparePartJpaRepositories.SpareTypeReferenceJpaRepository;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class SparePartTypeEntityMapper {

    @Autowired
    protected SpareTypeReferenceJpaRepository spareTypeRepository;

    public SparePartTypeEntity toEntity(SpareType type) {
        if (type == null) return null;
        return spareTypeRepository.findByName(type.name())
                .orElseThrow(() -> new RuntimeException("Spare part type not found: " + type.name()));
    }

    public SpareType toDomain(SparePartTypeEntity entity) {
        if (entity == null) return null;
        return SpareType.valueOf(entity.getName());
    }
}