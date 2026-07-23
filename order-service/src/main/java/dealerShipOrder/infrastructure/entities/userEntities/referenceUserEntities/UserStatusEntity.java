package dealerShipOrder.infrastructure.entities.userEntities.referenceUserEntities;

import javax.persistence.*;

import dealerShipOrder.infrastructure.entities.BaseEntity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_statuses")
@Getter
@Setter
public class UserStatusEntity extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "can_authenticate")
    private boolean canAuthenticate;

    @Column(name = "can_be_restored")
    private boolean canBeRestored;
}