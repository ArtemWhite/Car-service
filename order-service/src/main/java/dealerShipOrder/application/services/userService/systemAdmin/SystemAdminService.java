package dealerShipOrder.application.services.userService.systemAdmin;

import dealerShipOrder.application.dtos.request.userRequest.*;
import dealerShipOrder.application.dtos.response.userResponse.UserBaseResponse;
import dealerShipOrder.application.dtos.response.userResponse.UserListResponse;
import dealerShipOrder.application.dtos.response.userResponse.users.SystemAdminResponse;
import dealerShipOrder.domain.models.users.systemAdmin.SystemPermission;

import java.util.List;

public interface SystemAdminService {

    SystemAdminResponse getMyProfile();
    SystemAdminResponse getUserById(String id);
    SystemAdminResponse updateOwnProfile(UpdateUserRequest request);
    SystemAdminResponse changeOwnPassword(ChangePasswordRequest request);

    UserBaseResponse createUser(CreateUserRequest request);
    UserBaseResponse updateUser(String userId, UpdateUserRequest request);
    void deleteUser(String userId, String reason);
    UserBaseResponse blockUser(String userId, String reason);
    UserBaseResponse unblockUser(String userId);
    UserBaseResponse deactivateUser(String userId);

    List<UserBaseResponse> getAllUsers();
    List<UserBaseResponse> getUsersByType(String userType);
    UserBaseResponse getUserDetails(String userId);
    UserListResponse getUsersWithFilters(UserFilterRequest filter);

    SystemAdminResponse grantPermission(String targetAdminId, SystemPermission permission);
    SystemAdminResponse revokePermission(String targetAdminId, SystemPermission permission);
    SystemAdminResponse promoteAdmin(String targetAdminId, String newLevel);
    SystemAdminResponse promoteManager(String managerId, String newPosition);
    SystemAdminResponse promoteWarehouseAdmin(String targetAdminId, String newPosition);

    List<OperationHistoryRequest> getAuditLog();
    List<OperationHistoryRequest> getUserAuditLog(String userId);
    void changeSystemSettings(Object settings);
}