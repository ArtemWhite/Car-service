package dealerShipOrder.infrastructure.entities.orderEntities;

import javax.persistence.*;

import dealerShipOrder.infrastructure.entities.orderEntities.referenceOrderEntities.OrderStatusEntity;
import dealerShipOrder.infrastructure.entities.orderEntities.referenceOrderEntities.OrderTypeEntity;
import dealerShipOrder.infrastructure.entities.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Where(clause = "removed = false")
@Getter
@Setter
public class OrderEntity extends BaseEntity {

    @Column(name = "client_id", nullable = false)
    private String clientId;

    @Column(name = "manager_id")
    private String managerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", nullable = false)
    private OrderTypeEntity type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", nullable = false)
    private OrderStatusEntity status;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "car_id")
    private String carId;

    @Column(name = "configuration_id")
    private String configurationId;

    @Column(name = "car_model_id")
    private String carModelId;

    @Column(length = 500)
    private String notes;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderHistoryEntryEntity> history = new ArrayList<>();

}