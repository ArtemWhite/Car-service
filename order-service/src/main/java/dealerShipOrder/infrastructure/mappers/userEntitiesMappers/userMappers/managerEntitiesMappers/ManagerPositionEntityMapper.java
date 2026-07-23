package dealerShipOrder.infrastructure.mappers.userEntitiesMappers.userMappers.managerEntitiesMappers;

import dealerShipOrder.domain.models.users.manager.Position;
import dealerShipOrder.infrastructure.entities.userEntities.managerEntities.ManagerPositionEntity;
import dealerShipOrder.infrastructure.jpaRepository.userJpaRepositories.managerJpaRepositories.ManagerPositionJpaRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class ManagerPositionEntityMapper {

    @Autowired
    protected ManagerPositionJpaRepository positionRepository;

    public ManagerPositionEntity toEntity(Position position) {
        if (position == null) return null;
        return positionRepository.findByName(position.name())
                .orElseThrow(() -> new RuntimeException("Position not found: " + position.name()));
    }

    public Position toDomain(ManagerPositionEntity entity) {
        if (entity == null) return null;
        return Position.valueOf(entity.getName());
    }
}