package infrastructure.mappers.sparePartEntitiesMappers;

import domain.models.car.CarModel;
import domain.models.car.Price;
import domain.models.sparePart.SparePart;
import domain.models.sparePart.SpareType;
import infrastructure.entities.carEntities.referenceCarEntities.CarModelEntity;
import infrastructure.entities.sparePartEntities.SparePartEntity;
import infrastructure.entities.sparePartEntities.referenceSparePartEntities.SparePartCompatibilityEntity;
import infrastructure.entities.sparePartEntities.referenceSparePartEntities.SparePartTypeEntity;
import infrastructure.jpaRepository.carJpaRepositories.referenceCarJpaRepositories.CarModelJpaRepository;
import infrastructure.jpaRepository.sparePartJpaRepositories.referenceSparePartJpaRepositories.SpareTypeReferenceJpaRepository;
import infrastructure.mappers.carEntitiesMappers.carReferenceMappers.CarModelEntityMapper;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class SparePartEntityMapper {

    @Autowired
    protected SpareTypeReferenceJpaRepository spareTypeRepository;

    @Autowired
    protected CarModelJpaRepository carModelRepository;

    @Autowired
    protected CarModelEntityMapper carModelMapper;

    public void updateEntity(SparePartEntity entity, SparePart domain) {
        if (domain == null) return;

        entity.setType(toSparePartTypeEntity(domain.getType()));
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setPrice(BigDecimal.valueOf(domain.getPrice().getAmount().doubleValue()));
        entity.setCurrency(domain.getPrice().getCurrency().getCurrencyCode());
        entity.setUpdatedAt(Instant.now());

        entity.getCompatibilities().clear();
        for (CarModel model : domain.getCompatibles()) {
            SparePartCompatibilityEntity compatibility = new SparePartCompatibilityEntity();
            compatibility.setId(UUID.randomUUID());
            compatibility.setSparePart(entity);
            compatibility.setCarModel(toCarModelEntity(model));
            compatibility.setCreatedAt(Instant.now());
            compatibility.setUpdatedAt(Instant.now());
            compatibility.setRemoved(false);
            entity.getCompatibilities().add(compatibility);
        }
    }


    public SparePartEntity toEntity(SparePart sparePart) {
        if (sparePart == null) return null;

        SparePartEntity entity = new SparePartEntity();
        entity.setId(toUuid(sparePart.getId()));
        entity.setType(toSparePartTypeEntity(sparePart.getType()));
        entity.setName(sparePart.getName());
        entity.setDescription(sparePart.getDescription());
        entity.setPrice(BigDecimal.valueOf(sparePart.getPrice().getAmount().doubleValue()));
        entity.setCurrency(sparePart.getPrice().getCurrency().toString());

        entity.setCompatibilities(toCompatibilityEntities(sparePart.getCompatibles(), entity));

        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        entity.setRemoved(false);

        entity.setManufacturer(null);
        entity.setPartNumber(null);
        entity.setStockQuantity(0);
        entity.setSectionId(null);
        entity.setLocation(null);

        return entity;
    }

    public SparePart toDomain(SparePartEntity entity) {
        if (entity == null) return null;

        Set<CarModel> compatibleModels = toCarModels(entity.getCompatibilities());

        Price price = Price.of(
                entity.getPrice().doubleValue(),
                entity.getCurrency() != null ? entity.getCurrency() : "RUB"
        );

        return SparePart.builder()
                .id(entity.getId().toString())
                .type(toSpareType(entity.getType()))
                .name(entity.getName())
                .description(entity.getDescription())
                .price(price)
                .compatibles(compatibleModels)
                .manufacturer(entity.getManufacturer())
                .partNumber(entity.getPartNumber())
                .build();
    }

    protected java.util.List<SparePartCompatibilityEntity> toCompatibilityEntities(
            Set<CarModel> models, SparePartEntity sparePartEntity) {

        System.out.println("=== toCompatibilityEntities called with " + (models != null ? models.size() : 0) + " models ===");

        if (models == null || models.isEmpty()) {
            System.out.println("Models is null or empty");
            return new java.util.ArrayList<>();
        }

        for (CarModel model : models) {
            System.out.println("Processing model: " + model.getId() + " - " + model.getName());
        }

        return models.stream()
                .map(model -> {
                    try {
                        CarModelEntity carModelEntity = toCarModelEntity(model);
                        System.out.println("Found CarModelEntity: " + carModelEntity.getId() + " - " + carModelEntity.getName());

                        SparePartCompatibilityEntity compatibility = new SparePartCompatibilityEntity();
                        compatibility.setId(UUID.randomUUID());
                        compatibility.setSparePart(sparePartEntity);
                        compatibility.setCarModel(carModelEntity);
                        compatibility.setCreatedAt(Instant.now());
                        compatibility.setUpdatedAt(Instant.now());
                        compatibility.setRemoved(false);
                        return compatibility;
                    } catch (Exception e) {
                        System.err.println("Error mapping model " + model.getId() + ": " + e.getMessage());
                        throw e;
                    }
                })
                .collect(Collectors.toList());
    }

    protected Set<CarModel> toCarModels(java.util.List<SparePartCompatibilityEntity> compatibilities) {
        if (compatibilities == null || compatibilities.isEmpty()) return new HashSet<>();

        return compatibilities.stream()
                .filter(comp -> comp.getCarModel() != null)
                .map(comp -> toCarModel(comp.getCarModel()))
                .collect(Collectors.toSet());
    }

    protected CarModel toCarModel(CarModelEntity entity) {
        if (entity == null) return null;

        return new CarModel(
                entity.getId().toString(),
                entity.getName(),
                domain.models.car.types.CarBrand.valueOf(entity.getBrand().getName()),
                entity.getGeneration()
        );
    }

    protected CarModelEntity toCarModelEntity(CarModel model) {
        if (model == null) return null;

        try {
            UUID uuid = UUID.fromString(model.getId());
            return carModelRepository.findById(uuid)
                    .orElseThrow(() -> new RuntimeException("Car model not found by id: " + model.getId()));
        } catch (IllegalArgumentException e) {
            return carModelRepository.findByNameAndBrand(model.getName(), model.getCarBrand().name())
                    .orElseThrow(() -> new RuntimeException("Car model not found: " + model.getName()));
        }
    }

    protected SparePartTypeEntity toSparePartTypeEntity(SpareType type) {
        if (type == null) return null;
        return spareTypeRepository.findByName(type.name())
                .orElseThrow(() -> new RuntimeException("Spare part type not found: " + type.name()));
    }

    protected SpareType toSpareType(SparePartTypeEntity entity) {
        if (entity == null) return null;
        return SpareType.valueOf(entity.getName());
    }

    protected String toUuid(UUID uuid) {
        return uuid == null ? null : uuid.toString();
    }

    protected UUID toUuid(String id) {
        if (id == null) return null;
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    protected Instant toInstant(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.atZone(ZoneId.systemDefault()).toInstant();
    }

    protected LocalDateTime toLocalDateTime(Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
}