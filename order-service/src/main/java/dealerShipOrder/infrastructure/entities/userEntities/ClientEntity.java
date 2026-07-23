package dealerShipOrder.infrastructure.entities.userEntities;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clients")
@PrimaryKeyJoinColumn(name = "user_id")
@Getter
@Setter
public class ClientEntity extends UserEntity {

    @Column(name = "preferred_contact_method", length = 20)
    private String preferredContactMethod;

    @Column(name = "newsletter_subscribed")
    private boolean newsletterSubscribed;

    @ElementCollection
    @CollectionTable(name = "client_orders", joinColumns = @JoinColumn(name = "client_id"))
    @Column(name = "order_id")
    private List<String> orderIds = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "client_test_drives", joinColumns = @JoinColumn(name = "client_id"))
    @Column(name = "test_drive_id")
    private List<String> testDriveRequestIds = new ArrayList<>();
}