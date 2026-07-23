package dealerShipOrder.domain.repository.testDriveRequestRepository.testDriveRequestRepository;


import dealerShipOrder.domain.models.testDriveRequest.TestDriveRequest;
import dealerShipOrder.domain.models.testDriveRequest.TestDriveStatus;

import java.util.List;

public interface TestDriveClientSearch {
    List<TestDriveRequest> findByClientId(String clientId);
    List<TestDriveRequest> findByClientIdAndStatus(String clientId, TestDriveStatus status);
    List<TestDriveRequest> findUpcomingByClientId(String clientId);
    boolean hasActiveRequestForClient(String clientId);
    long countByClientId(String clientId);
}
