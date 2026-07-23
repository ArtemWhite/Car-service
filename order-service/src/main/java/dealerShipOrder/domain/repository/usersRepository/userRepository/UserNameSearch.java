package dealerShipOrder.domain.repository.usersRepository.userRepository;

import dealerShipOrder.domain.models.users.User;

import java.util.List;

public interface UserNameSearch {
    List<User> findByFirstName(String firstName);
    List<User> findByLastName(String lastName);
    List<User> findByFullNameContaining(String query);
}
