package dealerShipOrder.domain.models.users.systemAdmin;

import dealerShipOrder.domain.models.expection.DomainValidationException;
import dealerShipOrder.domain.models.users.UserType;
import dealerShipOrder.domain.models.users.User;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.*;

public class SystemAdmin extends User {
    @Getter
    private final AdminLevel level;
    private final Set<SystemPermission> permissions;
    private final List<AuditLogEntry> auditLog;
    @Getter
    private LocalDateTime lastLoginAt;

    public SystemAdmin(String firstName, String lastName, String middleName, String email, String phone,
                       String password, String employeeId, AdminLevel level) {
        super(employeeId, firstName, lastName, middleName, email, phone, password, UserType.SYSTEM_ADMIN);
        this.level = level;
        this.permissions = new HashSet<>(level.getDefaultPermissions());
        this.auditLog = new ArrayList<>();
        this.lastLoginAt = LocalDateTime.now();
    }
    public void checkPermission(SystemPermission permission) {
        if (!permissions.contains(permission) && level != AdminLevel.SUPER_ADMIN) {
            throw new DomainValidationException("Permission denied: " + permission);
        }
    }

    public boolean hasPermission(SystemPermission permission) {
        return permissions.contains(permission) || level == AdminLevel.SUPER_ADMIN;
    }

    public void logAction(String action, String details) {
        this.auditLog.add(new AuditLogEntry(this.getId(), action, details, LocalDateTime.now()));
        this.updateLastActive();
    }

    public void addPermission(SystemPermission permission) {
        this.permissions.add(permission);
        this.updateLastActive();
    }

    public void removePermission(SystemPermission permission) {
        this.permissions.remove(permission);
        this.updateLastActive();
    }

    public void login() {
        this.lastLoginAt = LocalDateTime.now();
        logAction("LOGIN", "Admin logged in");
    }

    public boolean canPromoteTo(AdminLevel newLevel, SystemAdmin targetAdmin) {
        if (newLevel.ordinal() <= targetAdmin.getLevel().ordinal()) {
            return false;
        }

        if (this.level == AdminLevel.SUPER_ADMIN) {
            return true;
        }

        return newLevel.ordinal() <= this.level.ordinal();
    }

    public List<AuditLogEntry> getAuditLog() {
        return Collections.unmodifiableList(auditLog);
    }

    public Set<SystemPermission> getPermissions() {
        return Collections.unmodifiableSet(permissions);
    }
}