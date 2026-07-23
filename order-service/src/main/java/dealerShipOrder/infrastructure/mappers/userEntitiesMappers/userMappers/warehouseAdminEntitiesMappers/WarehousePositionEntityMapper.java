package dealerShipOrder.infrastructure.mappers.userEntitiesMappers.userMappers.warehouseAdminEntitiesMappers;

import dealerShipOrder.domain.models.users.warehouseAdmin.WarehousePosition;
import dealerShipOrder.infrastructure.entities.userEntities.warehouseAdminEntities.WarehousePositionEntity;
import dealerShipOrder.infrastructure.jpaRepository.userJpaRepositories.warehouseAdminJpaRepositories.WarehousePositionJpaRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class WarehousePositionEntityMapper {

    @Autowired
    protected WarehousePositionJpaRepository positionRepository;

    public WarehousePositionEntity toEntity(WarehousePosition position) {
        if (position == null) return null;
        return positionRepository.findByName(position.name())
                .orElseThrow(() -> new RuntimeException("Warehouse position not found: " + position.name()));
    }

    public WarehousePosition toDomain(WarehousePositionEntity entity) {
        if (entity == null) return null;
        return WarehousePosition.valueOf(entity.getName());
    }
}