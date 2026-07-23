package dealerShipOrder.application.services.userService.systemAdmin;

import dealerShipOrder.application.dtos.request.userRequest.*;
import dealerShipOrder.application.dtos.response.userResponse.UserBaseResponse;
import dealerShipOrder.application.dtos.response.userResponse.UserListResponse;
import dealerShipOrder.application.dtos.response.userResponse.users.SystemAdminResponse;
import dealerShipOrder.application.mapper.UserMapper;
import dealerShipOrder.application.services.userService.BaseUserService;
import dealerShipOrder.domain.repository.usersRepository.userRepository.UserRepository;
import dealerShipOrder.domain.models.expection.DomainValidationException;
import dealerShipOrder.domain.models.users.User;
import dealerShipOrder.domain.models.users.UserStatus;
import dealerShipOrder.domain.models.users.client.Client;
import dealerShipOrder.domain.models.users.manager.Manager;
import dealerShipOrder.domain.models.users.manager.Position;
import dealerShipOrder.domain.models.users.systemAdmin.AdminLevel;
import dealerShipOrder.domain.models.users.systemAdmin.SystemAdmin;
import dealerShipOrder.domain.models.users.systemAdmin.SystemPermission;
import dealerShipOrder.domain.models.users.warehouseAdmin.WarehouseAdmin;
import dealerShipOrder.domain.models.users.warehouseAdmin.WarehousePosition;
import dealerShipOrder.infrastructure.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SystemAdminServiceImpl extends BaseUserService implements SystemAdminService {

    public SystemAdminServiceImpl(
            UserRepository userRepository,
            UserMapper userMapper) {
        super(userRepository, userMapper);
    }

    @Override
    public SystemAdminResponse updateOwnProfile(UpdateUserRequest request) {
        SystemAdmin admin = getCurrentSystemAdmin();
        userMapper.updateDomain(admin, request);
        SystemAdmin updated = (SystemAdmin) saveUser(admin);
        return userMapper.toSystemAdminResponse(updated);
    }

    @Override
    public SystemAdminResponse changeOwnPassword(ChangePasswordRequest request) {
        SystemAdmin admin = getCurrentSystemAdmin();
        if (!admin.authenticate(request.getOldPassword())) {
            throw new DomainValidationException("Old password is incorrect");
        }
        admin.changePassword(request.getOldPassword(), request.getNewPassword());
        SystemAdmin updated = (SystemAdmin) saveUser(admin);
        return userMapper.toSystemAdminResponse(updated);
    }

    private SystemAdmin getCurrentSystemAdmin() {
        String adminId = SecurityUtils.getCurrentUserId();
        User user = findUserById(adminId);
        if (!(user instanceof SystemAdmin admin)) {
            throw new DomainValidationException("User is not a system admin");
        }
        return admin;
    }

    @Override
    @Transactional(readOnly = true)
    public SystemAdminResponse getUserById(String id) {
        SystemAdmin admin = getCurrentSystemAdmin();
        return userMapper.toSystemAdminResponse(admin);
    }

    @Override
    public UserBaseResponse createUser(CreateUserRequest request) {
        SystemAdmin admin = getCurrentSystemAdmin();
        admin.checkPermission(SystemPermission.CREATE_USER);

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DomainValidationException("User with this email already exists");
        }

        User user = userMapper.toDomain(request);
        User saved = saveUser(user);

        admin.logAction("CREATE_USER", "Created user: " + saved.getId() + " (" + saved.getEmail() + ")");
        saveUser(admin);

        return getUserResponseByType(saved);
    }

    @Override
    public UserBaseResponse updateUser(String userId, UpdateUserRequest request) {
        SystemAdmin admin = getCurrentSystemAdmin();
        admin.checkPermission(SystemPermission.UPDATE_USER);

        User user = findUserById(userId);
        userMapper.updateDomain(user, request);
        User updated = saveUser(user);

        admin.logAction("UPDATE_USER", "Updated user: " + userId);
        saveUser(admin);

        return getUserResponseByType(updated);
    }

    @Override
    public void deleteUser(String userId, String reason) {
        SystemAdmin admin = getCurrentSystemAdmin();
        admin.checkPermission(SystemPermission.DELETE_USER);

        userRepository.delete(userId);

        admin.logAction("DELETE_USER", "Deleted user: " + userId + ". Reason: " + reason);
        saveUser(admin);
    }

    @Override
    public UserBaseResponse blockUser(String userId, String reason) {
        SystemAdmin admin = getCurrentSystemAdmin();
        admin.checkPermission(SystemPermission.BLOCK_USER);

        User user = findUserById(userId);
        user.block();
        User updated = saveUser(user);

        admin.logAction("BLOCK_USER", "Blocked user: " + userId + ". Reason: " + reason);
        saveUser(admin);

        return getUserResponseByType(updated);
    }

    @Override
    public UserBaseResponse unblockUser(String userId) {
        SystemAdmin admin = getCurrentSystemAdmin();
        admin.checkPermission(SystemPermission.BLOCK_USER);

        User user = findUserById(userId);
        user.activate();
        User updated = saveUser(user);

        admin.logAction("UNBLOCK_USER", "Unblocked user: " + userId);
        saveUser(admin);

        return getUserResponseByType(updated);
    }

    private UserBaseResponse getUserResponseByType(User user) {
        return switch (user) {
            case Client client -> userMapper.toClientResponse(client);
            case Manager manager -> userMapper.toManagerResponse(manager);
            case SystemAdmin admin -> userMapper.toSystemAdminResponse(admin);
            case WarehouseAdmin warehouseAdmin -> userMapper.toWarehouseAdminResponse(warehouseAdmin);
            default -> null;
        };
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserBaseResponse> getAllUsers() {
        SystemAdmin admin = getCurrentSystemAdmin();
        admin.checkPermission(SystemPermission.VIEW_USERS);

        return userRepository.findAll().stream()
                .map(userMapper::toBaseResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserBaseResponse> getUsersByType(String userType) {
        SystemAdmin admin = getCurrentSystemAdmin();
        admin.checkPermission(SystemPermission.VIEW_USERS);

        Class<? extends User> userClass = switch (userType) {
            case "CLIENT" -> Client.class;
            case "MANAGER" -> Manager.class;
            case "SYSTEM_ADMIN" -> SystemAdmin.class;
            case "WAREHOUSE_ADMIN" -> WarehouseAdmin.class;
            default -> throw new DomainValidationException("Unknown user type: " + userType);
        };

        return userRepository.findAllByRole(userClass).stream()
                .map(userMapper::toBaseResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserBaseResponse getUserDetails(String userId) {
        SystemAdmin admin = getCurrentSystemAdmin();
        admin.checkPermission(SystemPermission.VIEW_USERS);

        User user = findUserById(userId);
        return getUserResponseByType(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserListResponse getUsersWithFilters(UserFilterRequest filter) {
        SystemAdmin admin = getCurrentSystemAdmin();
        admin.checkPermission(SystemPermission.VIEW_USERS);

        List<User> users = userRepository.findAll();
        List<UserBaseResponse> responses = users.stream()
                .map(userMapper::toBaseResponse)
                .collect(Collectors.toList());

        return new UserListResponse(
                responses,
                users.size(),
                (int) users.stream().filter(u -> u.getStatus() == UserStatus.ACTIVE).count(),
                (int) users.stream().filter(u -> u.getStatus() == UserStatus.INACTIVE).count(),
                (int) users.stream().filter(u -> u.getStatus() == UserStatus.BLOCKED).count()
        );
    }

    @Override
    public SystemAdminResponse grantPermission(String targetAdminId, SystemPermission permission) {
        SystemAdmin admin = getCurrentSystemAdmin();
        admin.checkPermission(SystemPermission.MANAGE_PERMISSIONS);

        SystemAdmin target = findSystemAdminById(targetAdminId);

        if (target.getLevel().ordinal() >= admin.getLevel().ordinal()) {
            throw new DomainValidationException("Cannot manage admin with same or higher level");
        }

        target.addPermission(permission);
        SystemAdmin updated = (SystemAdmin) saveUser(target);

        admin.logAction("GRANT_PERMISSION", "Granted " + permission + " to " + target.getEmail());
        saveUser(admin);

        return userMapper.toSystemAdminResponse(updated);
    }

    private SystemAdmin findSystemAdminById(String adminId) {
        User user = findUserById(adminId);
        if (!(user instanceof SystemAdmin systemAdmin)) {
            throw new DomainValidationException("User is not a system admin");
        }
        return systemAdmin;
    }

    @Override
    public SystemAdminResponse revokePermission(String targetAdminId, SystemPermission permission) {
        SystemAdmin admin = getCurrentSystemAdmin();
        admin.checkPermission(SystemPermission.MANAGE_PERMISSIONS);

        SystemAdmin target = findSystemAdminById(targetAdminId);

        target.removePermission(permission);
        SystemAdmin updated = (SystemAdmin) saveUser(target);

        admin.logAction("REVOKE_PERMISSION", "Revoked " + permission + " from " + target.getEmail());
        saveUser(admin);

        return userMapper.toSystemAdminResponse(updated);
    }

    @Override
    public SystemAdminResponse promoteAdmin(String targetAdminId, String newLevel) {
        SystemAdmin admin = getCurrentSystemAdmin();
        admin.checkPermission(SystemPermission.MANAGE_PERMISSIONS);

        SystemAdmin target = findSystemAdminById(targetAdminId);
        AdminLevel level = AdminLevel.valueOf(newLevel);

        if (level.ordinal() >= admin.getLevel().ordinal()) {
            throw new DomainValidationException("Cannot promote to same or higher level than yourself");
        }

        SystemAdmin promoted = new SystemAdmin(
                target.getFirstName(),
                target.getLastName(),
                target.getMiddleName(),
                target.getEmail(),
                target.getPhone(),
                "",
                target.getId(),
                level
        );

        userRepository.delete(targetAdminId);
        SystemAdmin saved = (SystemAdmin) saveUser(promoted);

        admin.logAction("PROMOTE_ADMIN", "Promoted " + target.getEmail() + " to " + newLevel);
        saveUser(admin);

        return userMapper.toSystemAdminResponse(saved);
    }

    @Override
    public SystemAdminResponse promoteManager(String managerId, String newPosition) {
        SystemAdmin admin = getCurrentSystemAdmin();
        admin.checkPermission(SystemPermission.MANAGE_PERMISSIONS);

        User user = findUserById(managerId);
        if (!(user instanceof Manager manager)) {
            throw new DomainValidationException("User is not a manager");
        }

        manager.promote(Position.valueOf(newPosition));
        saveUser(manager);

        admin.logAction("PROMOTE_MANAGER", "Promoted manager " + managerId + " to " + newPosition);
        saveUser(admin);

        return userMapper.toSystemAdminResponse(admin);
    }

    @Override
    public SystemAdminResponse promoteWarehouseAdmin(String targetAdminId, String newPosition) {
        SystemAdmin admin = getCurrentSystemAdmin();
        admin.checkPermission(SystemPermission.MANAGE_PERMISSIONS);

        WarehouseAdmin target = findWarehouseAdminById(targetAdminId);
        target.setPosition(WarehousePosition.valueOf(newPosition));
        saveUser(target);

        admin.logAction("PROMOTE_WAREHOUSE_ADMIN", "Promoted warehouse admin " + targetAdminId + " to " + newPosition);
        saveUser(admin);

        return userMapper.toSystemAdminResponse(admin);
    }

    private WarehouseAdmin findWarehouseAdminById(String adminId) {
        User user = findUserById(adminId);
        if (!(user instanceof WarehouseAdmin warehouseAdmin)) {
            throw new DomainValidationException("User is not a warehouse admin");
        }
        return warehouseAdmin;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OperationHistoryRequest> getAuditLog() {
        SystemAdmin admin = getCurrentSystemAdmin();
        admin.checkPermission(SystemPermission.VIEW_AUDIT_LOG);

        return admin.getAuditLog().stream()
                .map(log -> OperationHistoryRequest.builder()
                        .id(log.getAdminId())
                        .operationType(log.getAction())
                        .description(log.getDetails())
                        .timestamp(log.getTimestamp())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OperationHistoryRequest> getUserAuditLog(String userId) {
        SystemAdmin admin = getCurrentSystemAdmin();
        admin.checkPermission(SystemPermission.VIEW_AUDIT_LOG);

        return admin.getAuditLog().stream()
                .filter(log -> log.getAdminId().equals(userId))
                .map(log -> OperationHistoryRequest.builder()
                        .id(log.getAdminId())
                        .operationType(log.getAction())
                        .description(log.getDetails())
                        .timestamp(log.getTimestamp())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void changeSystemSettings(Object settings) {
        SystemAdmin admin = getCurrentSystemAdmin();
        admin.checkPermission(SystemPermission.SYSTEM_CONFIG);

        admin.logAction("SYSTEM_CONFIG", "Changed system settings");
        saveUser(admin);
    }

    @Override
    public UserBaseResponse deactivateUser(String userId) {
        SystemAdmin admin = getCurrentSystemAdmin();
        admin.checkPermission(SystemPermission.UPDATE_USER);

        User user = findUserById(userId);
        user.deactivate();
        User updated = saveUser(user);

        admin.logAction("DEACTIVATE_USER", "Deactivated user: " + userId);
        saveUser(admin);

        return getUserResponseByType(updated);
    }

    @Override
    public SystemAdminResponse getMyProfile() {
        SystemAdmin admin = getCurrentSystemAdmin();
        return userMapper.toSystemAdminResponse(admin);
    }
}