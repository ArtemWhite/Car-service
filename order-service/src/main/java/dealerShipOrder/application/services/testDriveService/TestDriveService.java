package dealerShipOrder.application.services.testDriveService;

import dealerShipOrder.application.dtos.request.testDriveRequest.TestDriveFilterRequest;
import dealerShipOrder.application.dtos.response.testDriveResponse.TestDriveListResponse;
import dealerShipOrder.application.dtos.response.testDriveResponse.TestDriveResponse;

import java.util.List;

public interface TestDriveService{
    TestDriveResponse getTestDriveById(String id);
    List<TestDriveResponse> getAllTestDrives();
    TestDriveListResponse getTestDrivesWithFilters(TestDriveFilterRequest filter);
}
