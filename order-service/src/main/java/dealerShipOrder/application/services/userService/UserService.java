package dealerShipOrder.application.services.userService;

import dealerShipOrder.application.dtos.request.userRequest.ChangePasswordRequest;
import dealerShipOrder.application.dtos.request.userRequest.UpdateUserRequest;
import dealerShipOrder.application.dtos.response.userResponse.UserBaseResponse;

public interface UserService
{
    UserBaseResponse getUserById(String id);

    UserBaseResponse updateOwnProfile(UpdateUserRequest request);
    UserBaseResponse changeOwnPassword(ChangePasswordRequest request);

    UserBaseResponse updateOwnProfile(String userId, UpdateUserRequest request);
    UserBaseResponse changeOwnPassword(String userId, ChangePasswordRequest request);
}