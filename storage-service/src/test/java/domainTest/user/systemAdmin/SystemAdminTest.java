package domainTest.user.systemAdmin;

import domain.exception.DomainValidationException;
import domain.models.users.systemAdmin.AdminLevel;
import domain.models.users.systemAdmin.AuditLogEntry;
import domain.models.users.systemAdmin.SystemAdmin;
import domain.models.users.systemAdmin.SystemPermission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.time.LocalDateTime;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SystemAdmin Tests")
class SystemAdminTest {

    private SystemAdmin juniorAdmin;
    private SystemAdmin admin;
    private SystemAdmin superAdmin;

    @BeforeEach
    void setUp() {
        juniorAdmin = new SystemAdmin("John", "Doe", "Michael", "john@email.com", "+1234567890", "password123", "emp123", AdminLevel.JUNIOR_ADMIN);
        admin = new SystemAdmin("Jane", "Smith", "Ann", "jane@email.com", "+9876543210", "pass456", "emp456", AdminLevel.ADMIN);
        superAdmin = new SystemAdmin("Bob", "Johnson", "Lee", "bob@email.com", "+1111111111", "pass789", "emp789", AdminLevel.SUPER_ADMIN);
    }

    @Test
    @DisplayName("Should create system admin with junior level")
    void shouldCreateJuniorAdmin() {
        assertNotNull(juniorAdmin);
        assertEquals("emp123", juniorAdmin.getId());
        assertEquals(AdminLevel.JUNIOR_ADMIN, juniorAdmin.getLevel());
        assertNotNull(juniorAdmin.getPermissions());
        assertNotNull(juniorAdmin.getAuditLog());
        assertTrue(juniorAdmin.getAuditLog().isEmpty());
        assertNotNull(juniorAdmin.getLastLoginAt());
    }

    @Test
    @DisplayName("Should create system admin with admin level")
    void shouldCreateAdmin() {
        assertEquals(AdminLevel.ADMIN, admin.getLevel());
    }

    @Test
    @DisplayName("Should create system admin with super admin level")
    void shouldCreateSuperAdmin() {
        assertEquals(AdminLevel.SUPER_ADMIN, superAdmin.getLevel());
    }

    @Test
    @DisplayName("Should have correct default permissions for junior admin")
    void shouldHaveCorrectJuniorAdminPermissions() {
        Set<SystemPermission> perms = juniorAdmin.getPermissions();

        assertTrue(perms.contains(SystemPermission.VIEW_CARS));
        assertTrue(perms.contains(SystemPermission.VIEW_SPARE_PARTS));
        assertTrue(perms.contains(SystemPermission.VIEW_ORDERS));
        assertTrue(perms.contains(SystemPermission.VIEW_TEST_DRIVES));

        assertFalse(perms.contains(SystemPermission.CREATE_USER));
        assertFalse(perms.contains(SystemPermission.MANAGE_CARS));
        assertFalse(perms.contains(SystemPermission.MANAGE_PERMISSIONS));
    }

    @Test
    @DisplayName("Should have correct default permissions for admin")
    void shouldHaveCorrectAdminPermissions() {
        Set<SystemPermission> perms = admin.getPermissions();

        assertTrue(perms.contains(SystemPermission.CREATE_USER));
        assertTrue(perms.contains(SystemPermission.MANAGE_CARS));
        assertTrue(perms.contains(SystemPermission.MANAGE_SPARE_PARTS));
        assertTrue(perms.contains(SystemPermission.UPDATE_ORDER));
        assertTrue(perms.contains(SystemPermission.CANCEL_ORDER));

        assertFalse(perms.contains(SystemPermission.MANAGE_PERMISSIONS));
        assertFalse(perms.contains(SystemPermission.SYSTEM_CONFIG));
    }

    @Test
    @DisplayName("Should have all permissions for super admin")
    void shouldHaveAllPermissionsForSuperAdmin() {
        Set<SystemPermission> perms = superAdmin.getPermissions();

        assertEquals(SystemPermission.values().length, perms.size());
        for (SystemPermission perm : SystemPermission.values()) {
            assertTrue(perms.contains(perm));
        }
    }

    @Test
    @DisplayName("Should check permission for junior admin")
    void shouldCheckPermissionForJuniorAdmin() {
        assertDoesNotThrow(() -> juniorAdmin.checkPermission(SystemPermission.VIEW_CARS));

        assertThrows(DomainValidationException.class, () -> {
            juniorAdmin.checkPermission(SystemPermission.CREATE_USER);
        });
    }

    @Test
    @DisplayName("Should check permission for admin")
    void shouldCheckPermissionForAdmin() {
        assertDoesNotThrow(() -> admin.checkPermission(SystemPermission.CREATE_USER));

        assertThrows(DomainValidationException.class, () -> {
            admin.checkPermission(SystemPermission.MANAGE_PERMISSIONS);
        });
    }

    @Test
    @DisplayName("Should check permission for super admin")
    void shouldCheckPermissionForSuperAdmin() {
        assertDoesNotThrow(() -> superAdmin.checkPermission(SystemPermission.MANAGE_PERMISSIONS));
        assertDoesNotThrow(() -> superAdmin.checkPermission(SystemPermission.SYSTEM_CONFIG));
    }

    @Test
    @DisplayName("Should check hasPermission for junior admin")
    void shouldCheckHasPermissionForJuniorAdmin() {
        assertTrue(juniorAdmin.hasPermission(SystemPermission.VIEW_CARS));
        assertFalse(juniorAdmin.hasPermission(SystemPermission.CREATE_USER));
    }

    @Test
    @DisplayName("Should check hasPermission for super admin")
    void shouldCheckHasPermissionForSuperAdmin() {
        assertTrue(superAdmin.hasPermission(SystemPermission.MANAGE_PERMISSIONS));
        assertTrue(superAdmin.hasPermission(SystemPermission.SYSTEM_CONFIG));
    }

    @Test
    @DisplayName("Should add permission")
    void shouldAddPermission() {
        assertFalse(juniorAdmin.hasPermission(SystemPermission.CREATE_USER));

        juniorAdmin.addPermission(SystemPermission.CREATE_USER);

        assertTrue(juniorAdmin.hasPermission(SystemPermission.CREATE_USER));
        assertDoesNotThrow(() -> juniorAdmin.checkPermission(SystemPermission.CREATE_USER));
    }

    @Test
    @DisplayName("Should remove permission")
    void shouldRemovePermission() {
        assertTrue(juniorAdmin.hasPermission(SystemPermission.VIEW_CARS));

        juniorAdmin.removePermission(SystemPermission.VIEW_CARS);

        assertFalse(juniorAdmin.hasPermission(SystemPermission.VIEW_CARS));
        assertThrows(DomainValidationException.class, () -> {
            juniorAdmin.checkPermission(SystemPermission.VIEW_CARS);
        });
    }

    @Test
    @DisplayName("Should log action")
    void shouldLogAction() {
        juniorAdmin.logAction("TEST_ACTION", "Test details");

        assertEquals(1, juniorAdmin.getAuditLog().size());
        AuditLogEntry entry = juniorAdmin.getAuditLog().get(0);
        assertEquals("emp123", entry.getAdminId());
        assertEquals("TEST_ACTION", entry.getAction());
        assertEquals("Test details", entry.getDetails());
        assertNotNull(entry.getTimestamp());
    }

    @Test
    @DisplayName("Should return unmodifiable audit log")
    void shouldReturnUnmodifiableAuditLog() {
        juniorAdmin.logAction("ACTION1", "Details1");

        assertThrows(UnsupportedOperationException.class, () -> {
            juniorAdmin.getAuditLog().add(new AuditLogEntry("id", "action", "details", LocalDateTime.now()));
        });
    }

    @Test
    @DisplayName("Should update lastLoginAt on login")
    void shouldUpdateLastLoginAtOnLogin() {
        LocalDateTime before = juniorAdmin.getLastLoginAt();

        try { Thread.sleep(10); } catch (InterruptedException e) {}
        juniorAdmin.login();

        assertTrue(juniorAdmin.getLastLoginAt().isAfter(before));
    }

    @Test
    @DisplayName("Should add audit log entry on login")
    void shouldAddAuditLogOnLogin() {
        int initialSize = juniorAdmin.getAuditLog().size();

        juniorAdmin.login();

        assertEquals(initialSize + 1, juniorAdmin.getAuditLog().size());
        AuditLogEntry entry = juniorAdmin.getAuditLog().get(initialSize);
        assertEquals("LOGIN", entry.getAction());
        assertEquals("Admin logged in", entry.getDetails());
    }

    @Test
    @DisplayName("Should check canPromoteTo for junior admin")
    void shouldCheckCanPromoteToForJuniorAdmin() {
        assertFalse(juniorAdmin.canPromoteTo(AdminLevel.ADMIN, admin));

        assertFalse(juniorAdmin.canPromoteTo(AdminLevel.SUPER_ADMIN, admin));

        assertFalse(admin.canPromoteTo(AdminLevel.JUNIOR_ADMIN, juniorAdmin));

        assertTrue(admin.canPromoteTo(AdminLevel.ADMIN, juniorAdmin));
    }

    @Test
    @DisplayName("Should check canPromoteTo for super admin")
    void shouldCheckCanPromoteToForSuperAdmin() {
        assertTrue(superAdmin.canPromoteTo(AdminLevel.ADMIN, juniorAdmin));

        assertTrue(superAdmin.canPromoteTo(AdminLevel.SUPER_ADMIN, juniorAdmin));

        assertTrue(superAdmin.canPromoteTo(AdminLevel.SUPER_ADMIN, admin));

        assertFalse(superAdmin.canPromoteTo(AdminLevel.ADMIN, admin));

        assertFalse(superAdmin.canPromoteTo(AdminLevel.JUNIOR_ADMIN, admin));
    }
}