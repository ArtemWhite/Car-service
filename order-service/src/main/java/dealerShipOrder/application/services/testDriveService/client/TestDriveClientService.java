package dealerShipOrder.application.services.testDriveService.client;

import dealerShipOrder.application.dtos.request.testDriveRequest.CreateTestDriveRequest;
import dealerShipOrder.application.dtos.response.testDriveResponse.TestDriveResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface TestDriveClientService {
    TestDriveResponse createRequest(CreateTestDriveRequest request);
    List<TestDriveResponse> getMyRequests();
    TestDriveResponse cancelRequest(String requestId, String reason);
    TestDriveResponse rescheduleRequest(String requestId, LocalDateTime newTime);
}