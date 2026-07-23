package infrastructure.mappers.carEntitiesMappers.carConfigurationMappers;

import domain.models.car.componentModels.Component;
import domain.models.car.componentModels.ComponentType;
import domain.models.car.Price;
import domain.models.car.CarModel;
import infrastructure.entities.carEntities.configurationCarEntities.componentEntities.ComponentEntity;
import infrastructure.entities.carEntities.configurationCarEntities.componentEntities.ComponentTypeEntity;
import infrastructure.entities.carEntities.referenceCarEntities.CarModelEntity;
import infrastructure.jpaRepository.carJpaRepositories.configurationCarJpaRepositories.componentJpaRepositories.ComponentTypeJpaRepository;
import infrastructure.jpaRepository.carJpaRepositories.referenceCarJpaRepositories.CarModelJpaRepository;
import infrastructure.mappers.carEntitiesMappers.PriceMapper;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {PriceMapper.class})
public abstract class ComponentEntityMapper {

    @Autowired
    protected ComponentTypeJpaRepository componentTypeRepository;

    @Autowired
    protected CarModelJpaRepository carModelRepository;

    @Mapping(target = "type", expression = "java(toComponentTypeEntity(component.getType()))")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "extraCharge", expression = "java(toBigDecimal(component.getExtraCharge()))")
    @Mapping(target = "compatibleModels", expression = "java(toCarModelEntities(component.getCompatibleModels()))")
    @Mapping(target = "removed", constant = "false")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract ComponentEntity toEntity(Component component);

    @Mapping(target = "type", expression = "java(ComponentType.valueOf(entity.getType().getName()))")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "extraCharge", expression = "java(toPrice(entity.getExtraCharge()))")
    @Mapping(target = "compatibleModels", expression = "java(toCarModels(entity.getCompatibleModels()))")
    public abstract Component toDomain(ComponentEntity entity);

    protected ComponentTypeEntity toComponentTypeEntity(ComponentType type) {
        if (type == null) return null;
        return componentTypeRepository.findByNameAndRemovedFalse(type.name())
                .orElseThrow(() -> new RuntimeException("Component type not found: " + type.name()));
    }

    protected BigDecimal toBigDecimal(Price price) {
        return price == null ? null : BigDecimal.valueOf(price.getAmount().doubleValue());
    }

    protected Price toPrice(BigDecimal amount) {
        return amount == null ? null : Price.of(amount.doubleValue(), "RUB");
    }

    protected List<CarModelEntity> toCarModelEntities(Set<CarModel> models) {
        if (models == null) return new ArrayList<>();
        return models.stream()
                .map(model -> carModelRepository.findByNameAndBrand(model.getName(), model.getCarBrand().name())
                        .orElseThrow(() -> new RuntimeException("Car model not found: " + model.getName())))
                .collect(Collectors.toList());
    }

    protected Set<CarModel> toCarModels(List<CarModelEntity> entities) {
        if (entities == null) return new HashSet<>();
        return entities.stream()
                .map(entity -> new CarModel(
                        entity.getId().toString(),
                        entity.getName(),
                        domain.models.car.types.CarBrand.valueOf(entity.getBrand().getName()),
                        entity.getGeneration()
                ))
                .collect(Collectors.toSet());
    }
}