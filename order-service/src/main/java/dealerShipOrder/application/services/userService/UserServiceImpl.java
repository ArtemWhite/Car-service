package dealerShipOrder.application.services.userService;

import dealerShipOrder.application.dtos.request.userRequest.ChangePasswordRequest;
import dealerShipOrder.application.dtos.request.userRequest.UpdateUserRequest;
import dealerShipOrder.application.dtos.response.userResponse.UserBaseResponse;
import dealerShipOrder.application.mapper.UserMapper;
import dealerShipOrder.domain.repository.usersRepository.userRepository.UserRepository;
import dealerShipOrder.domain.models.expection.DomainValidationException;
import dealerShipOrder.domain.models.users.User;
import dealerShipOrder.infrastructure.security.SecurityUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Primary
@Transactional
public class UserServiceImpl extends BaseUserService implements UserService {

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        super(userRepository, userMapper);
    }

    private User getCurrentUser() {
        String userId = SecurityUtils.getCurrentUserId();
        return findUserById(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserBaseResponse getUserById(String id) {
        if (id == null || id.isBlank() || "me".equalsIgnoreCase(id)) {
            User user = getCurrentUser();
            return getUserResponseByType(user);
        }
        User user = findUserById(id);
        return getUserResponseByType(user);
    }

    private UserBaseResponse getUserResponseByType(User user) {
        return switch (user) {
            case dealerShipOrder.domain.models.users.client.Client client -> userMapper.toClientResponse(client);
            case dealerShipOrder.domain.models.users.manager.Manager manager -> userMapper.toManagerResponse(manager);
            case dealerShipOrder.domain.models.users.systemAdmin.SystemAdmin admin -> userMapper.toSystemAdminResponse(admin);
            case dealerShipOrder.domain.models.users.warehouseAdmin.WarehouseAdmin warehouseAdmin -> userMapper.toWarehouseAdminResponse(warehouseAdmin);
            default -> userMapper.toBaseResponse(user);
        };
    }

    @Override
    public UserBaseResponse updateOwnProfile(UpdateUserRequest request) {
        User user = getCurrentUser();
        userMapper.updateDomain(user, request);
        User updated = saveUser(user);
        return getUserResponseByType(updated);
    }

    @Override
    public UserBaseResponse changeOwnPassword(ChangePasswordRequest request) {
        User user = getCurrentUser();
        if (!user.authenticate(request.getOldPassword())) {
            throw new DomainValidationException("Old password is incorrect");
        }
        user.changePassword(request.getOldPassword(), request.getNewPassword());
        User updated = saveUser(user);
        return userMapper.toBaseResponse(updated);
    }

    @Override
    public UserBaseResponse updateOwnProfile(String userId, UpdateUserRequest request) {
        User user = getCurrentUser();
        if (!user.getId().equals(userId)) {
            throw new DomainValidationException("User ID mismatch");
        }
        return updateOwnProfile(request);
    }

    @Override
    public UserBaseResponse changeOwnPassword(String userId, ChangePasswordRequest request) {
        User user = getCurrentUser();
        if (!user.getId().equals(userId)) {
            throw new DomainValidationException("User ID mismatch");
        }
        return changeOwnPassword(request);
    }
}