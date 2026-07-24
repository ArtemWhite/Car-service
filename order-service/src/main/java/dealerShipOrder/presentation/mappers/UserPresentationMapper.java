package dealerShipOrder.presentation.mappers;

import dealerShipOrder.application.dtos.request.userRequest.*;
import dealerShipOrder.application.dtos.response.userResponse.UserBaseResponse;
import dealerShipOrder.application.dtos.response.userResponse.UserListResponse;
import dealerShipOrder.application.dtos.response.userResponse.users.ClientResponse;
import dealerShipOrder.application.dtos.response.userResponse.users.ManagerResponse;
import dealerShipOrder.application.dtos.response.userResponse.users.SystemAdminResponse;
import dealerShipOrder.application.dtos.response.userResponse.users.WarehouseAdminResponse;
import dealerShipOrder.presentation.dtos.request.userRequestPresentationDto.UserChangePasswordPresentationRequest;
import dealerShipOrder.presentation.dtos.request.userRequestPresentationDto.UserCreatePresentationRequest;
import dealerShipOrder.presentation.dtos.request.userRequestPresentationDto.UserFilterPresentationRequest;
import dealerShipOrder.presentation.dtos.request.userRequestPresentationDto.UserUpdatePresentationRequest;
import dealerShipOrder.presentation.dtos.response.userResponsePresentationDto.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserPresentationMapper {

    public CreateUserRequest toApplication(UserCreatePresentationRequest request) {
        if (request == null) return null;

        CreateUserRequest target = new CreateUserRequest();
        target.setFirstName(request.getFirstName());
        target.setLastName(request.getLastName());
        target.setMiddleName(request.getMiddleName());
        target.setEmail(request.getEmail());
        target.setPhone(request.getPhone());
        target.setPassword(request.getPassword());
        target.setUserType(request.getUserType());
        target.setEmployeeId(request.getEmployeeId());
        target.setAdminLevel(request.getAdminLevel());
        target.setPosition(request.getPosition());
        target.setManagedSectionIds(request.getManagedSectionIds());

        return target;
    }

    public UpdateUserRequest toApplication(UserUpdatePresentationRequest request, String userId) {
        if (request == null) return null;

        UpdateUserRequest target = new UpdateUserRequest();
        target.setUserId(userId);
        target.setFirstName(request.getFirstName());
        target.setLastName(request.getLastName());
        target.setMiddleName(request.getMiddleName());
        target.setEmail(request.getEmail());
        target.setPhone(request.getPhone());
        target.setStatus(request.getStatus());
        target.setPosition(request.getPosition());
        target.setAvailable(request.getAvailable());
        target.setWarehousePosition(request.getWarehousePosition());
        target.setManagedSectionIds(request.getManagedSectionIds());
        target.setPreferredContactMethod(request.getPreferredContactMethod());
        target.setNewsletterSubscribed(request.getNewsletterSubscribed());

        return target;
    }

    public UpdateUserRequest toApplicationForOwnProfile(UserUpdatePresentationRequest request) {
        if (request == null) return null;

        UpdateUserRequest target = new UpdateUserRequest();
        target.setFirstName(request.getFirstName());
        target.setLastName(request.getLastName());
        target.setMiddleName(request.getMiddleName());
        target.setEmail(request.getEmail());
        target.setPhone(request.getPhone());
        target.setStatus(request.getStatus());
        target.setPosition(request.getPosition());
        target.setAvailable(request.getAvailable());
        target.setWarehousePosition(request.getWarehousePosition());
        target.setManagedSectionIds(request.getManagedSectionIds());
        target.setPreferredContactMethod(request.getPreferredContactMethod());
        target.setNewsletterSubscribed(request.getNewsletterSubscribed());

        return target;
    }

    public UserFilterRequest toApplication(UserFilterPresentationRequest request) {
        if (request == null) return new UserFilterRequest();

        UserFilterRequest target = new UserFilterRequest();
        target.setUserType(request.getUserType());
        target.setStatus(request.getStatus());
        target.setEmail(request.getEmail());
        target.setPhone(request.getPhone());
        target.setFirstName(request.getFirstName());
        target.setLastName(request.getLastName());
        target.setActive(request.getActive());
        target.setManagerPosition(request.getManagerPosition());
        target.setAdminLevel(request.getAdminLevel());

        return target;
    }

    public ChangePasswordRequest toApplication(UserChangePasswordPresentationRequest request, String userId) {
        if (request == null) return null;

        ChangePasswordRequest target = new ChangePasswordRequest();
        target.setUserId(userId);
        target.setOldPassword(request.getOldPassword());
        target.setNewPassword(request.getNewPassword());

        return target;
    }

    public ChangePasswordRequest toApplicationForOwnProfile(UserChangePasswordPresentationRequest request) {
        if (request == null) return null;

        ChangePasswordRequest target = new ChangePasswordRequest();
        target.setOldPassword(request.getOldPassword());
        target.setNewPassword(request.getNewPassword());

        return target;
    }

    public UserBasePresentationResponse toPresentation(UserBaseResponse source) {
        if (source == null) return null;

        if (source instanceof ClientResponse cr) return toPresentation(cr);
        if (source instanceof ManagerResponse mr) return toPresentation(mr);
        if (source instanceof SystemAdminResponse sar) return toPresentation(sar);
        if (source instanceof WarehouseAdminResponse war) return toPresentation(war);

        return UserBasePresentationResponse.builder()
                .id(source.getId())
                .firstName(source.getFirstName())
                .lastName(source.getLastName())
                .middleName(source.getMiddleName())
                .fullName(source.getFullName())
                .email(source.getEmail())
                .phone(source.getPhone())
                .userType(source.getUserType())
                .status(source.getStatus())
                .statusDisplayName(source.getStatusDisplayName())
                .registeredAt(source.getRegisteredAt())
                .lastActiveAt(source.getLastActiveAt())
                .employeeId(source.getEmployeeId())
                .build();
    }

    public ClientPresentationResponse toPresentation(ClientResponse source) {
        if (source == null) return null;

        UserBasePresentationResponse base = toPresentation((UserBaseResponse) source);

        return ClientPresentationResponse.builder()
                .id(base.getId())
                .firstName(base.getFirstName())
                .lastName(base.getLastName())
                .middleName(base.getMiddleName())
                .fullName(base.getFullName())
                .email(base.getEmail())
                .phone(base.getPhone())
                .userType(base.getUserType())
                .status(base.getStatus())
                .statusDisplayName(base.getStatusDisplayName())
                .registeredAt(base.getRegisteredAt())
                .lastActiveAt(base.getLastActiveAt())
                .employeeId(base.getEmployeeId())
                .preferredContactMethod(source.getPreferredContactMethod())
                .newsletterSubscribed(source.getNewsletterSubscribed())
                .orderCount(source.getOrderCount())
                .testDriveCount(source.getTestDriveCount())
                .build();
    }

    public ManagerPresentationResponse toPresentation(ManagerResponse source) {
        if (source == null) return null;

        UserBasePresentationResponse base = toPresentation((UserBaseResponse) source);

        return ManagerPresentationResponse.builder()
                .id(base.getId())
                .firstName(base.getFirstName())
                .lastName(base.getLastName())
                .middleName(base.getMiddleName())
                .fullName(base.getFullName())
                .email(base.getEmail())
                .phone(base.getPhone())
                .userType(base.getUserType())
                .status(base.getStatus())
                .statusDisplayName(base.getStatusDisplayName())
                .registeredAt(base.getRegisteredAt())
                .lastActiveAt(base.getLastActiveAt())
                .employeeId(base.getEmployeeId())
                .position(source.getPosition())
                .positionDisplayName(source.getPositionDisplayName())
                .assignedOrdersCount(source.getAssignedOrdersCount())
                .managedTestDrivesCount(source.getManagedTestDrivesCount())
                .available(source.getAvailable())
                .build();
    }

    public SystemAdminPresentationResponse toPresentation(SystemAdminResponse source) {
        if (source == null) return null;

        UserBasePresentationResponse base = toPresentation((UserBaseResponse) source);

        return SystemAdminPresentationResponse.builder()
                .id(base.getId())
                .firstName(base.getFirstName())
                .lastName(base.getLastName())
                .middleName(base.getMiddleName())
                .fullName(base.getFullName())
                .email(base.getEmail())
                .phone(base.getPhone())
                .userType(base.getUserType())
                .status(base.getStatus())
                .statusDisplayName(base.getStatusDisplayName())
                .registeredAt(base.getRegisteredAt())
                .lastActiveAt(base.getLastActiveAt())
                .employeeId(base.getEmployeeId())
                .adminLevel(source.getAdminLevel())
                .permissionsCount(source.getPermissionsCount())
                .build();
    }

    public WarehouseAdminPresentationResponse toPresentation(WarehouseAdminResponse source) {
        if (source == null) return null;

        UserBasePresentationResponse base = toPresentation((UserBaseResponse) source);

        return WarehouseAdminPresentationResponse.builder()
                .id(base.getId())
                .firstName(base.getFirstName())
                .lastName(base.getLastName())
                .middleName(base.getMiddleName())
                .fullName(base.getFullName())
                .email(base.getEmail())
                .phone(base.getPhone())
                .userType(base.getUserType())
                .status(base.getStatus())
                .statusDisplayName(base.getStatusDisplayName())
                .registeredAt(base.getRegisteredAt())
                .lastActiveAt(base.getLastActiveAt())
                .employeeId(base.getEmployeeId())
                .warehousePosition(source.getWarehousePosition())
                .managedSectionIds(source.getManagedSectionIds())
                .onDuty(source.getOnDuty())
                .build();
    }

    public UserListPresentationResponse toPresentation(UserListResponse source) {
        if (source == null) return null;

        return UserListPresentationResponse.builder()
                .users(source.getUsers().stream()
                        .map(this::toPresentation)
                        .collect(Collectors.toList()))
                .totalCount(source.getTotalCount())
                .activeCount(source.getActiveCount())
                .inactiveCount(source.getInactiveCount())
                .blockedCount(source.getBlockedCount())
                .build();
    }

    public UserListPresentationResponse toUserListPresentation(List<? extends UserBaseResponse> source) {
        if (source == null || source.isEmpty()) {
            return UserListPresentationResponse.builder()
                    .users(List.of())
                    .totalCount(0)
                    .activeCount(0)
                    .inactiveCount(0)
                    .blockedCount(0)
                    .build();
        }

        long activeCount = source.stream().filter(u -> "ACTIVE".equals(u.getStatus())).count();
        long inactiveCount = source.stream().filter(u -> "INACTIVE".equals(u.getStatus())).count();
        long blockedCount = source.stream().filter(u -> "BLOCKED".equals(u.getStatus())).count();

        return UserListPresentationResponse.builder()
                .users(source.stream()
                        .map(this::toPresentation)
                        .collect(Collectors.toList()))
                .totalCount(source.size())
                .activeCount((int) activeCount)
                .inactiveCount((int) inactiveCount)
                .blockedCount((int) blockedCount)
                .build();
    }

    public OperationHistoryListPresentationResponse toOperationHistoryListPresentation(List<OperationHistoryRequest> source) {
        if (source == null || source.isEmpty()) {
            return OperationHistoryListPresentationResponse.builder()
                    .operations(List.of())
                    .totalCount(0)
                    .build();
        }

        return OperationHistoryListPresentationResponse.builder()
                .operations(source.stream()
                        .map(this::toOperationHistoryPresentation)
                        .collect(Collectors.toList()))
                .totalCount(source.size())
                .build();
    }

    public OperationHistoryPresentationResponse toOperationHistoryPresentation(OperationHistoryRequest source) {
        if (source == null) return null;

        return OperationHistoryPresentationResponse.builder()
                .id(source.getId())
                .operationType(source.getOperationType())
                .operationTypeDisplayName(source.getOperationTypeDisplayName())
                .description(source.getDescription())
                .timestamp(source.getTimestamp())
                .adminId(source.getAdminId())
                .adminName(source.getAdminName())
                .itemId(source.getItemId())
                .itemType(source.getItemType())
                .quantity(source.getQuantity())
                .fromSection(source.getFromSection())
                .toSection(source.getToSection())
                .fromLocation(source.getFromLocation())
                .toLocation(source.getToLocation())
                .documentNumber(source.getDocumentNumber())
                .build();
    }

    public ManagedSectionsPresentationResponse toManagedSectionsPresentation(Set<String> source) {
        if (source == null) {
            return ManagedSectionsPresentationResponse.builder()
                    .sectionIds(Set.of())
                    .count(0)
                    .build();
        }

        return ManagedSectionsPresentationResponse.builder()
                .sectionIds(source)
                .count(source.size())
                .build();
    }

    public OnDutyPresentationResponse toOnDutyPresentation(boolean onDuty) {
        return OnDutyPresentationResponse.builder()
                .onDuty(onDuty)
                .build();
    }

    public ManagerPresentationResponse toManagerPresentation(ManagerResponse source) {
        return toPresentation(source);
    }

    public WarehouseAdminPresentationResponse toWarehouseAdminPresentation(WarehouseAdminResponse source) {
        return toPresentation(source);
    }

    public SystemAdminPresentationResponse toSystemAdminPresentation(SystemAdminResponse source) {
        return toPresentation(source);
    }

    public UserListPresentationResponse toUserListPresentationFromListResponse(UserListResponse source) {
        return toPresentation(source);
    }
}