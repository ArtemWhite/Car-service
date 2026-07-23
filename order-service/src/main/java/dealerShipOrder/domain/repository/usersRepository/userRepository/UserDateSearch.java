package dealerShipOrder.domain.repository.usersRepository.userRepository;

import dealerShipOrder.domain.models.users.User;

import java.time.LocalDateTime;
import java.util.List;

public interface UserDateSearch {
    List<User> findByRegisteredAtBetween(LocalDateTime start, LocalDateTime end);
    List<User> findByLastActiveAtBefore(LocalDateTime date);
    List<User> findInactiveSince(LocalDateTime date);
}
