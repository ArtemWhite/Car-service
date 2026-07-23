package dealerShipOrder.infrastructure.adapters.userAdapters.userReferencesAdapters;

import dealerShipOrder.domain.models.users.User;
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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserDateAdapter {

    private final UserJpaRepository jpaRepository;
    private final ClientEntityMapper clientMapper;
    private final ManagerEntityMapper managerMapper;
    private final SystemAdminEntityMapper systemAdminMapper;
    private final WarehouseAdminEntityMapper warehouseAdminMapper;

    private Instant toInstant(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.atZone(ZoneId.systemDefault()).toInstant();
    }

    public List<User> findByRegisteredAtBetween(LocalDateTime start, LocalDateTime end) {
        Instant startInstant = toInstant(start);
        Instant endInstant = toInstant(end);

        return jpaRepository.findAllByRemovedFalse().stream()
                .filter(entity -> {
                    Instant createdAt = entity.getCreatedAt();
                    return (startInstant == null || createdAt.compareTo(startInstant) >= 0) &&
                            (endInstant == null || createdAt.compareTo(endInstant) <= 0);
                })
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    public List<User> findByLastActiveAtBefore(LocalDateTime date) {
        Instant instant = toInstant(date);
        return jpaRepository.findInactiveSince(instant).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    public List<User> findInactiveSince(LocalDateTime date) {
        return findByLastActiveAtBefore(date);
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