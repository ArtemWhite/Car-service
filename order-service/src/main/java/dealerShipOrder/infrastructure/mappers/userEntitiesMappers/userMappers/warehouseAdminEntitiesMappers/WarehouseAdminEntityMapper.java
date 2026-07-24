package dealerShipOrder.infrastructure.mappers.userEntitiesMappers.userMappers.warehouseAdminEntitiesMappers;

import dealerShipOrder.domain.models.users.User;
import dealerShipOrder.domain.models.users.warehouseAdmin.WarehouseAdmin;
import dealerShipOrder.domain.models.users.warehouseAdmin.WarehousePosition;
import dealerShipOrder.infrastructure.entities.userEntities.UserEntity;
import dealerShipOrder.infrastructure.entities.userEntities.warehouseAdminEntities.WarehouseAdminEntity;
import dealerShipOrder.infrastructure.entities.userEntities.warehouseAdminEntities.WarehousePositionEntity;
import dealerShipOrder.infrastructure.jpaRepository.userJpaRepositories.warehouseAdminJpaRepositories.WarehousePositionJpaRepository;
import dealerShipOrder.infrastructure.mappers.userEntitiesMappers.userMappers.BaseUserEntityMapper;

import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;

@Mapper(componentModel = "spring")
public abstract class WarehouseAdminEntityMapper extends BaseUserEntityMapper {

    @Autowired
    protected WarehousePositionJpaRepository warehousePositionRepository;

    public WarehouseAdminEntity toEntity(WarehouseAdmin admin) {
        if (admin == null) return null;

        WarehouseAdminEntity entity = new WarehouseAdminEntity();
        fillBaseUserEntity(entity, admin);
        entity.setPosition(toWarehousePositionEntity(admin.getPosition()));
        entity.setOnDuty(admin.isOnDuty());
        entity.setManagedSectionIds(new HashSet<>(admin.getManagedSectionIds()));
        return entity;
    }

    public WarehouseAdmin toDomain(WarehouseAdminEntity entity) {
        if (entity == null) return null;

        WarehouseAdmin admin = new WarehouseAdmin(
                entity.getFirstName(),
                entity.getLastName(),
                entity.getMiddleName(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getPasswordHash(),
                entity.getId().toString()
        );

        restorePosition(admin, entity.getPosition());
        restoreOnDuty(admin, entity.isOnDuty());
        restoreManagedSectionIds(admin, entity.getManagedSectionIds());

        return admin;
    }

    protected WarehousePositionEntity toWarehousePositionEntity(WarehousePosition position) {
        if (position == null) return null;
        return warehousePositionRepository.findByName(position.name())
                .orElseThrow(() -> new RuntimeException("Warehouse position not found: " + position.name()));
    }

    protected WarehousePosition toWarehousePosition(WarehousePositionEntity entity) {
        if (entity == null) return null;
        return WarehousePosition.valueOf(entity.getName());
    }

    public WarehouseAdminEntity toEntity(User user) {
        if (user instanceof WarehouseAdmin admin) {
            return toEntity(admin);
        }
        throw new IllegalArgumentException("Expected WarehouseAdmin, got: " + user.getClass());
    }

    public WarehouseAdmin toDomain(UserEntity entity) {
        if (entity instanceof WarehouseAdminEntity adminEntity) {
            return toDomain(adminEntity);
        }
        throw new IllegalArgumentException("Expected WarehouseAdminEntity, got: " + entity.getClass());
    }

    private void restorePosition(WarehouseAdmin admin, WarehousePositionEntity positionEntity) {
        if (positionEntity != null) {
            admin.setPosition(toWarehousePosition(positionEntity));
        }
    }

    private void restoreOnDuty(WarehouseAdmin admin, boolean onDuty) {
        try {
            Field field = WarehouseAdmin.class.getDeclaredField("onDuty");
            field.setAccessible(true);
            field.set(admin, onDuty);
        } catch (Exception e) {
            throw new RuntimeException("Failed to restore onDuty", e);
        }
    }

    private void restoreManagedSectionIds(WarehouseAdmin admin, java.util.Set<String> sectionIds) {
        try {
            Field field = WarehouseAdmin.class.getDeclaredField("managedSectionIds");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Set<String> set = (java.util.Set<String>) field.get(admin);
            set.clear();
            if (sectionIds != null) {
                set.addAll(sectionIds);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to restore managedSectionIds", e);
        }
    }
}