package dealerShipOrder.infrastructure.mappers.userEntitiesMappers.userMappers.systemAdminEntitiesMappers;

import dealerShipOrder.domain.models.users.User;
import dealerShipOrder.domain.models.users.systemAdmin.*;
import dealerShipOrder.infrastructure.entities.userEntities.UserEntity;
import dealerShipOrder.infrastructure.entities.userEntities.systemAdminEntities.*;
import dealerShipOrder.infrastructure.jpaRepository.userJpaRepositories.systemAdminJpaRepositories.AdminLevelJpaRepository;
import dealerShipOrder.infrastructure.jpaRepository.userJpaRepositories.systemAdminJpaRepositories.SystemAdminJpaRepository;
import dealerShipOrder.infrastructure.jpaRepository.userJpaRepositories.systemAdminJpaRepositories.SystemPermissionJpaRepository;
import dealerShipOrder.infrastructure.mappers.userEntitiesMappers.userMappers.BaseUserEntityMapper;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class SystemAdminEntityMapper extends BaseUserEntityMapper {

    @Autowired
    protected AdminLevelJpaRepository adminLevelRepository;

    @Autowired
    protected SystemPermissionJpaRepository permissionRepository;

    @Autowired
    protected SystemAdminJpaRepository systemAdminJpaRepository;

    public SystemAdminEntity toEntity(SystemAdmin admin) {
        if (admin == null) return null;

        UUID uuid = toUuid(admin.getId());
        SystemAdminEntity entity = (uuid != null) ?
                systemAdminJpaRepository.findById(uuid).orElseGet(SystemAdminEntity::new) : new SystemAdminEntity();

        fillBaseUserEntity(entity, admin);
        entity.setAdminLevel(toAdminLevelEntity(admin.getLevel()));
        List<SystemPermissionEntity> newPerms = toPermissionEntities(admin.getPermissions());
        if (entity.getPermissions() != null) {
            entity.getPermissions().clear();
            entity.getPermissions().addAll(newPerms);
        } else {
            entity.setPermissions(newPerms);
        }
        entity.setLastLoginAt(toInstant(admin.getLastLoginAt()));

        if (admin.getAuditLog() != null) {
            List<AuditLogEntryEntity> auditEntities = admin.getAuditLog().stream().map(log -> {
                AuditLogEntryEntity entry = new AuditLogEntryEntity();
                entry.setAdmin(entity);
                entry.setAction(log.getAction());
                entry.setDetails(log.getDetails());
                entry.setTimestamp(toInstant(log.getTimestamp()));
                return entry;
            }).collect(Collectors.toList());
            if (entity.getAuditLog() != null) {
                entity.getAuditLog().clear();
                entity.getAuditLog().addAll(auditEntities);
            } else {
                entity.setAuditLog(auditEntities);
            }
        }
        return entity;
    }

    public SystemAdmin toDomain(SystemAdminEntity entity) {
        if (entity == null) return null;

        SystemAdmin admin = new SystemAdmin(
                entity.getFirstName(),
                entity.getLastName(),
                entity.getMiddleName(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getPasswordHash(),
                entity.getId().toString(),
                toAdminLevel(entity.getAdminLevel())
        );

        restorePermissions(admin, toPermissions(entity.getPermissions()));
        restoreLastLoginAt(admin, toLocalDateTime(entity.getLastLoginAt()));
        fillBaseUserDomain(admin, entity);

        return admin;
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

    private void restorePermissions(SystemAdmin admin, Set<SystemPermission> permissions) {
        try {
            Field field = SystemAdmin.class.getDeclaredField("permissions");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Set<SystemPermission> set = (Set<SystemPermission>) field.get(admin);
            set.clear();
            if (permissions != null) {
                set.addAll(permissions);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to restore permissions", e);
        }
    }

    private void restoreLastLoginAt(SystemAdmin admin, LocalDateTime lastLoginAt) {
        try {
            Field field = SystemAdmin.class.getDeclaredField("lastLoginAt");
            field.setAccessible(true);
            field.set(admin, lastLoginAt);
        } catch (Exception e) {
            throw new RuntimeException("Failed to restore lastLoginAt", e);
        }
    }
}