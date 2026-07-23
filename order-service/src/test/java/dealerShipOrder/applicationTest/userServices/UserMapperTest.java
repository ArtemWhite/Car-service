package dealerShipOrder.applicationTest.userServices;

import dealerShipOrder.application.dtos.request.userRequest.CreateUserRequest;
import dealerShipOrder.application.dtos.request.userRequest.UpdateUserRequest;
import dealerShipOrder.application.dtos.response.userResponse.UserBaseResponse;
import dealerShipOrder.application.dtos.response.userResponse.users.ClientResponse;
import dealerShipOrder.application.dtos.response.userResponse.users.ManagerResponse;
import dealerShipOrder.application.dtos.response.userResponse.users.SystemAdminResponse;
import dealerShipOrder.application.dtos.response.userResponse.users.WarehouseAdminResponse;
import dealerShipOrder.application.mapper.UserMapper;
import dealerShipOrder.domain.models.users.User;
import dealerShipOrder.domain.models.users.UserStatus;
import dealerShipOrder.domain.models.users.client.Client;
import dealerShipOrder.domain.models.users.manager.Manager;
import dealerShipOrder.domain.models.users.manager.Position;
import dealerShipOrder.domain.models.users.systemAdmin.SystemAdmin;
import dealerShipOrder.domain.models.users.systemAdmin.AdminLevel;
import dealerShipOrder.domain.models.users.systemAdmin.SystemPermission;
import dealerShipOrder.domain.models.users.warehouseAdmin.WarehouseAdmin;
import dealerShipOrder.domain.models.users.warehouseAdmin.WarehousePosition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserMapper Tests")
class UserMapperTest {

    private UserMapper userMapper;
    private Client client;
    private Manager manager;
    private SystemAdmin systemAdmin;
    private WarehouseAdmin warehouseAdmin;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();

        client = new Client("emp123", "John", "Doe", "Michael", "john@email.com", "+1234567890", "password123");
        client.subscribeToNewsletter();
        client.setPreferredContactMethod("phone");
        client.addOrder("order1");
        client.addTestDriveRequest("td1");

        manager = new Manager("Jane", "Smith", "Ann", "jane@email.com", "+9876543210", "password456", "emp456");
        manager.promote(Position.SENIOR_MANAGER);
        manager.setAvailable(true);
        manager.assignOrder("order1");
        manager.assignToTestDrive("td1");

        systemAdmin = new SystemAdmin("Admin", "User", null, "admin@email.com", "+111", "pass", "emp1", AdminLevel.ADMIN);
        systemAdmin.addPermission(SystemPermission.CREATE_USER);

        warehouseAdmin = new WarehouseAdmin("Bob", "Johnson", null, "bob@email.com", "+222", "pass", "emp2");
        warehouseAdmin.assignToSection("sectionA");
        warehouseAdmin.startShift();
    }

    @Test
    @DisplayName("Should convert CLIENT request to Client")
    void shouldConvertClientRequestToClient() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUserType("CLIENT");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setMiddleName("Michael");
        request.setEmail("john@email.com");
        request.setPhone("+1234567890");
        request.setPassword("password123");
        request.setEmployeeId("emp123");

        User result = userMapper.toDomain(request);

        assertNotNull(result);
        assertTrue(result instanceof Client);
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("Michael", result.getMiddleName());
        assertEquals("john@email.com", result.getEmail());
        assertEquals("+1234567890", result.getPhone());
    }

    @Test
    @DisplayName("Should convert MANAGER request to Manager")
    void shouldConvertManagerRequestToManager() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUserType("MANAGER");
        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setEmail("jane@email.com");
        request.setPhone("+9876543210");
        request.setPassword("password456");
        request.setEmployeeId("emp456");

        User result = userMapper.toDomain(request);

        assertNotNull(result);
        assertTrue(result instanceof Manager);
    }

    @Test
    @DisplayName("Should convert SYSTEM_ADMIN request to SystemAdmin")
    void shouldConvertSystemAdminRequestToSystemAdmin() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUserType("SYSTEM_ADMIN");
        request.setFirstName("Admin");
        request.setLastName("User");
        request.setEmail("admin@email.com");
        request.setPhone("+111");
        request.setPassword("pass");
        request.setEmployeeId("emp1");
        request.setAdminLevel("ADMIN");

        User result = userMapper.toDomain(request);

        assertNotNull(result);
        assertTrue(result instanceof SystemAdmin);
        assertEquals(AdminLevel.ADMIN, ((SystemAdmin) result).getLevel());
    }

    @Test
    @DisplayName("Should convert WAREHOUSE_ADMIN request to WarehouseAdmin")
    void shouldConvertWarehouseAdminRequestToWarehouseAdmin() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUserType("WAREHOUSE_ADMIN");
        request.setFirstName("Bob");
        request.setLastName("Johnson");
        request.setEmail("bob@email.com");
        request.setPhone("+222");
        request.setPassword("pass");
        request.setEmployeeId("emp2");

        User result = userMapper.toDomain(request);

        assertNotNull(result);
        assertTrue(result instanceof WarehouseAdmin);
    }

    @Test
    @DisplayName("Should throw exception for unknown user type")
    void shouldThrowExceptionForUnknownUserType() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUserType("UNKNOWN");

        assertThrows(IllegalArgumentException.class, () -> {
            userMapper.toDomain(request);
        });
    }

    @Test
    @DisplayName("Should convert Client to ClientResponse")
    void shouldConvertClientToClientResponse() {
        ClientResponse response = userMapper.toClientResponse(client);

        assertNotNull(response);
        assertEquals(client.getId(), response.getId());
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals("Michael", response.getMiddleName());
        assertEquals("John Doe Michael", response.getFullName());
        assertEquals("john@email.com", response.getEmail());
        assertEquals("+1234567890", response.getPhone());
        assertEquals("CLIENT", response.getUserType());
        assertEquals("phone", response.getPreferredContactMethod());
        assertTrue(response.getNewsletterSubscribed());
        assertEquals(1, response.getOrderCount());
        assertEquals(1, response.getTestDriveCount());
    }

    @Test
    @DisplayName("Should convert Manager to ManagerResponse")
    void shouldConvertManagerToManagerResponse() {
        ManagerResponse response = userMapper.toManagerResponse(manager);

        assertNotNull(response);
        assertEquals(manager.getId(), response.getId());
        assertEquals("Jane", response.getFirstName());
        assertEquals("Smith", response.getLastName());
        assertEquals("Ann", response.getMiddleName());
        assertEquals("SENIOR_MANAGER", response.getPosition());
        assertEquals("Старший менеджер", response.getPositionDisplayName());
        assertEquals(1, response.getAssignedOrdersCount());
        assertEquals(1, response.getManagedTestDrivesCount());
        assertTrue(response.getAvailable());
        assertEquals("MANAGER", response.getUserType());
    }

    @Test
    @DisplayName("Should convert SystemAdmin to SystemAdminResponse")
    void shouldConvertSystemAdminToSystemAdminResponse() {
        SystemAdminResponse response = userMapper.toSystemAdminResponse(systemAdmin);

        assertNotNull(response);
        assertEquals(systemAdmin.getId(), response.getId());
        assertEquals("Admin", response.getFirstName());
        assertEquals("User", response.getLastName());
        assertEquals("ADMIN", response.getAdminLevel());

        assertEquals(15, response.getPermissionsCount());
        assertEquals("SYSTEM_ADMIN", response.getUserType());
    }

    @Test
    @DisplayName("Should convert WarehouseAdmin to WarehouseAdminResponse")
    void shouldConvertWarehouseAdminToWarehouseAdminResponse() {
        WarehouseAdminResponse response = userMapper.toWarehouseAdminResponse(warehouseAdmin);

        assertNotNull(response);
        assertEquals(warehouseAdmin.getId(), response.getId());
        assertEquals("Bob", response.getFirstName());
        assertEquals("Johnson", response.getLastName());
        assertEquals("WAREHOUSE_WORKER", response.getWarehousePosition());
        assertEquals(1, response.getManagedSectionIds().size());
        assertTrue(response.getManagedSectionIds().contains("sectionA"));
        assertTrue(response.getOnDuty());
        assertEquals("WAREHOUSE_ADMIN", response.getUserType());
    }

    @Test
    @DisplayName("Should convert any User to UserBaseResponse")
    void shouldConvertAnyUserToUserBaseResponse() {
        UserBaseResponse clientResponse = userMapper.toBaseResponse(client);
        UserBaseResponse managerResponse = userMapper.toBaseResponse(manager);

        assertNotNull(clientResponse);
        assertNotNull(managerResponse);
        assertEquals(client.getId(), clientResponse.getId());
        assertEquals(manager.getId(), managerResponse.getId());
    }

    @Test
    @DisplayName("Should convert list of Users to list of UserBaseResponses")
    void shouldConvertListOfUsersToListOfResponses() {
        List<User> users = List.of(client, manager, systemAdmin, warehouseAdmin);

        List<UserBaseResponse> responses = userMapper.toBaseResponseList(users);

        assertNotNull(responses);
        assertEquals(4, responses.size());
    }

    @Test
    @DisplayName("Should return empty list for empty input")
    void shouldReturnEmptyListForEmptyInput() {
        List<User> users = List.of();

        List<UserBaseResponse> responses = userMapper.toBaseResponseList(users);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    @DisplayName("Should update user personal info")
    void shouldUpdateUserPersonalInfo() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setFirstName("Jonathan");
        request.setLastName("Smith");
        request.setMiddleName("David");

        userMapper.updateDomain(client, request);

        assertEquals("Jonathan", client.getFirstName());
        assertEquals("Smith", client.getLastName());
        assertEquals("David", client.getMiddleName());
    }

    @Test
    @DisplayName("Should update user contact info")
    void shouldUpdateUserContactInfo() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("newemail@email.com");
        request.setPhone("+9999999999");

        userMapper.updateDomain(client, request);

        assertEquals("newemail@email.com", client.getEmail());
        assertEquals("+9999999999", client.getPhone());
    }

    @Test
    @DisplayName("Should update user status")
    void shouldUpdateUserStatus() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setStatus("BLOCKED");

        userMapper.updateDomain(client, request);

        assertEquals(UserStatus.BLOCKED, client.getStatus());
    }

    @Test
    @DisplayName("Should update manager position")
    void shouldUpdateManagerPosition() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setPosition("LEAD_MANAGER");

        userMapper.updateDomain(manager, request);

        assertEquals(Position.LEAD_MANAGER, manager.getPosition());
        assertEquals(20, manager.getMaxConcurrentOrders());
    }

    @Test
    @DisplayName("Should update manager availability")
    void shouldUpdateManagerAvailability() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setAvailable(false);

        userMapper.updateDomain(manager, request);

        assertFalse(manager.isAvailable());
    }

    @Test
    @DisplayName("Should update client preferences")
    void shouldUpdateClientPreferences() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setPreferredContactMethod("email");
        request.setNewsletterSubscribed(false);

        userMapper.updateDomain(client, request);

        assertEquals("email", client.getPreferredContactMethod());
        assertFalse(client.isNewsletterSubscribed());
    }

    @Test
    @DisplayName("Should update warehouse admin position")
    void shouldUpdateWarehouseAdminPosition() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setWarehousePosition("WAREHOUSE_MANAGER");

        userMapper.updateDomain(warehouseAdmin, request);

        assertEquals(WarehousePosition.WAREHOUSE_MANAGER, warehouseAdmin.getPosition());
    }

    @Test
    @DisplayName("Should ignore null fields in update")
    void shouldIgnoreNullFieldsInUpdate() {
        String originalFirstName = client.getFirstName();
        UpdateUserRequest request = new UpdateUserRequest();

        userMapper.updateDomain(client, request);

        assertEquals(originalFirstName, client.getFirstName());
    }
}