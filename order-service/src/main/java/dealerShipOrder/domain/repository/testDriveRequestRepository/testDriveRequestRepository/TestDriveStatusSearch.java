package dealerShipOrder.domain.repository.testDriveRequestRepository.testDriveRequestRepository;

import dealerShipOrder.domain.models.testDriveRequest.TestDriveRequest;
import dealerShipOrder.domain.models.testDriveRequest.TestDriveStatus;

import java.util.List;

public interface TestDriveStatusSearch {
    List<TestDriveRequest> findByStatus(TestDriveStatus status);
    List<TestDriveRequest> findByStatusIn(List<TestDriveStatus> statuses);
    long countByStatus(TestDriveStatus status);
}
