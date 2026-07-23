package dealerShipOrder.application.services.userService;

import dealerShipOrder.application.mapper.UserMapper;
import dealerShipOrder.domain.repository.usersRepository.userRepository.UserRepository;
import dealerShipOrder.domain.models.expection.EntityNotFoundException;
import dealerShipOrder.domain.models.users.User;

public abstract class BaseUserService {

    protected final UserRepository userRepository;
    protected final UserMapper userMapper;

    public BaseUserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    protected User findUserById(String userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new EntityNotFoundException("User not found with id: " + userId));
    }

    protected User saveUser(User user) {
        return userRepository.save(user);
    }
}