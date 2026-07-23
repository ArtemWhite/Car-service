package dealerShipOrder.infrastructure.adapters.userAdapters.userReferencesAdapters;

import dealerShipOrder.domain.models.users.User;
import dealerShipOrder.domain.models.users.UserStatus;
import dealerShipOrder.domain.models.users.client.Client;
import dealerShipOrder.domain.models.users.manager.Manager;
import dealerShipOrder.domain.models.users.systemAdmin.SystemAdmin;
import dealerShipOrder.domain.models.users.warehouseAdmin.WarehouseAdmin;
import dealerShipOrder.infrastructure.entities.userEntities.ClientEntity;
import dealerShipOrder.infrastructure.entities.userEntities.managerEntities.ManagerEntity;
import dealerShipOrder.infrastructure.entities.userEntities.systemAdminEntities.SystemAdminEntity;
import dealerShipOrder.infrastructure.entities.userEntities.warehouseAdminEntities.WarehouseAdminEntity;
import dealerShipOrder.infrastructure.jpaRepository.userJpaRepositories.UserJpaRepository;
import dealerShipOrder.infrastructure.mappers.userEntitiesMappers.userMappers.clientEntitiesMappers.ClientEntityMapper;
import dealerShipOrder.infrastructure.mappers.userEntitiesMappers.userMappers.managerEntitiesMappers.ManagerEntityMapper;
import dealerShipOrder.infrastructure.mappers.userEntitiesMappers.userMappers.systemAdminEntitiesMappers.SystemAdminEntityMapper;
import dealerShipOrder.infrastructure.mappers.userEntitiesMappers.userMappers.warehouseAdminEntitiesMappers.WarehouseAdminEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserRoleAdapter {

    private final UserJpaRepository jpaRepository;
    private final ClientEntityMapper clientMapper;
    private final ManagerEntityMapper managerMapper;
    private final SystemAdminEntityMapper systemAdminMapper;
    private final WarehouseAdminEntityMapper warehouseAdminMapper;

    @SuppressWarnings("unchecked")
    public <T extends User> List<T> findAllByRole(Class<T> roleClass) {
        String userType;
        if (roleClass == Client.class) {
            userType = "CLIENT";
        } else if (roleClass == Manager.class) {
            userType = "MANAGER";
        } else if (roleClass == SystemAdmin.class) {
            userType = "SYSTEM_ADMIN";
        } else if (roleClass == WarehouseAdmin.class) {
            userType = "WAREHOUSE_ADMIN";
        } else {
            return List.of();
        }

        return jpaRepository.findByUserType(userType).stream()
                .map(this::toDomain)
                .map(user -> (T) user)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public <T extends User> List<T> findByRoleAndStatus(Class<T> roleClass, UserStatus status) {
        return findAllByRole(roleClass).stream()
                .filter(user -> user.getStatus() == status)
                .map(user -> (T) user)
                .collect(Collectors.toList());
    }

    public <T extends User> long countByRole(Class<T> roleClass) {
        return findAllByRole(roleClass).size();
    }

    private User toDomain(dealerShipOrder.infrastructure.entities.userEntities.UserEntity entity) {
        if (entity instanceof ClientEntity clientEntity) {
            return clientMapper.toDomain(clientEntity);
        } else if (entity instanceof ManagerEntity managerEntity) {
            return managerMapper.toDomain(managerEntity);
        } else if (entity instanceof SystemAdminEntity systemAdminEntity) {
            return systemAdminMapper.toDomain(systemAdminEntity);
        } else if (entity instanceof WarehouseAdminEntity warehouseAdminEntity) {
            return warehouseAdminMapper.toDomain(warehouseAdminEntity);
        }
        throw new IllegalArgumentException("Unknown entity type: " + entity.getClass());
    }
}