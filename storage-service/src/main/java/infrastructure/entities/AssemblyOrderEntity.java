package infrastructure.entities;

import domain.models.assembly.AssemblyOrderStatus;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "assembly_orders")
@Getter
@Setter
@Where(clause = "removed = false")
public class AssemblyOrderEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "source_order_id", nullable = false)
    private String sourceOrderId;

    @Column(name = "order_type", nullable = false, length = 50)
    private String orderType;

    @Column(name = "car_id")
    private String carId;

    @Column(name = "configuration_id")
    private String configurationId;

    @Column(name = "car_model_id")
    private String carModelId;

    @Column(name = "status", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private AssemblyOrderStatus status;

    @Column(name = "responsible_warehouse_admin_id")
    private String responsibleWarehouseAdminId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "removed", nullable = false)
    private boolean removed;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        removed = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}