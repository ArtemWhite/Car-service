package dealerShipOrder.application.services.testDriveService.manager;

import dealerShipOrder.application.dtos.response.testDriveResponse.TestDriveResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface TestDriveManagerService {
    TestDriveResponse assignManager(String requestId);
    List<TestDriveResponse> getMyRequests();
    List<TestDriveResponse> getPendingRequests();
    TestDriveResponse confirmRequest(String requestId, LocalDateTime time);
    TestDriveResponse completeRequest(String requestId);
    TestDriveResponse markNoShow(String requestId);
}