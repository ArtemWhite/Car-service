package dealerShipOrder.infrastructure.entities.testDriveRequestEntities;

import javax.persistence.*;

import dealerShipOrder.infrastructure.entities.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.time.Instant;

@Entity
@Table(name = "test_drive_requests")
@Where(clause = "removed = false")
@Getter
@Setter
public class TestDriveRequestEntity extends BaseEntity {

    @Column(name = "client_id", nullable = false)
    private String clientId;

    @Column(name = "car_id", nullable = false)
    private String carId;

    @Column(name = "manager_id")
    private String managerId;

    @Column(name = "requested_time", nullable = false)
    private Instant requestedTime;

    @Column(name = "confirmed_time")
    private Instant confirmedTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", nullable = false)
    private TestDriveStatusEntity status;

    @Column(length = 500)
    private String notes;
}