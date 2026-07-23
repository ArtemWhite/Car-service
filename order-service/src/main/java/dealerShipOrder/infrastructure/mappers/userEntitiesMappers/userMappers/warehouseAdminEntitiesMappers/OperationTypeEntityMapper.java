package dealerShipOrder.infrastructure.mappers.userEntitiesMappers.userMappers.warehouseAdminEntitiesMappers;

import dealerShipOrder.domain.models.users.warehouseAdmin.OperationType;
import dealerShipOrder.infrastructure.entities.userEntities.warehouseAdminEntities.OperationTypeEntity;
import dealerShipOrder.infrastructure.jpaRepository.userJpaRepositories.warehouseAdminJpaRepositories.OperationTypeJpaRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class OperationTypeEntityMapper {

    @Autowired
    protected OperationTypeJpaRepository operationTypeRepository;

    public OperationTypeEntity toEntity(OperationType type) {
        if (type == null) return null;
        return operationTypeRepository.findByName(type.name())
                .orElseThrow(() -> new RuntimeException("Operation type not found: " + type.name()));
    }

    public OperationType toDomain(OperationTypeEntity entity) {
        if (entity == null) return null;
        return OperationType.valueOf(entity.getName());
    }
}