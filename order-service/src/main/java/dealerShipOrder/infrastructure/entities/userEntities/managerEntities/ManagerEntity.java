package dealerShipOrder.infrastructure.entities.userEntities.managerEntities;

import javax.persistence.*;

import dealerShipOrder.infrastructure.entities.userEntities.UserEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "managers")
@PrimaryKeyJoinColumn(name = "user_id")
@Getter
@Setter
public class ManagerEntity extends UserEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id", nullable = false)
    private ManagerPositionEntity position;

    @Column(name = "max_concurrent_orders")
    private int maxConcurrentOrders;

    @Column(name = "max_concurrent_test_drives")
    private int maxConcurrentTestDrives;

    private boolean available;

    @ElementCollection
    @CollectionTable(name = "manager_orders", joinColumns = @JoinColumn(name = "manager_id"))
    @Column(name = "order_id")
    private List<String> assignedOrderIds = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "manager_test_drives", joinColumns = @JoinColumn(name = "manager_id"))
    @Column(name = "test_drive_id")
    private List<String> managedTestDriveIds = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "manager_test_drive_fleet", joinColumns = @JoinColumn(name = "manager_id"))
    @Column(name = "car_id")
    private List<String> testDriveFleetCarIds = new ArrayList<>();
}