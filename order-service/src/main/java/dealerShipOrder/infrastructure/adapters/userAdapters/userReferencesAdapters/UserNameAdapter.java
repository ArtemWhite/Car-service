package dealerShipOrder.infrastructure.adapters.userAdapters.userReferencesAdapters;

import dealerShipOrder.domain.models.users.User;
import dealerShipOrder.infrastructure.entities.userEntities.ClientEntity;
import dealerShipOrder.infrastructure.entities.userEntities.UserEntity;
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
public class UserNameAdapter {

    private final UserJpaRepository jpaRepository;
    private final ClientEntityMapper clientMapper;
    private final ManagerEntityMapper managerMapper;
    private final SystemAdminEntityMapper systemAdminMapper;
    private final WarehouseAdminEntityMapper warehouseAdminMapper;

    public List<User> findByFirstName(String firstName) {
        return jpaRepository.findAllByRemovedFalse().stream()
                .filter(entity -> entity.getFirstName().equalsIgnoreCase(firstName))
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    public List<User> findByLastName(String lastName) {
        return jpaRepository.findAllByRemovedFalse().stream()
                .filter(entity -> entity.getLastName().equalsIgnoreCase(lastName))
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    public List<User> findByFullNameContaining(String query) {
        String lowerQuery = query.toLowerCase();
        return jpaRepository.findAllByRemovedFalse().stream()
                .filter(entity -> (entity.getFirstName() + " " + entity.getLastName()).toLowerCase().contains(lowerQuery))
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private User toDomain(UserEntity entity) {
        if (entity instanceof ClientEntity clientEntity) {
            return clientMapper.toDomain(clientEntity);
        } else if (entity instanceof ManagerEntity managerEntity) {
            return managerMapper.toDomain(managerEntity);
        } else if (entity instanceof SystemAdminEntity systemAdminEntity) {
            return systemAdminMapper.toDomain(systemAdminEntity);
        } else if (entity instanceof WarehouseAdminEntity warehouseAdminEntity) {
            return warehouseAdminMapper.toDomain(warehouseAdminEntity);
        }
        return createMinimalDomain(entity);
    }

    private User createMinimalDomain(UserEntity entity) {
        String userType = entity.getUserType() != null ? entity.getUserType().getName() : "CLIENT";
        User user = switch (userType) {
            case "CLIENT" -> new dealerShipOrder.domain.models.users.client.Client(entity.getId().toString(),
                    entity.getFirstName(), entity.getLastName(), entity.getMiddleName(),
                    entity.getEmail(), entity.getPhone(), entity.getPasswordHash());
            case "MANAGER" -> new dealerShipOrder.domain.models.users.manager.Manager(
                    entity.getFirstName(), entity.getLastName(), entity.getMiddleName(),
                    entity.getEmail(), entity.getPhone(), entity.getPasswordHash(),
                    entity.getId().toString());
            case "SYSTEM_ADMIN" -> new dealerShipOrder.domain.models.users.systemAdmin.SystemAdmin(
                    entity.getFirstName(), entity.getLastName(), entity.getMiddleName(),
                    entity.getEmail(), entity.getPhone(), entity.getPasswordHash(),
                    entity.getId().toString(),
                    dealerShipOrder.domain.models.users.systemAdmin.AdminLevel.JUNIOR_ADMIN);
            case "WAREHOUSE_ADMIN" -> new dealerShipOrder.domain.models.users.warehouseAdmin.WarehouseAdmin(
                    entity.getFirstName(), entity.getLastName(), entity.getMiddleName(),
                    entity.getEmail(), entity.getPhone(), entity.getPasswordHash(),
                    entity.getId().toString());
            default -> throw new IllegalArgumentException("Unknown user type: " + userType);
        };
        if (entity.getStatus() != null) {
            switch (entity.getStatus().getName()) {
                case "BLOCKED" -> user.block();
                case "INACTIVE" -> user.deactivate();
            }
        }
        return user;
    }
}