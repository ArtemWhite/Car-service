package domainTest.user.systemAdmin;

import domain.models.users.systemAdmin.SystemPermission;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("SystemPermission Enum Tests")
class SystemPermissionTest {

    @Test
    @DisplayName("Should have all 17 permissions")
    void shouldHaveAllPermissions() {
        SystemPermission[] perms = SystemPermission.values();
        assertEquals(17, perms.length);
    }
}
