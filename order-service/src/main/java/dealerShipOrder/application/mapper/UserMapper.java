package dealerShipOrder.application.mapper;

import dealerShipOrder.application.dtos.request.userRequest.CreateUserRequest;
import dealerShipOrder.application.dtos.request.userRequest.UpdateUserRequest;
import dealerShipOrder.application.dtos.response.userResponse.UserBaseResponse;
import dealerShipOrder.application.dtos.response.userResponse.users.ClientResponse;
import dealerShipOrder.application.dtos.response.userResponse.users.ManagerResponse;
import dealerShipOrder.application.dtos.response.userResponse.users.SystemAdminResponse;
import dealerShipOrder.application.dtos.response.userResponse.users.WarehouseAdminResponse;
import dealerShipOrder.domain.models.users.User;
import dealerShipOrder.domain.models.users.client.Client;
import dealerShipOrder.domain.models.users.manager.Manager;
import dealerShipOrder.domain.models.users.manager.Position;
import dealerShipOrder.domain.models.users.systemAdmin.AdminLevel;
import dealerShipOrder.domain.models.users.systemAdmin.SystemAdmin;
import dealerShipOrder.domain.models.users.warehouseAdmin.WarehouseAdmin;
import dealerShipOrder.domain.models.users.warehouseAdmin.WarehousePosition;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper
{
    public User toDomain(CreateUserRequest request) {
        return switch (request.getUserType()) {
            case "CLIENT" -> new Client(
                    request.getEmployeeId(),
                    request.getFirstName(),
                    request.getLastName(),
                    request.getMiddleName(),
                    request.getEmail(),
                    request.getPhone(),
                    request.getPassword()
            );
            case "MANAGER" -> new Manager(
                    request.getFirstName(),
                    request.getLastName(),
                    request.getMiddleName(),
                    request.getEmail(),
                    request.getPhone(),
                    request.getPassword(),
                    request.getEmployeeId()
            );
            case "SYSTEM_ADMIN" -> new SystemAdmin(
                    request.getFirstName(),
                    request.getLastName(),
                    request.getMiddleName(),
                    request.getEmail(),
                    request.getPhone(),
                    request.getPassword(),
                    request.getEmployeeId(),
                    AdminLevel.valueOf(request.getAdminLevel())
            );
            case "WAREHOUSE_ADMIN" -> new WarehouseAdmin(
                    request.getFirstName(),
                    request.getLastName(),
                    request.getMiddleName(),
                    request.getEmail(),
                    request.getPhone(),
                    request.getPassword(),
                    request.getEmployeeId()
            );
            default -> throw new IllegalArgumentException("Unknown user type: " + request.getUserType());
        };
    }

    public ClientResponse toClientResponse(Client client) {
        ClientResponse response = new ClientResponse();
        fillBaseFields(response, client);
        response.setUserType("CLIENT");
        response.setPreferredContactMethod(client.getPreferredContactMethod());
        response.setNewsletterSubscribed(client.isNewsletterSubscribed());
        response.setOrderCount(client.getOrderCount());
        response.setTestDriveCount(client.getTestDriveRequests().size());
        return response;
    }

    public ManagerResponse toManagerResponse(Manager manager) {
        ManagerResponse response = new ManagerResponse();
        fillBaseFields(response, manager);
        response.setUserType("MANAGER");
        response.setEmployeeId(manager.getId());
        response.setPosition(manager.getPosition().name());
        response.setPositionDisplayName(manager.getPosition().getDisplayName());
        response.setAssignedOrdersCount(manager.getAssignedOrdersCount());
        response.setManagedTestDrivesCount(manager.getManagedTestDrives().size());
        response.setAvailable(manager.isAvailable());
        return response;
    }

    public SystemAdminResponse toSystemAdminResponse(SystemAdmin admin) {
        SystemAdminResponse response = new SystemAdminResponse();
        fillBaseFields(response, admin);
        response.setUserType("SYSTEM_ADMIN");
        response.setEmployeeId(admin.getId());
        response.setAdminLevel(admin.getLevel().name());
        response.setPermissionsCount(admin.getPermissions().size());
        return response;
    }

    public WarehouseAdminResponse toWarehouseAdminResponse(WarehouseAdmin admin) {
        WarehouseAdminResponse response = new WarehouseAdminResponse();
        fillBaseFields(response, admin);
        response.setUserType("WAREHOUSE_ADMIN");
        response.setEmployeeId(admin.getId());
        response.setWarehousePosition(admin.getPosition().name());
        response.setManagedSectionIds(admin.getManagedSectionIds());
        response.setOnDuty(admin.isOnDuty());
        return response;
    }

    private void fillBaseFields(UserBaseResponse response, User user) {
        response.setId(user.getId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setMiddleName(user.getMiddleName());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setUserType(user.getUserType() != null ? user.getUserType().name() : null);
        response.setStatus(user.getStatus() != null ? user.getStatus().name() : null);
        response.setStatusDisplayName(user.getStatus() != null ? user.getStatus().getDisplayName() : null);
        response.setRegisteredAt(user.getRegisteredAt());
        response.setLastActiveAt(user.getLastActiveAt());
        response.setEmployeeId(user.getId());
    }

    public List<UserBaseResponse> toBaseResponseList(List<User> users) {
        return users.stream()
                .map(this::toBaseResponse)
                .collect(Collectors.toList());
    }

    public UserBaseResponse toBaseResponse(User user) {
        UserBaseResponse response = new UserBaseResponse();
        fillBaseFields(response, user);
        return response;
    }

    public void updateDomain(User user, UpdateUserRequest request) {
        if (request.getFirstName() != null) {
            user.updatePersonalInfo(
                    request.getFirstName(),
                    request.getLastName() != null ? request.getLastName() : user.getLastName(),
                    request.getMiddleName() != null ? request.getMiddleName() : user.getMiddleName()
            );
        }

        if (request.getEmail() != null || request.getPhone() != null) {
            user.updateContactInfo(
                    request.getEmail() != null ? request.getEmail() : user.getEmail(),
                    request.getPhone() != null ? request.getPhone() : user.getPhone()
            );
        }

        if (request.getStatus() != null) {
            updateUserStatus(user, request.getStatus());
        }

        if (user instanceof Manager && request.getPosition() != null) {
            ((Manager) user).promote(Position.valueOf(request.getPosition()));
        }

        if (user instanceof Manager && request.getAvailable() != null) {
            ((Manager) user).setAvailable(request.getAvailable());
        }

        if (user instanceof Client client) {
            if (request.getPreferredContactMethod() != null) {
                client.setPreferredContactMethod(request.getPreferredContactMethod());
            }
            if (request.getNewsletterSubscribed() != null) {
                if (request.getNewsletterSubscribed()) {
                    client.subscribeToNewsletter();
                } else {
                    client.unsubscribeFromNewsletter();
                }
            }
        }

        if (user instanceof WarehouseAdmin admin) {
            if (request.getWarehousePosition() != null) {
                admin.setPosition(WarehousePosition.valueOf(request.getWarehousePosition()));
            }
        }
    }

    private void updateUserStatus(User user, String status) {
        switch (status) {
            case "ACTIVE":
                user.activate();
                break;
            case "INACTIVE":
                user.deactivate();
                break;
            case "BLOCKED":
                user.block();
                break;
        }
    }
}