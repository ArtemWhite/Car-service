package dealerShipOrder.infrastructure.mappers.userEntitiesMappers.userMappers.systemAdminEntitiesMappers;

import dealerShipOrder.domain.models.users.systemAdmin.AdminLevel;
import dealerShipOrder.infrastructure.entities.userEntities.systemAdminEntities.AdminLevelEntity;
import dealerShipOrder.infrastructure.jpaRepository.userJpaRepositories.systemAdminJpaRepositories.AdminLevelJpaRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class AdminLevelEntityMapper {

    @Autowired
    protected AdminLevelJpaRepository adminLevelRepository;

    public AdminLevelEntity toEntity(AdminLevel level) {
        if (level == null) return null;
        return adminLevelRepository.findByName(level.name())
                .orElseThrow(() -> new RuntimeException("Admin level not found: " + level.name()));
    }

    public AdminLevel toDomain(AdminLevelEntity entity) {
        if (entity == null) return null;
        return AdminLevel.valueOf(entity.getName());
    }
}