package dealerShipOrder.domain.repository.testDriveRequestRepository.testDriveRequestRepository;

import dealerShipOrder.domain.models.testDriveRequest.TestDriveRequest;
import dealerShipOrder.domain.models.testDriveRequest.TestDriveStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface TestDriveDateSearch {
    List<TestDriveRequest> findByDateRange(LocalDateTime start, LocalDateTime end);
    List<TestDriveRequest> findUpcomingTestDrives();
    List<TestDriveRequest> findPastTestDrives();
    List<TestDriveRequest> findByDateAndStatus(LocalDateTime date, TestDriveStatus status);
    List<TestDriveRequest> findByDateTimeBetween(LocalDateTime start, LocalDateTime end);
}
