package infrastructure.mappers.carEntitiesMappers.carConfigurationMappers;

import domain.models.car.CarConfiguration;
import domain.models.car.CarModel;
import domain.models.car.Price;
import domain.models.car.componentModels.Component;
import domain.models.car.componentModels.ComponentType;
import infrastructure.entities.carEntities.configurationCarEntities.CarConfigurationEntity;
import infrastructure.entities.carEntities.configurationCarEntities.componentEntities.ComponentEntity;
import infrastructure.entities.carEntities.referenceCarEntities.CarBrandEntity;
import infrastructure.entities.carEntities.referenceCarEntities.CarModelEntity;
import infrastructure.jpaRepository.carJpaRepositories.configurationCarJpaRepositories.componentJpaRepositories.ComponentJpaRepository;
import infrastructure.jpaRepository.carJpaRepositories.configurationCarJpaRepositories.componentJpaRepositories.ComponentTypeJpaRepository;
import infrastructure.jpaRepository.carJpaRepositories.referenceCarJpaRepositories.CarBrandJpaRepository;
import infrastructure.jpaRepository.carJpaRepositories.referenceCarJpaRepositories.CarModelJpaRepository;
import infrastructure.mappers.carEntitiesMappers.PriceMapper;
import infrastructure.mappers.carEntitiesMappers.carReferenceMappers.CarModelEntityMapper;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.*;

@Mapper(
        componentModel = "spring",
        uses = {
                CarModelEntityMapper.class,
                ComponentEntityMapper.class,
                PriceMapper.class
        }
)
public abstract class CarConfigurationEntityMapper {

    @Autowired
    protected CarModelJpaRepository carModelRepository;

    @Autowired
    protected CarBrandJpaRepository carBrandRepository;

    @Autowired
    protected ComponentJpaRepository componentRepository;

    @Autowired
    protected ComponentTypeJpaRepository componentTypeRepository;

    @Autowired
    protected ComponentEntityMapper componentMapper;

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "model", expression = "java(toCarModelEntity(configuration.getModel()))")
    @Mapping(target = "basePrice", expression = "java(toBigDecimal(configuration.getBasePrice()))")
    @Mapping(target = "baseComponents", expression = "java(mapComponentsToList(configuration.getBaseComponents()))")
    @Mapping(target = "removed", constant = "false")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract CarConfigurationEntity toEntity(CarConfiguration configuration);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "model", expression = "java(toCarModel(entity.getModel()))")
    @Mapping(target = "basePrice", expression = "java(toPrice(entity.getBasePrice()))")
    @Mapping(target = "baseComponents", expression = "java(mapComponentsToMap(entity.getBaseComponents()))")
    public abstract CarConfiguration toDomain(CarConfigurationEntity entity);

    protected List<ComponentEntity> mapComponentsToList(Map<ComponentType, Component> components) {
        if (components == null || components.isEmpty()) return new ArrayList<>();
        return components.values().stream()
                .map(componentMapper::toEntity)
                .toList();
    }

    protected Map<ComponentType, Component> mapComponentsToMap(List<ComponentEntity> entities) {
        if (entities == null || entities.isEmpty()) return new HashMap<>();
        Map<ComponentType, Component> result = new HashMap<>();
        for (ComponentEntity entity : entities) {
            Component component = componentMapper.toDomain(entity);
            result.put(component.getType(), component);
        }
        return result;
    }

    protected CarModelEntity toCarModelEntity(CarModel model) {
        if (model == null) return null;

        CarBrandEntity brandEntity = carBrandRepository.findByNameAndRemovedFalse(model.getCarBrand().name())
                .orElseThrow(() -> new RuntimeException("Brand not found: " + model.getCarBrand().name()));

        return carModelRepository.findByNameAndBrand(model.getName(), model.getCarBrand().name())
                .orElseGet(() -> {
                    CarModelEntity newModel = new CarModelEntity();
                    newModel.setId(model.getId() != null ? UUID.fromString(model.getId()) : UUID.randomUUID());
                    newModel.setName(model.getName());
                    newModel.setBrand(brandEntity);
                    newModel.setGeneration(model.getGeneration());
                    newModel.setFullName(model.getFullName());
                    newModel.setRemoved(false);
                    newModel.setCreatedAt(new Date().toInstant());
                    newModel.setUpdatedAt(new Date().toInstant());
                    return carModelRepository.save(newModel);
                });
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

    protected BigDecimal toBigDecimal(Price price) {
        return price == null ? null : BigDecimal.valueOf(price.getAmount().doubleValue());
    }

    protected Price toPrice(BigDecimal amount) {
        return amount == null ? null : Price.of(amount.doubleValue(), "RUB");
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "model", expression = "java(toCarModelEntity(configuration.getModel()))")
    @Mapping(target = "basePrice", expression = "java(toBigDecimal(configuration.getBasePrice()))")
    @Mapping(target = "baseComponents", expression = "java(mapComponentsToList(configuration.getBaseComponents()))")
    @Mapping(target = "removed", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract void updateEntity(@MappingTarget CarConfigurationEntity entity,
                                      CarConfiguration configuration);

    @AfterMapping
    protected void updateBaseComponents(@MappingTarget CarConfigurationEntity entity,
                                        CarConfiguration configuration) {
        if (configuration.getBaseComponents() != null && !configuration.getBaseComponents().isEmpty()) {
            List<ComponentEntity> currentComponents = entity.getBaseComponents();

            if (currentComponents == null) {
                currentComponents = new ArrayList<>();
                entity.setBaseComponents(currentComponents);
            }

            if (!(currentComponents instanceof ArrayList)) {
                currentComponents = new ArrayList<>(currentComponents);
                entity.setBaseComponents(currentComponents);
            }

            currentComponents.clear();
            currentComponents.addAll(mapComponentsToList(configuration.getBaseComponents()));
        } else if (configuration.getBaseComponents() != null && configuration.getBaseComponents().isEmpty()) {
            List<ComponentEntity> currentComponents = entity.getBaseComponents();
            if (currentComponents != null && !(currentComponents instanceof ArrayList)) {
                entity.setBaseComponents(new ArrayList<>());
            } else if (currentComponents != null) {
                currentComponents.clear();
            }
        }
    }
}