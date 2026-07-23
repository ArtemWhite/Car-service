package dealerShipOrder.application.services.userService.manager;

import dealerShipOrder.application.dtos.request.userRequest.ChangePasswordRequest;
import dealerShipOrder.application.dtos.request.userRequest.UpdateUserRequest;
import dealerShipOrder.application.dtos.response.userResponse.users.ManagerResponse;
import dealerShipOrder.application.mapper.UserMapper;
import dealerShipOrder.application.services.userService.BaseUserService;
import dealerShipOrder.domain.repository.usersRepository.userRepository.UserRepository;
import dealerShipOrder.domain.models.expection.DomainValidationException;
import dealerShipOrder.domain.models.users.User;
import dealerShipOrder.domain.models.users.manager.Manager;
import dealerShipOrder.domain.models.users.manager.Position;
import dealerShipOrder.infrastructure.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ManagerServiceImpl extends BaseUserService implements ManagerService {

    public ManagerServiceImpl(
            UserRepository userRepository,
            UserMapper userMapper) {
        super(userRepository, userMapper);
    }

    private Manager getCurrentManager() {
        String managerId = SecurityUtils.getCurrentUserId();
        User user = findUserById(managerId);
        if (!(user instanceof Manager manager)) {
            throw new DomainValidationException("User is not a manager");
        }
        return manager;
    }

    @Override
    public ManagerResponse getMyProfile() {
        Manager manager = getCurrentManager();
        return userMapper.toManagerResponse(manager);
    }

    @Override
    public ManagerResponse getUserById(String id) {
        Manager manager = getCurrentManager();
        return userMapper.toManagerResponse(manager);
    }

    @Override
    public ManagerResponse updateOwnProfile(UpdateUserRequest request) {
        Manager manager = getCurrentManager();
        request.setPosition(null);
        userMapper.updateDomain(manager, request);
        Manager updated = (Manager) saveUser(manager);
        return userMapper.toManagerResponse(updated);
    }

    @Override
    public ManagerResponse changeOwnPassword(ChangePasswordRequest request) {
        Manager manager = getCurrentManager();
        if (!manager.authenticate(request.getOldPassword())) {
            throw new DomainValidationException("Old password is incorrect");
        }
        manager.changePassword(request.getOldPassword(), request.getNewPassword());
        Manager updated = (Manager) saveUser(manager);
        return userMapper.toManagerResponse(updated);
    }

    @Override
    public ManagerResponse setAvailability(boolean available) {
        Manager manager = getCurrentManager();
        manager.setAvailable(available);
        Manager updated = (Manager) saveUser(manager);
        return userMapper.toManagerResponse(updated);
    }

    @Override
    public List<ManagerResponse> getAllManagers() {
        return userRepository.findAllByRole(Manager.class).stream()
                .map(userMapper::toManagerResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ManagerResponse> getAvailableManagers() {
        return userRepository.findAvailableManagers().stream()
                .filter(user -> user instanceof Manager)
                .map(user -> (Manager) user)
                .map(userMapper::toManagerResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ManagerResponse promote(String newPosition) {
        Manager manager = getCurrentManager();
        manager.promote(Position.valueOf(newPosition));
        Manager updated = (Manager) saveUser(manager);
        return userMapper.toManagerResponse(updated);
    }
}