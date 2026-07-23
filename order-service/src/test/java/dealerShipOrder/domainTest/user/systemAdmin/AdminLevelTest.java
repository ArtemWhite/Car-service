package dealerShipOrder.domainTest.user.systemAdmin;

import dealerShipOrder.domain.models.users.systemAdmin.AdminLevel;
import dealerShipOrder.domain.models.users.systemAdmin.SystemPermission;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AdminLevel Enum Tests")
class AdminLevelTest {

    @Test
    @DisplayName("Should have all 3 admin levels")
    void shouldHaveAllLevels() {
        AdminLevel[] levels = AdminLevel.values();
        assertEquals(3, levels.length);
    }

    @Test
    @DisplayName("Should have correct display names")
    void shouldHaveCorrectDisplayNames() {
        assertEquals("Младший администратор", AdminLevel.JUNIOR_ADMIN.getDisplayName());
        assertEquals("Администратор", AdminLevel.ADMIN.getDisplayName());
        assertEquals("Супер-администратор", AdminLevel.SUPER_ADMIN.getDisplayName());
    }

    @Test
    @DisplayName("Should have correct level numbers")
    void shouldHaveCorrectLevelNumbers() {
        assertEquals(1, AdminLevel.JUNIOR_ADMIN.getLevel());
        assertEquals(2, AdminLevel.ADMIN.getLevel());
        assertEquals(3, AdminLevel.SUPER_ADMIN.getLevel());
    }

    @Test
    @DisplayName("Should have correct default permissions for JUNIOR_ADMIN")
    void shouldHaveCorrectJuniorAdminPermissions() {
        Set<SystemPermission> perms = AdminLevel.JUNIOR_ADMIN.getDefaultPermissions();

        assertTrue(perms.contains(SystemPermission.VIEW_CARS));
        assertTrue(perms.contains(SystemPermission.VIEW_SPARE_PARTS));
        assertTrue(perms.contains(SystemPermission.VIEW_ORDERS));
        assertTrue(perms.contains(SystemPermission.VIEW_TEST_DRIVES));
        assertEquals(4, perms.size());
    }

    @Test
    @DisplayName("Should have correct default permissions for ADMIN")
    void shouldHaveCorrectAdminPermissions() {
        Set<SystemPermission> perms = AdminLevel.ADMIN.getDefaultPermissions();

        assertTrue(perms.contains(SystemPermission.CREATE_USER));
        assertTrue(perms.contains(SystemPermission.MANAGE_CARS));
        assertTrue(perms.contains(SystemPermission.MANAGE_SPARE_PARTS));
        assertFalse(perms.contains(SystemPermission.MANAGE_PERMISSIONS));
        assertFalse(perms.contains(SystemPermission.SYSTEM_CONFIG));
    }

    @Test
    @DisplayName("Should have all permissions for SUPER_ADMIN")
    void shouldHaveAllPermissionsForSuperAdmin() {
        Set<SystemPermission> perms = AdminLevel.SUPER_ADMIN.getDefaultPermissions();

        assertEquals(SystemPermission.values().length, perms.size());
    }
}

