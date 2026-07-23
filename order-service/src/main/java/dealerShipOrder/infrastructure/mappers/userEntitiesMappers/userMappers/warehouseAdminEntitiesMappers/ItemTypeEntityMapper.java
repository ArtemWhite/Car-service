package dealerShipOrder.infrastructure.mappers.userEntitiesMappers.userMappers.warehouseAdminEntitiesMappers;

import dealerShipOrder.domain.models.users.warehouseAdmin.ItemType;
import dealerShipOrder.infrastructure.entities.userEntities.systemAdminEntities.ItemTypeEntity;
import dealerShipOrder.infrastructure.jpaRepository.userJpaRepositories.warehouseAdminJpaRepositories.ItemTypeJpaRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class ItemTypeEntityMapper {

    @Autowired
    protected ItemTypeJpaRepository itemTypeRepository;

    public ItemTypeEntity toEntity(ItemType type) {
        if (type == null) return null;
        return itemTypeRepository.findByName(type.name())
                .orElseThrow(() -> new RuntimeException("Item type not found: " + type.name()));
    }

    public ItemType toDomain(ItemTypeEntity entity) {
        if (entity == null) return null;
        return ItemType.valueOf(entity.getName());
    }
}