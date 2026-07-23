package dealerShipOrder.infrastructure.mappers.userEntitiesMappers.userMappers.systemAdminEntitiesMappers;

import dealerShipOrder.domain.models.users.User;
import dealerShipOrder.domain.models.users.systemAdmin.*;
import dealerShipOrder.infrastructure.entities.userEntities.UserEntity;
import dealerShipOrder.infrastructure.entities.userEntities.systemAdminEntities.*;
import dealerShipOrder.infrastructure.jpaRepository.userJpaRepositories.systemAdminJpaRepositories.AdminLevelJpaRepository;
import dealerShipOrder.infrastructure.jpaRepository.userJpaRepositories.systemAdminJpaRepositories.SystemPermissionJpaRepository;
import dealerShipOrder.infrastructure.mappers.userEntitiesMappers.userMappers.BaseUserEntityMapper;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class SystemAdminEntityMapper extends BaseUserEntityMapper {

    @Autowired
    protected AdminLevelJpaRepository adminLevelRepository;

    @Autowired
    protected SystemPermissionJpaRepository permissionRepository;

    public SystemAdminEntity toEntity(SystemAdmin admin) {
        if (admin == null) return null;

        SystemAdminEntity entity = new SystemAdminEntity();
        fillBaseUserEntity(entity, admin);
        entity.setAdminLevel(toAdminLevelEntity(admin.getLevel()));
        entity.setPermissions(toPermissionEntities(admin.getPermissions()));
        entity.setLastLoginAt(toInstant(admin.getLastLoginAt()));
        return entity;
    }

    public SystemAdmin toDomain(SystemAdminEntity entity) {
        if (entity == null) return null;

        return new SystemAdmin(
                entity.getFirstName(),
                entity.getLastName(),
                entity.getMiddleName(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getPasswordHash(),
                entity.getId().toString(),
                toAdminLevel(entity.getAdminLevel())
        );
    }

    protected AdminLevelEntity toAdminLevelEntity(AdminLevel level) {
        if (level == null) return null;
        return adminLevelRepository.findByName(level.name())
                .orElseThrow(() -> new RuntimeException("Admin level not found: " + level.name()));
    }

    protected AdminLevel toAdminLevel(AdminLevelEntity entity) {
        if (entity == null) return null;
        return AdminLevel.valueOf(entity.getName());
    }

    protected List<SystemPermissionEntity> toPermissionEntities(Set<SystemPermission> permissions) {
        if (permissions == null) return new ArrayList<>();
        return permissions.stream()
                .map(p -> permissionRepository.findByName(p.name())
                        .orElseThrow(() -> new RuntimeException("Permission not found: " + p.name())))
                .collect(Collectors.toList());
    }

    protected Set<SystemPermission> toPermissions(List<SystemPermissionEntity> entities) {
        if (entities == null) return Collections.emptySet();
        return entities.stream()
                .map(e -> SystemPermission.valueOf(e.getName()))
                .collect(Collectors.toSet());
    }

    public SystemAdminEntity toEntity(User user) {
        if (user instanceof SystemAdmin admin) {
            return toEntity(admin);
        }
        throw new IllegalArgumentException("Expected SystemAdmin, got: " + user.getClass());
    }

    public SystemAdmin toDomain(UserEntity entity) {
        if (entity instanceof SystemAdminEntity adminEntity) {
            return toDomain(adminEntity);
        }
        throw new IllegalArgumentException("Expected SystemAdminEntity, got: " + entity.getClass());
    }
}