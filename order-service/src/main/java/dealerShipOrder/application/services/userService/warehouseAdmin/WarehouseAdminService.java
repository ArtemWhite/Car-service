package dealerShipOrder.application.services.userService.warehouseAdmin;

import dealerShipOrder.application.dtos.request.userRequest.ChangePasswordRequest;
import dealerShipOrder.application.dtos.request.userRequest.OperationHistoryRequest;
import dealerShipOrder.application.dtos.request.userRequest.UpdateUserRequest;
import dealerShipOrder.application.dtos.response.userResponse.users.WarehouseAdminResponse;

import java.util.List;
import java.util.Set;

public interface WarehouseAdminService {

    WarehouseAdminResponse getMyProfile();
    WarehouseAdminResponse getUserById(String id);
    WarehouseAdminResponse updateOwnProfile(UpdateUserRequest request);
    WarehouseAdminResponse changeOwnPassword(ChangePasswordRequest request);

    WarehouseAdminResponse assignToSection(String sectionId);
    WarehouseAdminResponse removeFromSection(String sectionId);
    Set<String> getManagedSections();

    WarehouseAdminResponse startShift();
    WarehouseAdminResponse endShift();
    boolean isOnDuty();

    List<OperationHistoryRequest> getOperationHistory();
    List<OperationHistoryRequest> getOperationsByDate(String from, String to);
    List<OperationHistoryRequest> getOperationsByType(String operationType);
}