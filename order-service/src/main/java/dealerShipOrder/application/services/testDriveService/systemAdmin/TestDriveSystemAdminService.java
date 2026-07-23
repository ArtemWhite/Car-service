package dealerShipOrder.application.services.testDriveService.systemAdmin;

import dealerShipOrder.application.dtos.request.testDriveRequest.UpdateTestDriveRequest;
import dealerShipOrder.application.dtos.response.testDriveResponse.TestDriveResponse;

import java.util.List;

public interface TestDriveSystemAdminService {
    TestDriveResponse updateRequest(String requestId, UpdateTestDriveRequest request);
    void deleteRequest(String requestId, String reason);
    List<TestDriveResponse> getRequestsByStatus(String status);
}