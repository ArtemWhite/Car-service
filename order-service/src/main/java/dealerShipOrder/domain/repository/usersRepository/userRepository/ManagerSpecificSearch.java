package dealerShipOrder.domain.repository.usersRepository.userRepository;

import dealerShipOrder.domain.models.users.User;
import dealerShipOrder.domain.models.users.manager.Position;

import java.util.List;

public interface ManagerSpecificSearch {
    List<User> findAvailableManagers();
    List<User> findManagersByPosition(Position position);
    List<User> findManagersWithActiveOrders();
}
