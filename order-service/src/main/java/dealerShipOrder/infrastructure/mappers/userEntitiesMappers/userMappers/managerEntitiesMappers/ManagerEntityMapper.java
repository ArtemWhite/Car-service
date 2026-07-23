package dealerShipOrder.infrastructure.mappers.userEntitiesMappers.userMappers.managerEntitiesMappers;

import dealerShipOrder.domain.models.users.User;
import dealerShipOrder.domain.models.users.manager.Manager;
import dealerShipOrder.domain.models.users.manager.Position;
import dealerShipOrder.infrastructure.entities.userEntities.UserEntity;
import dealerShipOrder.infrastructure.entities.userEntities.managerEntities.ManagerEntity;
import dealerShipOrder.infrastructure.entities.userEntities.managerEntities.ManagerPositionEntity;
import dealerShipOrder.infrastructure.jpaRepository.userJpaRepositories.managerJpaRepositories.ManagerPositionJpaRepository;
import dealerShipOrder.infrastructure.mappers.userEntitiesMappers.userMappers.BaseUserEntityMapper;

import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Field;
import java.util.List;

@Mapper(componentModel = "spring")
public abstract class ManagerEntityMapper extends BaseUserEntityMapper {

    @Autowired
    protected ManagerPositionJpaRepository positionRepository;

    public ManagerEntity toEntity(Manager manager) {
        if (manager == null) return null;

        ManagerEntity entity = new ManagerEntity();
        fillBaseUserEntity(entity, manager);
        entity.setPosition(toPositionEntity(manager.getPosition()));
        entity.setMaxConcurrentOrders(manager.getMaxConcurrentOrders());
        entity.setMaxConcurrentTestDrives(manager.getMaxConcurrentTestDrives());
        entity.setAvailable(manager.isAvailable());
        entity.setAssignedOrderIds(toListOfStrings(manager.getAssignedOrders()));
        entity.setManagedTestDriveIds(toListOfStrings(manager.getManagedTestDrives()));
        entity.setTestDriveFleetCarIds(toListOfStrings(manager.getTestDriveFleet()));
        return entity;
    }

    public Manager toDomain(ManagerEntity entity) {
        if (entity == null) return null;

        Manager manager = new Manager(
                entity.getFirstName(),
                entity.getLastName(),
                entity.getMiddleName(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getPasswordHash(),
                entity.getId().toString()
        );
        restorePosition(manager, entity.getPosition());
        restoreMaxConcurrentOrders(manager, entity.getMaxConcurrentOrders());
        restoreMaxConcurrentTestDrives(manager, entity.getMaxConcurrentTestDrives());
        restoreAvailability(manager, entity.isAvailable());
        restoreAssignedOrders(manager, entity.getAssignedOrderIds());
        restoreManagedTestDrives(manager, entity.getManagedTestDriveIds());
        restoreTestDriveFleet(manager, entity.getTestDriveFleetCarIds());

        return manager;
    }

    protected ManagerPositionEntity toPositionEntity(Position position) {
        if (position == null) return null;
        return positionRepository.findByName(position.name())
                .orElseThrow(() -> new RuntimeException("Position not found: " + position.name()));
    }

    protected Position toPosition(ManagerPositionEntity entity) {
        if (entity == null) return null;
        return Position.valueOf(entity.getName());
    }

    public ManagerEntity toEntity(User user) {
        if (user instanceof Manager manager) {
            return toEntity(manager);
        }
        throw new IllegalArgumentException("Expected Manager, got: " + user.getClass());
    }

    public Manager toDomain(UserEntity entity) {
        if (entity instanceof ManagerEntity managerEntity) {
            return toDomain(managerEntity);
        }
        throw new IllegalArgumentException("Expected ManagerEntity, got: " + entity.getClass());
    }

    private void restorePosition(Manager manager, ManagerPositionEntity positionEntity) {
        if (positionEntity != null) {
            manager.promote(toPosition(positionEntity));
        }
    }

    private void restoreMaxConcurrentOrders(Manager manager, int maxConcurrentOrders) {
        try {
            Field field = Manager.class.getDeclaredField("maxConcurrentOrders");
            field.setAccessible(true);
            field.set(manager, maxConcurrentOrders);
        } catch (Exception e) {
            throw new RuntimeException("Failed to restore maxConcurrentOrders", e);
        }
    }

    private void restoreMaxConcurrentTestDrives(Manager manager, int maxConcurrentTestDrives) {
        try {
            Field field = Manager.class.getDeclaredField("maxConcurrentTestDrives");
            field.setAccessible(true);
            field.set(manager, maxConcurrentTestDrives);
        } catch (Exception e) {
            throw new RuntimeException("Failed to restore maxConcurrentTestDrives", e);
        }
    }

    private void restoreAvailability(Manager manager, boolean available) {
        manager.setAvailable(available);
    }

    private void restoreAssignedOrders(Manager manager, List<String> orderIds) {
        try {
            Field field = Manager.class.getDeclaredField("assignedOrderIds");
            field.setAccessible(true);
            List<String> list = (List<String>) field.get(manager);
            list.clear();
            if (orderIds != null) {
                list.addAll(orderIds);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to restore assignedOrders", e);
        }
    }

    private void restoreManagedTestDrives(Manager manager, List<String> testDriveIds) {
        try {
            Field field = Manager.class.getDeclaredField("managedTestDriveIds");
            field.setAccessible(true);
            List<String> list = (List<String>) field.get(manager);
            list.clear();
            if (testDriveIds != null) {
                list.addAll(testDriveIds);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to restore managedTestDrives", e);
        }
    }

    private void restoreTestDriveFleet(Manager manager, List<String> carIds) {
        try {
            Field field = Manager.class.getDeclaredField("testDriveFleetCarIds");
            field.setAccessible(true);
            List<String> list = (List<String>) field.get(manager);
            list.clear();
            if (carIds != null) {
                list.addAll(carIds);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to restore testDriveFleet", e);
        }
    }
}