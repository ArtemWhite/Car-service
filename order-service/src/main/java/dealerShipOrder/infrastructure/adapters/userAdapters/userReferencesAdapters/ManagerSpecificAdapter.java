package dealerShipOrder.infrastructure.adapters.userAdapters.userReferencesAdapters;

import dealerShipOrder.domain.models.users.User;
import dealerShipOrder.domain.models.users.manager.Position;
import dealerShipOrder.infrastructure.jpaRepository.userJpaRepositories.managerJpaRepositories.ManagerJpaRepository;
import dealerShipOrder.infrastructure.mappers.userEntitiesMappers.userMappers.managerEntitiesMappers.ManagerEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ManagerSpecificAdapter {

    private final ManagerJpaRepository managerJpaRepository;
    private final ManagerEntityMapper managerMapper;

    public List<User> findAvailableManagers() {
        return managerJpaRepository.findAvailableManagers().stream()
                .map(managerMapper::toDomain)
                .collect(Collectors.toList());
    }

    public List<User> findManagersByPosition(Position position) {
        return managerJpaRepository.findByPosition(position.name()).stream()
                .map(managerMapper::toDomain)
                .collect(Collectors.toList());
    }

    public List<User> findManagersWithActiveOrders() {
        return managerJpaRepository.findManagersWithActiveOrders().stream()
                .map(managerMapper::toDomain)
                .collect(Collectors.toList());
    }
}