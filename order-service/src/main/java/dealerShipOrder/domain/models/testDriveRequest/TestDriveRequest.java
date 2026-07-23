package dealerShipOrder.domain.models.testDriveRequest;

import dealerShipOrder.domain.models.expection.DomainValidationException;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TestDriveRequest
{
    private final String id;
    private final String clientId;
    private final String carId;
    private String managerId;
    private LocalDateTime requestedTime;
    private LocalDateTime confirmedTime;
    private TestDriveStatus status;
    private String notes;

    public TestDriveRequest(String id, String clientId, String carId, LocalDateTime startTime) {
        if (startTime.isBefore(LocalDateTime.now())) {
            throw new DomainValidationException("Start time cannot be in the past");
        }

        this.id = id;
        this.clientId = clientId;
        this.carId = carId;
        this.requestedTime = startTime;
        this.status = TestDriveStatus.PENDING;
    }

    public void assignManager(String managerId) {
        if (this.managerId != null) {
            throw new DomainValidationException("Manager already assigned to this test drive");
        }
        if (status != TestDriveStatus.PENDING) {
            throw new DomainValidationException("Can only assign manager to pending requests");
        }
        this.managerId = managerId;
        this.status = TestDriveStatus.CONFIRMED;

        this.confirmedTime = this.requestedTime;
    }

    public void reschedule(LocalDateTime newTime) {
        if (status == TestDriveStatus.COMPLETED || status == TestDriveStatus.CANCELLED) {
            throw new DomainValidationException("Cannot reschedule completed or cancelled test drive");
        }
        if (newTime.isBefore(LocalDateTime.now())) {
            throw new DomainValidationException("New time cannot be in the past");
        }
        if (newTime.equals(this.requestedTime)) {
            throw new DomainValidationException("New time is the same as current time");
        }
        this.requestedTime = newTime;
        this.confirmedTime = null;
        this.status = TestDriveStatus.PENDING;
    }

    public void confirmTime(LocalDateTime time) {
        if (status == TestDriveStatus.CANCELLED) {
            throw new DomainValidationException("Cannot confirm cancelled test drive");
        }
        if (time.isBefore(LocalDateTime.now())) {
            throw new DomainValidationException("Confirm time cannot be in the past");
        }
        this.confirmedTime = time;
        this.status = TestDriveStatus.CONFIRMED;
    }

    public void cancel() {
        if (status == TestDriveStatus.COMPLETED) {
            throw new DomainValidationException("Cannot cancel completed test drive");
        }
        if (status == TestDriveStatus.CANCELLED) {
            throw new DomainValidationException("Test drive already cancelled");
        }
        this.status = TestDriveStatus.CANCELLED;
    }

    public void complete()
    {
        if (status != TestDriveStatus.CONFIRMED) {
            throw new DomainValidationException("Can only complete confirmed test drives");
        }
        if (confirmedTime.isAfter(LocalDateTime.now())) {
            throw new DomainValidationException("Cannot complete future test drive");
        }
        this.status = TestDriveStatus.COMPLETED;
    }

    public void markNoShow() {
        if (status != TestDriveStatus.CONFIRMED) {
            throw new DomainValidationException("Can only mark no-show for confirmed test drives");
        }
        this.status = TestDriveStatus.NO_SHOW;
    }

    public boolean isUpcoming() {
        return (status == TestDriveStatus.PENDING || status == TestDriveStatus.CONFIRMED)
                && (confirmedTime != null ? confirmedTime.isAfter(LocalDateTime.now())
                : requestedTime.isAfter(LocalDateTime.now()));
    }

    public boolean isPast() {
        return (status == TestDriveStatus.COMPLETED || status == TestDriveStatus.NO_SHOW) ||
                (confirmedTime != null && confirmedTime.isBefore(LocalDateTime.now()));
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public void forceRequestedTime(LocalDateTime time) {
        this.requestedTime = time;
    }
}

