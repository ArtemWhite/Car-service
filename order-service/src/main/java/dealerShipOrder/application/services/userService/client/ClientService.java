package dealerShipOrder.application.services.userService.client;

import dealerShipOrder.application.dtos.request.userRequest.ChangePasswordRequest;
import dealerShipOrder.application.dtos.request.userRequest.UpdateUserRequest;
import dealerShipOrder.application.dtos.response.orderResponse.OrderResponse;
import dealerShipOrder.application.dtos.response.testDriveResponse.TestDriveResponse;
import dealerShipOrder.application.dtos.response.userResponse.users.ClientResponse;

import java.util.List;

public interface ClientService {

    ClientResponse getMyProfile();
    ClientResponse getUserById(String id);
    ClientResponse updateOwnProfile(UpdateUserRequest request);
    ClientResponse changeOwnPassword(ChangePasswordRequest request);

    List<OrderResponse> getMyOrders();
    List<TestDriveResponse> getMyTestDrives();
    ClientResponse subscribeToNewsletter();
    ClientResponse unsubscribeFromNewsletter();
    ClientResponse setPreferredContactMethod(String method);
}