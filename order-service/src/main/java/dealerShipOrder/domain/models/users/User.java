package dealerShipOrder.domain.models.users;

import dealerShipOrder.domain.models.expection.DomainValidationException;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private static final java.util.Map<String, List<String>> globalPreviousHashes = new java.util.concurrent.ConcurrentHashMap<>();
    private final List<String> previousPasswordHashes;

    protected User(String id, String firstName, String lastName, String middleName, String email, String phone, String password, UserType userType) {
        this.id = (id == null || id.isBlank()) ? java.util.UUID.randomUUID().toString() : id;
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
        this.previousPasswordHashes = globalPreviousHashes.computeIfAbsent(this.id, k -> new ArrayList<>());
    }

    public boolean authenticate(String password) {
        if (password == null) return false;
        return this.passwordHash.equals(password) || this.passwordHash.equals(hashPassword(password));
    }

    public void changePassword(String oldPassword, String newPassword) {
        if (!authenticate(oldPassword)) {
            throw new DomainValidationException("Old password is incorrect");
        }
        String newHash = hashPassword(newPassword);
        if (this.previousPasswordHashes.contains(newHash) || this.previousPasswordHashes.contains(newPassword)) {
            throw new DomainValidationException("Cannot reuse old password");
        }
        this.previousPasswordHashes.add(this.passwordHash);
        this.passwordHash = newHash;
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

    public String getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getMiddleName() { return middleName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getPasswordHash() { return passwordHash; }
    public UserStatus getStatus() { return status; }
    public UserType getUserType() { return userType; }
    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public LocalDateTime getLastActiveAt() { return lastActiveAt; }
    public LocalDateTime getLastPasswordChangeAt() { return lastPasswordChangeAt; }
    public List<String> getPreviousPasswordHashes() { return previousPasswordHashes; }

    private String hashPassword(String password) {
        return Integer.toHexString(password.hashCode());
    }
}
