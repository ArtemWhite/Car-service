package dealerShipOrder.application.services.userService.manager;

import dealerShipOrder.application.dtos.request.userRequest.ChangePasswordRequest;
import dealerShipOrder.application.dtos.request.userRequest.UpdateUserRequest;
import dealerShipOrder.application.dtos.response.userResponse.users.ManagerResponse;

import java.util.List;

public interface ManagerService {

    ManagerResponse getMyProfile();
    ManagerResponse getUserById(String id);
    ManagerResponse updateOwnProfile(UpdateUserRequest request);
    ManagerResponse changeOwnPassword(ChangePasswordRequest request);

    ManagerResponse setAvailability(boolean available);
    List<ManagerResponse> getAllManagers();
    List<ManagerResponse> getAvailableManagers();
    ManagerResponse promote(String newPosition);
}