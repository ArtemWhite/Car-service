package dealerShipOrder.infrastructure.adapters.userAdapters.userReferencesAdapters;

import dealerShipOrder.domain.models.users.User;
import dealerShipOrder.domain.models.users.client.Client;
import dealerShipOrder.domain.models.users.manager.Manager;
import dealerShipOrder.domain.models.users.systemAdmin.SystemAdmin;
import dealerShipOrder.domain.models.users.warehouseAdmin.WarehouseAdmin;
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
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserBaseRepositoryAdapter {

    private final UserJpaRepository jpaRepository;
    private final ClientEntityMapper clientMapper;
    private final ManagerEntityMapper managerMapper;
    private final SystemAdminEntityMapper systemAdminMapper;
    private final WarehouseAdminEntityMapper warehouseAdminMapper;

    public User save(User user) {
        UserEntity entity = toEntity(user);
        UserEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    public Optional<User> findById(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            return jpaRepository.findByIdAndRemovedFalse(uuid)
                    .map(this::toDomain);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public List<User> findAll() {
        return jpaRepository.findAllByRemovedFalse().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    public void delete(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            jpaRepository.deleteById(uuid);
        } catch (IllegalArgumentException e) {

        }
    }

    public boolean existsById(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            return jpaRepository.existsById(uuid);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private UserEntity toEntity(User user) {
        if (user instanceof Client client) {
            return clientMapper.toEntity(client);
        } else if (user instanceof Manager manager) {
            return managerMapper.toEntity(manager);
        } else if (user instanceof SystemAdmin admin) {
            return systemAdminMapper.toEntity(admin);
        } else if (user instanceof WarehouseAdmin warehouseAdmin) {
            return warehouseAdminMapper.toEntity(warehouseAdmin);
        }
        throw new IllegalArgumentException("Unknown user type: " + user.getClass());
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
        throw new IllegalArgumentException("Unknown entity type: " + entity.getClass());
    }
}