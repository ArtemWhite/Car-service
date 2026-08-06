package dealerShipOrder.application.services.userService.systemAdmin;

import dealerShipOrder.application.dtos.request.userRequest.*;
import dealerShipOrder.application.dtos.response.userResponse.UserBaseResponse;
import dealerShipOrder.application.dtos.response.userResponse.UserListResponse;
import dealerShipOrder.application.dtos.response.userResponse.users.ClientResponse;
import dealerShipOrder.application.dtos.response.userResponse.users.ManagerResponse;
import dealerShipOrder.application.dtos.response.userResponse.users.SystemAdminResponse;
import dealerShipOrder.application.dtos.response.userResponse.users.WarehouseAdminResponse;
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
import dealerShipOrder.domain.models.users.systemAdmin.AuditLogEntry;
import dealerShipOrder.domain.models.users.systemAdmin.SystemAdmin;
import dealerShipOrder.domain.models.users.systemAdmin.SystemPermission;
import dealerShipOrder.domain.models.users.warehouseAdmin.WarehouseAdmin;
import dealerShipOrder.domain.models.users.warehouseAdmin.WarehousePosition;
import dealerShipOrder.infrastructure.jpaRepository.userJpaRepositories.systemAdminJpaRepositories.AuditLogEntryJpaRepository;
import dealerShipOrder.infrastructure.security.SecurityUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class SystemAdminServiceImpl extends BaseUserService implements SystemAdminService {

    private final AuditLogEntryJpaRepository auditLogRepository;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;
    @org.springframework.beans.factory.annotation.Autowired
    private javax.persistence.EntityManager entityManager;

    public SystemAdminServiceImpl(
            UserRepository userRepository,
            UserMapper userMapper,
            AuditLogEntryJpaRepository auditLogRepository,
            org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
        super(userRepository, userMapper);
        this.auditLogRepository = auditLogRepository;
        this.jdbcTemplate = jdbcTemplate;
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

        if (admin.getId().equals(userId)) {
            throw new DomainValidationException("Cannot delete your own account");
        }

        userRepository.delete(userId);

        admin.logAction("DELETE_USER", "Deleted user: " + userId + ". Reason: " + reason);
        saveUser(admin);
    }

    @Override
    public UserBaseResponse blockUser(String userId, String reason) {
        SystemAdmin admin = getCurrentSystemAdmin();
        admin.checkPermission(SystemPermission.BLOCK_USER);

        if (admin.getId().equals(userId)) {
            throw new DomainValidationException("Cannot block your own account");
        }

        User user = findUserById(userId);
        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new DomainValidationException("User is already blocked");
        }

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
            default -> userMapper.toBaseResponse(user);
        };
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserBaseResponse> getAllUsers() {
        SystemAdmin admin = getCurrentSystemAdmin();
        admin.checkPermission(SystemPermission.VIEW_USERS);

        return userRepository.findAll().stream()
                .map(this::getUserResponseByType)
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
                .map(this::getUserResponseByType)
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

        List<User> allUsers = userRepository.findAll();
        List<User> filteredUsers = allUsers.stream().filter(u -> {
            if (filter.getQuery() != null && !filter.getQuery().isBlank()) {
                String q = filter.getQuery().toLowerCase();
                boolean matchesEmail = u.getEmail() != null && u.getEmail().toLowerCase().contains(q);
                boolean matchesFirst = u.getFirstName() != null && u.getFirstName().toLowerCase().contains(q);
                boolean matchesLast = u.getLastName() != null && u.getLastName().toLowerCase().contains(q);
                boolean matchesFull = u.getFullName() != null && u.getFullName().toLowerCase().contains(q);
                if (!matchesEmail && !matchesFirst && !matchesLast && !matchesFull) {
                    return false;
                }
            }
            if (filter.getUserType() != null && !filter.getUserType().isBlank()) {
                if (u.getUserType() == null || !u.getUserType().name().equalsIgnoreCase(filter.getUserType())) {
                    return false;
                }
            }
            if (filter.getStatus() != null && !filter.getStatus().isBlank()) {
                if (u.getStatus() == null || !u.getStatus().name().equalsIgnoreCase(filter.getStatus())) {
                    return false;
                }
            }
            if (filter.getEmail() != null && !filter.getEmail().isBlank()) {
                if (u.getEmail() == null || !u.getEmail().toLowerCase().contains(filter.getEmail().toLowerCase())) {
                    return false;
                }
            }
            if (filter.getPhone() != null && !filter.getPhone().isBlank()) {
                if (u.getPhone() == null || !u.getPhone().contains(filter.getPhone())) {
                    return false;
                }
            }
            if (filter.getFirstName() != null && !filter.getFirstName().isBlank()) {
                if (u.getFirstName() == null || !u.getFirstName().toLowerCase().contains(filter.getFirstName().toLowerCase())) {
                    return false;
                }
            }
            if (filter.getLastName() != null && !filter.getLastName().isBlank()) {
                if (u.getLastName() == null || !u.getLastName().toLowerCase().contains(filter.getLastName().toLowerCase())) {
                    return false;
                }
            }
            if (Boolean.TRUE.equals(filter.getActive())) {
                if (u.getStatus() != UserStatus.ACTIVE) {
                    return false;
                }
            }
            if (filter.getManagerPosition() != null && !filter.getManagerPosition().isBlank()) {
                if (!(u instanceof Manager m) || m.getPosition() == null || !m.getPosition().name().equalsIgnoreCase(filter.getManagerPosition())) {
                    return false;
                }
            }
            if (filter.getAdminLevel() != null && !filter.getAdminLevel().isBlank()) {
                if (!(u instanceof SystemAdmin a) || a.getLevel() == null || !a.getLevel().name().equalsIgnoreCase(filter.getAdminLevel())) {
                    return false;
                }
            }
            if (filter.getAvailable() != null) {
                if (!(u instanceof Manager m) || m.isAvailable() != filter.getAvailable()) {
                    return false;
                }
            }
            if (filter.getSectionId() != null && !filter.getSectionId().isBlank()) {
                if (!(u instanceof WarehouseAdmin w) || w.getManagedSectionIds() == null || !w.getManagedSectionIds().contains(filter.getSectionId())) {
                    return false;
                }
            }
            if (filter.getNewsletterSubscribed() != null) {
                if (!(u instanceof Client c) || c.isNewsletterSubscribed() != filter.getNewsletterSubscribed()) {
                    return false;
                }
            }
            return true;
        }).collect(Collectors.toList());

        List<User> paginatedUsers;
        if (filter.getPage() != null && filter.getSize() != null && filter.getSize() > 0) {
            int fromIndex = Math.min(filter.getPage() * filter.getSize(), filteredUsers.size());
            int toIndex = Math.min(fromIndex + filter.getSize(), filteredUsers.size());
            paginatedUsers = filteredUsers.subList(fromIndex, toIndex);
        } else {
            paginatedUsers = filteredUsers;
        }

        List<UserBaseResponse> responses = paginatedUsers.stream()
                .map(this::getUserResponseByType)
                .collect(Collectors.toList());

        return new UserListResponse(
                responses,
                filteredUsers.size(),
                (int) allUsers.stream().filter(u -> u.getStatus() == UserStatus.ACTIVE).count(),
                (int) allUsers.stream().filter(u -> u.getStatus() == UserStatus.INACTIVE).count(),
                (int) allUsers.stream().filter(u -> u.getStatus() == UserStatus.BLOCKED).count()
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

        target.promoteTo(level);
        SystemAdmin saved = (SystemAdmin) saveUser(target);

        admin.logAction("PROMOTE_ADMIN", "Promoted " + target.getEmail() + " to " + newLevel);
        saveUser(admin);

        return userMapper.toSystemAdminResponse(saved);
    }

    @Override
    public ManagerResponse promoteManager(String managerId, String newPosition) {
        SystemAdmin admin = getCurrentSystemAdmin();
        admin.checkPermission(SystemPermission.MANAGE_PERMISSIONS);

        User user = findUserById(managerId);
        if (!(user instanceof Manager manager)) {
            throw new DomainValidationException("User is not a manager");
        }

        manager.promote(Position.valueOf(newPosition));
        Manager saved = (Manager) saveUser(manager);

        admin.logAction("PROMOTE_MANAGER", "Promoted manager " + managerId + " to " + newPosition);
        saveUser(admin);

        return userMapper.toManagerResponse(saved);
    }

    @Override
    public SystemAdminResponse promoteManagerToAdmin(String managerId, String adminLevelStr) {
        SystemAdmin admin = getCurrentSystemAdmin();
        admin.checkPermission(SystemPermission.MANAGE_PERMISSIONS);

        User user = findUserById(managerId);
        if (!(user instanceof Manager manager)) {
            throw new DomainValidationException("User is not a manager");
        }
        AdminLevel level = AdminLevel.valueOf(adminLevelStr != null ? adminLevelStr : "JUNIOR_ADMIN");

        jdbcTemplate.update("DELETE FROM managers WHERE user_id = ?::uuid", UUID.fromString(managerId));
        jdbcTemplate.update("UPDATE users SET user_type_id = (SELECT id FROM user_types WHERE name = 'SYSTEM_ADMIN') WHERE id = ?::uuid", UUID.fromString(managerId));
        jdbcTemplate.update("INSERT INTO system_admins (user_id, admin_level_id, last_login_at) VALUES (?::uuid, (SELECT id FROM admin_levels WHERE name = ?), NOW()) ON CONFLICT (user_id) DO UPDATE SET admin_level_id = EXCLUDED.admin_level_id", UUID.fromString(managerId), level.name());

        entityManager.flush();
        entityManager.clear();

        SystemAdmin newAdmin = findSystemAdminById(managerId);
        admin.logAction("PROMOTE_MANAGER_TO_ADMIN", "Promoted manager " + manager.getEmail() + " to " + level.name());
        saveUser(admin);

        return userMapper.toSystemAdminResponse(newAdmin);
    }

    @Override
    public WarehouseAdminResponse promoteWarehouseAdmin(String targetAdminId, String newPosition) {
        SystemAdmin admin = getCurrentSystemAdmin();
        admin.checkPermission(SystemPermission.MANAGE_PERMISSIONS);

        WarehouseAdmin target = findWarehouseAdminById(targetAdminId);
        target.setPosition(WarehousePosition.valueOf(newPosition));
        WarehouseAdmin saved = (WarehouseAdmin) saveUser(target);

        admin.logAction("PROMOTE_WAREHOUSE_ADMIN", "Promoted warehouse admin " + targetAdminId + " to " + newPosition);
        saveUser(admin);

        return userMapper.toWarehouseAdminResponse(saved);
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

        return auditLogRepository.findAll().stream()
                .filter(entity -> !entity.isRemoved())
                .map(entity -> OperationHistoryRequest.builder()
                        .id(entity.getId().toString())
                        .operationType(entity.getAction())
                        .description(entity.getDetails())
                        .timestamp(entity.getTimestamp() != null ? java.time.LocalDateTime.ofInstant(entity.getTimestamp(), ZoneId.systemDefault()) : java.time.LocalDateTime.now())
                        .adminId(entity.getAdmin() != null ? entity.getAdmin().getId().toString() : null)
                        .adminName(entity.getAdmin() != null ? entity.getAdmin().getFirstName() + " " + entity.getAdmin().getLastName() : "Admin")
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OperationHistoryRequest> getUserAuditLog(String userId) {
        SystemAdmin admin = getCurrentSystemAdmin();
        admin.checkPermission(SystemPermission.VIEW_AUDIT_LOG);

        return auditLogRepository.findByAdminId(UUID.fromString(userId)).stream()
                .filter(entity -> !entity.isRemoved())
                .map(entity -> OperationHistoryRequest.builder()
                        .id(entity.getId().toString())
                        .operationType(entity.getAction())
                        .description(entity.getDetails())
                        .timestamp(entity.getTimestamp() != null ? java.time.LocalDateTime.ofInstant(entity.getTimestamp(), ZoneId.systemDefault()) : java.time.LocalDateTime.now())
                        .adminId(entity.getAdmin() != null ? entity.getAdmin().getId().toString() : null)
                        .adminName(entity.getAdmin() != null ? entity.getAdmin().getFirstName() + " " + entity.getAdmin().getLastName() : "Admin")
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

        if (admin.getId().equals(userId)) {
            throw new DomainValidationException("Cannot deactivate your own account");
        }

        User user = findUserById(userId);
        user.deactivate();
        User updated = saveUser(user);

        admin.logAction("DEACTIVATE_USER", "Deactivated user: " + userId);
        saveUser(admin);

        return getUserResponseByType(updated);
    }

    @Override
    public UserBaseResponse activateUser(String userId) {
        SystemAdmin admin = getCurrentSystemAdmin();
        admin.checkPermission(SystemPermission.UPDATE_USER);

        User user = findUserById(userId);
        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new DomainValidationException("User is already active");
        }

        user.activate();
        User updated = saveUser(user);

        admin.logAction("ACTIVATE_USER", "Activated user: " + userId);
        saveUser(admin);

        return getUserResponseByType(updated);
    }

    @Override
    public UserBaseResponse restoreUser(String userId) {
        SystemAdmin admin = getCurrentSystemAdmin();
        admin.checkPermission(SystemPermission.UPDATE_USER);

        User user = findUserById(userId);
        user.activate();
        User updated = saveUser(user);

        admin.logAction("RESTORE_USER", "Restored user: " + userId);
        saveUser(admin);

        return getUserResponseByType(updated);
    }

    @Override
    public SystemAdminResponse getMyProfile() {
        SystemAdmin admin = getCurrentSystemAdmin();
        return userMapper.toSystemAdminResponse(admin);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserBaseResponse> getAllSystemAdmins() {
        SystemAdmin admin = getCurrentSystemAdmin();
        admin.checkPermission(SystemPermission.VIEW_USERS);
        return userRepository.findAllByRole(SystemAdmin.class).stream()
                .map(this::getUserResponseByType)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserBaseResponse> getAllWarehouseAdmins() {
        SystemAdmin admin = getCurrentSystemAdmin();
        admin.checkPermission(SystemPermission.VIEW_USERS);
        return userRepository.findAllByRole(WarehouseAdmin.class).stream()
                .map(this::getUserResponseByType)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getAdminPermissions(String adminId) {
        SystemAdmin admin = getCurrentSystemAdmin();
        SystemAdmin target = findSystemAdminById(adminId);
        return Map.of(
                "adminId", target.getId(),
                "permissions", target.getPermissions().stream()
                        .map(Enum::name)
                        .collect(Collectors.toList())
        );
    }

    @Override
    @Transactional
    public SystemAdminResponse demoteAdmin(String targetAdminId, String newLevel) {
        SystemAdmin admin = getCurrentSystemAdmin();
        admin.checkPermission(SystemPermission.MANAGE_PERMISSIONS);

        SystemAdmin target = findSystemAdminById(targetAdminId);
        AdminLevel level = AdminLevel.valueOf(newLevel);

        target.promoteTo(level);
        SystemAdmin saved = (SystemAdmin) saveUser(target);

        admin.logAction("DEMOTE_ADMIN", "Demoted " + target.getEmail() + " to " + newLevel);
        saveUser(admin);

        return userMapper.toSystemAdminResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getSystemStats() {
        SystemAdmin admin = getCurrentSystemAdmin();
        admin.checkPermission(SystemPermission.VIEW_USERS);

        List<User> allUsers = userRepository.findAll();
        long activeUsers = allUsers.stream().filter(u -> u.getStatus() == UserStatus.ACTIVE).count();
        long clientsCount = userRepository.countByRole(Client.class);
        long managersCount = userRepository.countByRole(Manager.class);
        long adminsCount = userRepository.countByRole(SystemAdmin.class);
        long warehouseAdminsCount = userRepository.countByRole(WarehouseAdmin.class);

        return Map.of(
                "totalUsers", allUsers.size(),
                "activeUsers", activeUsers,
                "clientsCount", clientsCount,
                "managersCount", managersCount,
                "adminsCount", adminsCount,
                "warehouseAdminsCount", warehouseAdminsCount
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getUserRegistrationStats(int days) {
        SystemAdmin admin = getCurrentSystemAdmin();
        admin.checkPermission(SystemPermission.VIEW_USERS);

        List<Map<String, Object>> dailyStats = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < days; i++) {
            LocalDateTime day = now.minusDays(i);
            long count = userRepository.findAll().stream()
                    .filter(u -> u.getRegisteredAt() != null &&
                            u.getRegisteredAt().toLocalDate().equals(day.toLocalDate()))
                    .count();
            Map<String, Object> dayStat = new HashMap<>();
            dayStat.put("date", day.toLocalDate().toString());
            dayStat.put("count", count);
            dailyStats.add(dayStat);
        }

        return Map.of("dailyStats", dailyStats);
    }

    @Override
    @Transactional
    public List<UserBaseResponse> bulkUpdateUserStatus(List<String> userIds, String status) {
        SystemAdmin admin = getCurrentSystemAdmin();
        admin.checkPermission(SystemPermission.UPDATE_USER);

        UserStatus newStatus = UserStatus.valueOf(status);
        List<UserBaseResponse> results = new ArrayList<>();

        for (String userId : userIds) {
            User user = findUserById(userId);
            switch (newStatus) {
                case ACTIVE -> user.activate();
                case INACTIVE -> user.deactivate();
                case BLOCKED -> user.block();
            }
            User updated = saveUser(user);
            results.add(getUserResponseByType(updated));
        }

        admin.logAction("BULK_STATUS_UPDATE", "Updated status of " + userIds.size() + " users to " + status);
        saveUser(admin);

        return results;
    }

    @Override
    @Transactional
    public void bulkDeleteUsers(List<String> userIds, String reason) {
        SystemAdmin admin = getCurrentSystemAdmin();
        admin.checkPermission(SystemPermission.DELETE_USER);

        for (String userId : userIds) {
            if (!admin.getId().equals(userId)) {
                try {
                    UUID uuid = UUID.fromString(userId);
                    jdbcTemplate.update("UPDATE users SET removed = true, updated_at = NOW() WHERE id = ?::uuid", uuid);
                } catch (Exception e) {

                }
            }
        }
        String details = "Bulk deleted " + userIds.size() + " users. Reason: " + reason;
        if (details.length() > 500) {
            details = details.substring(0, 497) + "...";
        }
        admin.logAction("BULK_DELETE_USERS", details);
        saveUser(admin);
    }
}