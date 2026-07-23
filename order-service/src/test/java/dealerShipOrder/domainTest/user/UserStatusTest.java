package dealerShipOrder.domainTest.user;

import dealerShipOrder.domain.models.users.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserStatus Enum Tests")
class UserStatusTest {

    @Test
    @DisplayName("Should have all 5 statuses")
    void shouldHaveAllStatuses() {
        UserStatus[] statuses = UserStatus.values();
        assertEquals(5, statuses.length);
    }

    @Test
    @DisplayName("Should have correct display names")
    void shouldHaveCorrectDisplayNames() {
        assertEquals("Активен", UserStatus.ACTIVE.getDisplayName());
        assertEquals("Неактивен", UserStatus.INACTIVE.getDisplayName());
        assertEquals("Заблокирован", UserStatus.BLOCKED.getDisplayName());
        assertEquals("Ожидает подтверждения", UserStatus.PENDING_VERIFICATION.getDisplayName());
        assertEquals("Удалён", UserStatus.DELETED.getDisplayName());
    }

    @Test
    @DisplayName("Should have correct canAuthenticate values")
    void shouldHaveCorrectCanAuthenticate() {
        assertTrue(UserStatus.ACTIVE.canAuthenticate());
        assertFalse(UserStatus.INACTIVE.canAuthenticate());
        assertFalse(UserStatus.BLOCKED.canAuthenticate());
        assertFalse(UserStatus.PENDING_VERIFICATION.canAuthenticate());
        assertFalse(UserStatus.DELETED.canAuthenticate());
    }

    @Test
    @DisplayName("Should have correct canBeRestored values")
    void shouldHaveCorrectCanBeRestored() {
        assertTrue(UserStatus.ACTIVE.canBeRestored());
        assertTrue(UserStatus.INACTIVE.canBeRestored());
        assertFalse(UserStatus.BLOCKED.canBeRestored());
        assertTrue(UserStatus.PENDING_VERIFICATION.canBeRestored());
        assertFalse(UserStatus.DELETED.canBeRestored());
    }
}