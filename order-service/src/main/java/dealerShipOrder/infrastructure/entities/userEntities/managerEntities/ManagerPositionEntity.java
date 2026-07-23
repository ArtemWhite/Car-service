package dealerShipOrder.infrastructure.entities.userEntities.managerEntities;

import javax.persistence.*;

import dealerShipOrder.infrastructure.entities.BaseEntity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "manager_positions")
@Getter
@Setter
public class ManagerPositionEntity extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "max_concurrent_orders")
    private int maxConcurrentOrders;

    @Column(name = "max_concurrent_test_drives")
    private int maxConcurrentTestDrives;
}