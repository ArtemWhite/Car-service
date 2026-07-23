package infrastructure.adapters.sparePartAdapters.sparePartReferencesAdapters;

import domain.models.car.CarModel;
import domain.models.sparePart.SparePart;
import infrastructure.entities.carEntities.referenceCarEntities.CarModelEntity;
import infrastructure.entities.sparePartEntities.SparePartEntity;
import infrastructure.entities.sparePartEntities.referenceSparePartEntities.SparePartCompatibilityEntity;
import infrastructure.entities.sparePartEntities.referenceSparePartEntities.SparePartTypeEntity;
import infrastructure.jpaRepository.sparePartJpaRepositories.SparePartJpaRepository;
import infrastructure.mappers.sparePartEntitiesMappers.SparePartEntityMapper;
import infrastructure.mappers.carEntitiesMappers.carReferenceMappers.CarModelEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SparePartBaseRepositoryAdapter {

    private final SparePartJpaRepository jpaRepository;
    private final SparePartEntityMapper mapper;
    private final CarModelEntityMapper carModelEntityMapper;
    private final EntityManager entityManager;

    public SparePart save(SparePart sparePart) {
        UUID uuid = toUuid(sparePart.getId());
        Optional<SparePartEntity> existing = jpaRepository.findSparePartByIdAndRemovedFalse(uuid);

        if (existing.isPresent()) {
            SparePartEntity entity = existing.get();

            entity.setType(toSparePartTypeEntity(sparePart.getType()));
            entity.setName(sparePart.getName());
            entity.setDescription(sparePart.getDescription());
            entity.setPrice(BigDecimal.valueOf(sparePart.getPrice().getAmount().doubleValue()));
            entity.setCurrency(sparePart.getPrice().getCurrency().getCurrencyCode());
            entity.setUpdatedAt(Instant.now());

            entity.getCompatibilities().clear();

            for (CarModel model : sparePart.getCompatibles()) {
                SparePartCompatibilityEntity compatibility = new SparePartCompatibilityEntity();
                compatibility.setId(UUID.randomUUID());
                compatibility.setSparePart(entity);
                compatibility.setCarModel(toCarModelEntity(model));
                compatibility.setCreatedAt(Instant.now());
                compatibility.setUpdatedAt(Instant.now());
                compatibility.setRemoved(false);
                entity.getCompatibilities().add(compatibility);
            }

            SparePartEntity saved = jpaRepository.saveAndFlush(entity);
            entityManager.refresh(saved);

            return mapper.toDomain(saved);
        } else {
            SparePartEntity entity = mapper.toEntity(sparePart);
            SparePartEntity saved = jpaRepository.save(entity);
            return mapper.toDomain(saved);
        }
    }

    public Optional<SparePart> findById(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            return jpaRepository.findSparePartByIdAndRemovedFalse(uuid)
                    .map(mapper::toDomain);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public List<SparePart> findAll() {
        return jpaRepository.findAllSparePartsByRemovedFalse().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public void delete(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            jpaRepository.softDelete(uuid);
        } catch (IllegalArgumentException e) {

        }
    }

    public boolean existsById(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            return jpaRepository.existsById(uuid);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private UUID toUuid(String id) {
        if (id == null) return null;
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private SparePartTypeEntity toSparePartTypeEntity(domain.models.sparePart.SpareType type) {
        if (type == null) return null;
        return jpaRepository.findSpareTypeByName(type.name())
                .orElseThrow(() -> new RuntimeException("Spare part type not found: " + type.name()));
    }

    private CarModelEntity toCarModelEntity(CarModel model) {
        if (model == null) return null;
        return carModelEntityMapper.toEntity(model);
    }
}