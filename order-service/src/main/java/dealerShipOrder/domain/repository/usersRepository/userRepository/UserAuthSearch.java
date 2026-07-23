package dealerShipOrder.domain.repository.usersRepository.userRepository;

import dealerShipOrder.domain.models.users.User;

import java.util.Optional;

public interface UserAuthSearch {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByEmailAndPassword(String email, String passwordHash);
}
