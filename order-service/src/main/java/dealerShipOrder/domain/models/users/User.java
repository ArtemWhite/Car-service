package dealerShipOrder.domain.models.users;

import dealerShipOrder.domain.models.expection.DomainValidationException;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public abstract class User
{
    private final String id;
    private String firstName;
    private String lastName;
    private String middleName;
    private String email;
    private String phone;
    private String passwordHash;
    private UserStatus status;
    private UserType userType;
    private final LocalDateTime registeredAt;
    private LocalDateTime lastActiveAt;
    private LocalDateTime lastPasswordChangeAt;

    protected User(String id, String firstName, String lastName, String middleName, String email, String phone, String password, UserType userType) {
        this.id = id;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.passwordHash = hashPassword(password);
        this.status = UserStatus.ACTIVE;
        this.registeredAt = LocalDateTime.now();
        this.lastActiveAt = LocalDateTime.now();
        this.lastPasswordChangeAt = LocalDateTime.now();
        this.userType = userType;
    }

    public boolean authenticate(String password) {
        return this.passwordHash.equals(hashPassword(password));
    }

    public void changePassword(String oldPassword, String newPassword) {
        if (!authenticate(oldPassword)) {
            throw new DomainValidationException("Old password is incorrect");
        }
        this.passwordHash = hashPassword(newPassword);
        this.lastPasswordChangeAt = LocalDateTime.now();
    }

    public void updatePersonalInfo(String firstName, String lastName, String middleName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.middleName = middleName;
        this.lastActiveAt = LocalDateTime.now();
    }

    public void updateContactInfo(String email, String phone) {
        this.email = email;
        this.phone = phone;
        this.lastActiveAt = LocalDateTime.now();
    }

    public void updateLastActive() {
        this.lastActiveAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.status = UserStatus.INACTIVE;
    }

    public void block() {
        this.status = UserStatus.BLOCKED;
    }

    public void activate() {
        this.status = UserStatus.ACTIVE;
    }

    public String getFullName()
    {
        return firstName + " " + lastName + " " + middleName;
    }

    private String hashPassword(String password) {
        return Integer.toHexString(password.hashCode());
    }
}
