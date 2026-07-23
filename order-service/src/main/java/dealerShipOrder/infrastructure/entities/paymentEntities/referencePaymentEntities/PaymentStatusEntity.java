package dealerShipOrder.infrastructure.entities.paymentEntities.referencePaymentEntities;

import javax.persistence.*;

import dealerShipOrder.infrastructure.entities.BaseEntity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "payment_statuses")
@Getter
@Setter
public class PaymentStatusEntity extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;
}