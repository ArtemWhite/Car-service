package dealerShipOrder.infrastructure.mappers.userEntitiesMappers.userMappers;

import dealerShipOrder.domain.models.users.*;
import dealerShipOrder.domain.models.users.client.Client;
import dealerShipOrder.domain.models.users.manager.Manager;
import dealerShipOrder.domain.models.users.systemAdmin.SystemAdmin;
import dealerShipOrder.domain.models.users.warehouseAdmin.WarehouseAdmin;
import dealerShipOrder.infrastructure.entities.userEntities.*;
import dealerShipOrder.infrastructure.entities.userEntities.referenceUserEntities.UserStatusEntity;
import dealerShipOrder.infrastructure.entities.userEntities.referenceUserEntities.UserTypeEntity;
import dealerShipOrder.infrastructure.jpaRepository.userJpaRepositories.referenceUserJpaRepositories.UserStatusJpaRepository;
import dealerShipOrder.infrastructure.jpaRepository.userJpaRepositories.referenceUserJpaRepositories.UserTypeJpaRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Mapper(componentModel = "spring")
public abstract class BaseUserEntityMapper {

    @Autowired
    protected UserStatusJpaRepository userStatusRepository;

    @Autowired
    protected UserTypeJpaRepository userTypeRepository;

    protected void fillBaseUserEntity(UserEntity entity, User user) {
        entity.setId(toUuid(user.getId()));
        entity.setFirstName(user.getFirstName());
        entity.setLastName(user.getLastName());
        entity.setMiddleName(user.getMiddleName());
        entity.setEmail(user.getEmail());
        entity.setPhone(user.getPhone());
        entity.setPasswordHash(user.getPasswordHash());
        entity.setStatus(toUserStatusEntity(user.getStatus()));
        entity.setLastActiveAt(toInstant(user.getLastActiveAt()));
        entity.setLastPasswordChangeAt(toInstant(user.getLastPasswordChangeAt()));

        UserType userType = user.getUserType();
        if (userType == null) {

            if (user instanceof Client) {
                userType = UserType.CLIENT;
            } else if (user instanceof Manager) {
                userType = UserType.MANAGER;
            } else if (user instanceof SystemAdmin) {
                userType = UserType.SYSTEM_ADMIN;
            } else if (user instanceof WarehouseAdmin) {
                userType = UserType.WAREHOUSE_ADMIN;
            }
        }

        if (userType != null) {
            entity.setUserType(toUserTypeEntity(userType));
        }
    }

    protected UUID toUuid(String id) {
        if (id == null) return null;
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    protected String fromUuid(UUID uuid) {
        return uuid == null ? null : uuid.toString();
    }

    protected List<String> toListOfStrings(List<String> ids) {
        if (ids == null) return new ArrayList<>();
        return new ArrayList<>(ids);
    }

    protected Instant toInstant(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.atZone(ZoneId.systemDefault()).toInstant();
    }

    protected LocalDateTime toLocalDateTime(Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    protected UserStatusEntity toUserStatusEntity(UserStatus status) {
        if (status == null) return null;
        return userStatusRepository.findByName(status.name())
                .orElseThrow(() -> new RuntimeException("User status not found: " + status.name()));
    }

    protected UserStatus toUserStatus(UserStatusEntity entity) {
        if (entity == null) return null;
        return UserStatus.valueOf(entity.getName());
    }

    protected UserTypeEntity toUserTypeEntity(UserType userType) {
        if (userType == null) return null;
        return userTypeRepository.findByName(userType.name())
                .orElseThrow(() -> new RuntimeException("User type not found: " + userType.name()));
    }

    protected UserType toUserType(UserTypeEntity entity) {
        if (entity == null) return null;
        return UserType.valueOf(entity.getName());
    }
}