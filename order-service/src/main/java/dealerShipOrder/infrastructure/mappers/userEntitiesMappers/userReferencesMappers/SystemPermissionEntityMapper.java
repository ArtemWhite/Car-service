package dealerShipOrder.infrastructure.mappers.userEntitiesMappers.userReferencesMappers;

import dealerShipOrder.domain.models.users.systemAdmin.SystemPermission;
import dealerShipOrder.infrastructure.entities.userEntities.systemAdminEntities.SystemPermissionEntity;
import dealerShipOrder.infrastructure.jpaRepository.userJpaRepositories.systemAdminJpaRepositories.SystemPermissionJpaRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class SystemPermissionEntityMapper {

    @Autowired
    protected SystemPermissionJpaRepository permissionRepository;

    public SystemPermissionEntity toEntity(SystemPermission permission) {
        if (permission == null) return null;
        return permissionRepository.findByName(permission.name())
                .orElseThrow(() -> new RuntimeException("Permission not found: " + permission.name()));
    }

    public SystemPermission toDomain(SystemPermissionEntity entity) {
        if (entity == null) return null;
        return SystemPermission.valueOf(entity.getName());
    }
}