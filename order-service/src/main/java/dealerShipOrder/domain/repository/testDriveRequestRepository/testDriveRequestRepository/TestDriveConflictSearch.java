package dealerShipOrder.domain.repository.testDriveRequestRepository.testDriveRequestRepository;

import dealerShipOrder.domain.models.testDriveRequest.TestDriveRequest;

import java.time.LocalDateTime;
import java.util.List;

public interface TestDriveConflictSearch {
    List<TestDriveRequest> findConflictingRequests(String carId, LocalDateTime time);
    List<TestDriveRequest> findConflictingForManager(String managerId, LocalDateTime time);
    boolean hasConflict(String carId, LocalDateTime time);
}
