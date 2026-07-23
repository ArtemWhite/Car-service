package dealerShipOrder.infrastructure.entities.userEntities;

import javax.persistence.*;

import dealerShipOrder.infrastructure.entities.BaseEntity;
import dealerShipOrder.infrastructure.entities.userEntities.referenceUserEntities.UserStatusEntity;
import dealerShipOrder.infrastructure.entities.userEntities.referenceUserEntities.UserTypeEntity;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.time.Instant;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
@Where(clause = "removed = false")
@Getter
@Setter
public class UserEntity extends BaseEntity {

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "middle_name", length = 50)
    private String middleName;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", nullable = false)
    private UserStatusEntity status;

    @Column(name = "last_active_at")
    private Instant lastActiveAt;

    @Column(name = "last_password_change_at")
    private Instant lastPasswordChangeAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_type_id", nullable = false)
    private UserTypeEntity userType;
}