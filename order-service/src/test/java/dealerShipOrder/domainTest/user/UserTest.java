package dealerShipOrder.domainTest.user;

import dealerShipOrder.domain.models.expection.DomainValidationException;
import dealerShipOrder.domain.models.users.User;
import dealerShipOrder.domain.models.users.UserStatus;
import dealerShipOrder.domain.models.users.UserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User Tests")
class UserTest {

    private TestUser user;

    @BeforeEach
    void setUp() {
        user = new TestUser("id123", "John", "Doe", "Michael", "john@email.com", "+1234567890", "password123");
    }

    @Test
    @DisplayName("Should create user with all fields")
    void shouldCreateUserWithAllFields() {
        assertNotNull(user);
        assertEquals("id123", user.getId());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("Michael", user.getMiddleName());
        assertEquals("john@email.com", user.getEmail());
        assertEquals("+1234567890", user.getPhone());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertNotNull(user.getRegisteredAt());
        assertNotNull(user.getLastActiveAt());
        assertNotNull(user.getLastPasswordChangeAt());
    }

    @Test
    @DisplayName("Should set default status to ACTIVE")
    void shouldSetDefaultStatusActive() {
        assertEquals(UserStatus.ACTIVE, user.getStatus());
    }

    @Test
    @DisplayName("Should set timestamps on creation")
    void shouldSetTimestampsOnCreation() {
        LocalDateTime before = LocalDateTime.now();
        TestUser newUser = new TestUser("id2", "Jane", "Smith", null, "jane@email.com", "+9876543210", "pass");
        LocalDateTime after = LocalDateTime.now();

        assertNotNull(newUser.getRegisteredAt());
        assertNotNull(newUser.getLastActiveAt());
        assertNotNull(newUser.getLastPasswordChangeAt());
        assertTrue(newUser.getRegisteredAt().isAfter(before) || newUser.getRegisteredAt().isEqual(before));
        assertTrue(newUser.getRegisteredAt().isBefore(after) || newUser.getRegisteredAt().isEqual(after));
    }

    @Test
    @DisplayName("Should authenticate with correct password")
    void shouldAuthenticateWithCorrectPassword() {
        assertTrue(user.authenticate("password123"));
    }

    @Test
    @DisplayName("Should not authenticate with incorrect password")
    void shouldNotAuthenticateWithIncorrectPassword() {
        assertFalse(user.authenticate("wrongpassword"));
    }

    @Test
    @DisplayName("Should change password successfully")
    void shouldChangePasswordSuccessfully() {
        user.changePassword("password123", "newPassword456");

        assertTrue(user.authenticate("newPassword456"));
        assertFalse(user.authenticate("password123"));
    }

    @Test
    @DisplayName("Should throw exception when changing password with wrong old password")
    void shouldThrowWhenChangingPasswordWithWrongOldPassword() {
        assertThrows(DomainValidationException.class, () -> {
            user.changePassword("wrongOld", "newPassword");
        });
    }

    @Test
    @DisplayName("Should update lastPasswordChangeAt after password change")
    void shouldUpdateLastPasswordChangeAt() {
        LocalDateTime before = user.getLastPasswordChangeAt();

        try { Thread.sleep(10); } catch (InterruptedException e) {}
        user.changePassword("password123", "newPassword");

        assertTrue(user.getLastPasswordChangeAt().isAfter(before));
    }

    @Test
    @DisplayName("Should update personal info")
    void shouldUpdatePersonalInfo() {
        user.updatePersonalInfo("Jonathan", "Smith", "David");

        assertEquals("Jonathan", user.getFirstName());
        assertEquals("Smith", user.getLastName());
        assertEquals("David", user.getMiddleName());
    }

    @Test
    @DisplayName("Should update contact info")
    void shouldUpdateContactInfo() {
        user.updateContactInfo("newemail@email.com", "+1111111111");

        assertEquals("newemail@email.com", user.getEmail());
        assertEquals("+1111111111", user.getPhone());
    }

    @Test
    @DisplayName("Should update lastActiveAt when updating personal info")
    void shouldUpdateLastActiveAtOnPersonalInfoUpdate() {
        LocalDateTime before = user.getLastActiveAt();

        try { Thread.sleep(10); } catch (InterruptedException e) {}
        user.updatePersonalInfo("New", "Name", null);

        assertTrue(user.getLastActiveAt().isAfter(before));
    }

    @Test
    @DisplayName("Should deactivate user")
    void shouldDeactivateUser() {
        user.deactivate();
        assertEquals(UserStatus.INACTIVE, user.getStatus());
    }

    @Test
    @DisplayName("Should block user")
    void shouldBlockUser() {
        user.block();
        assertEquals(UserStatus.BLOCKED, user.getStatus());
    }

    @Test
    @DisplayName("Should activate user")
    void shouldActivateUser() {
        user.deactivate();
        assertEquals(UserStatus.INACTIVE, user.getStatus());

        user.activate();
        assertEquals(UserStatus.ACTIVE, user.getStatus());
    }

    @Test
    @DisplayName("Should return full name correctly")
    void shouldReturnFullName() {
        assertEquals("John Doe Michael", user.getFullName());

        TestUser userWithoutMiddle = new TestUser("id2", "Jane", "Smith", null, "jane@email.com", "+123", "pass");
        assertEquals("Jane Smith null", userWithoutMiddle.getFullName());
    }

    private static class TestUser extends User {
        public TestUser(String id, String firstName, String lastName, String middleName,
                        String email, String phone, String password) {
            super(id, firstName, lastName, middleName, email, phone, password, UserType.CLIENT);
        }
    }
}