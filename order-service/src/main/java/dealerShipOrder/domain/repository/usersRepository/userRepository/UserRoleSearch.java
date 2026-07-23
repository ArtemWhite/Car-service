package dealerShipOrder.domain.repository.usersRepository.userRepository;

import dealerShipOrder.domain.models.users.User;
import dealerShipOrder.domain.models.users.UserStatus;

import java.util.List;

public interface UserRoleSearch {
    <T extends User> List<T> findAllByRole(Class<T> roleClass);
    <T extends User> List<T> findByRoleAndStatus(Class<T> roleClass, UserStatus status);
    <T extends User> long countByRole(Class<T> roleClass);

}
