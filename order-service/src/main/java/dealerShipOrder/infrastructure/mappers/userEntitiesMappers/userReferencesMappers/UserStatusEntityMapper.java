package dealerShipOrder.infrastructure.mappers.userEntitiesMappers.userReferencesMappers;

import dealerShipOrder.domain.models.users.UserStatus;
import dealerShipOrder.infrastructure.entities.userEntities.referenceUserEntities.UserStatusEntity;
import dealerShipOrder.infrastructure.jpaRepository.userJpaRepositories.referenceUserJpaRepositories.UserStatusJpaRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class UserStatusEntityMapper {

    @Autowired
    protected UserStatusJpaRepository statusRepository;

    public UserStatusEntity toEntity(UserStatus status) {
        if (status == null) return null;
        return statusRepository.findByName(status.name())
                .orElseThrow(() -> new RuntimeException("User status not found: " + status.name()));
    }

    public UserStatus toDomain(UserStatusEntity entity) {
        if (entity == null) return null;
        return UserStatus.valueOf(entity.getName());
    }
}