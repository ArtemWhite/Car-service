package dealerShipOrder.domain.repository.usersRepository.userRepository;

import dealerShipOrder.domain.models.users.User;
import dealerShipOrder.domain.models.users.UserStatus;

import java.util.List;

public interface UserStatusSearch {
    List<User> findByStatus(UserStatus status);
    List<User> findActiveUsers();
    List<User> findInactiveUsers();
    List<User> findBlockedUsers();
    long countByStatus(UserStatus status);
}
