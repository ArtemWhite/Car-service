package dealerShipOrder.domain.repository.testDriveRequestRepository.testDriveRequestRepository;

import dealerShipOrder.domain.models.testDriveRequest.TestDriveRequest;
import dealerShipOrder.domain.models.testDriveRequest.TestDriveStatus;

import java.util.List;

public interface TestDriveManagerSearch {
    List<TestDriveRequest> findByManagerId(String managerId);
    List<TestDriveRequest> findByManagerIdAndStatus(String managerId, TestDriveStatus status);
    List<TestDriveRequest> findUpcomingByManagerId(String managerId);
    long countAssignedToManager(String managerId);
}
