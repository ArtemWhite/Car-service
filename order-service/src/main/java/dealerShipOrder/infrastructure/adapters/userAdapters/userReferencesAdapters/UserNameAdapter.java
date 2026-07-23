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
        throw new IllegalArgumentException("Unknown entity type: " + entity.getClass());
    }
}