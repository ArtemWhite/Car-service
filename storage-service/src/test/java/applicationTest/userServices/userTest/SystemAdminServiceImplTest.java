package applicationTest.userServices.userTest;

import application.dtos.request.userRequest.CreateUserRequest;
import application.dtos.request.userRequest.UpdateUserRequest;
import application.dtos.request.userRequest.UserFilterRequest;
import application.dtos.request.userRequest.ChangePasswordRequest;
import application.dtos.request.userRequest.OperationHistoryRequest;
import application.dtos.response.userResponse.UserBaseResponse;
import application.dtos.response.userResponse.UserListResponse;
import application.dtos.response.userResponse.users.SystemAdminResponse;
import application.dtos.response.userResponse.users.ClientResponse;
import application.dtos.response.userResponse.users.ManagerResponse;
import application.mapper.UserMapper;
import application.services.userService.systemAdmin.SystemAdminServiceImpl;
import domain.exception.DomainValidationException;
import domain.exception.EntityNotFoundException;
import domain.models.users.User;
import domain.models.users.UserStatus;
import domain.models.users.client.Client;
import domain.models.users.manager.Manager;
import domain.models.users.manager.Position;
import domain.models.users.systemAdmin.SystemAdmin;
import domain.models.users.systemAdmin.AdminLevel;
import domain.models.users.systemAdmin.SystemPermission;
import domain.models.users.warehouseAdmin.WarehouseAdmin;
import domain.repository.userRepository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SystemAdminService Tests")
class SystemAdminServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private SystemAdminServiceImpl systemAdminService;

    private SystemAdmin admin;
    private Client client;
    private Manager manager;
    private WarehouseAdmin warehouseAdmin;
    private SystemAdminResponse adminResponse;
    private ClientResponse clientResponse;
    private ManagerResponse managerResponse;
    private CreateUserRequest createRequest;
    private UpdateUserRequest updateRequest;
    private UserFilterRequest filterRequest;
    private UserBaseResponse userBaseResponse;

    @BeforeEach
    void setUp() {
        admin = new SystemAdmin("Admin", "User", null, "admin@email.com", "+123", "pass", "admin123", AdminLevel.SUPER_ADMIN);
        client = new Client("client123", "John", "Doe", null, "john@email.com", "+1234567890", "password123");
        manager = new Manager("Jane", "Smith", null, "jane@email.com", "+9876543210", "password456", "manager123");
        warehouseAdmin = new WarehouseAdmin("Bob", "Johnson", null, "bob@email.com", "+111", "pass", "warehouse123");

        adminResponse = new SystemAdminResponse();
        adminResponse.setId("admin123");

        clientResponse = new ClientResponse();
        clientResponse.setId("client123");

        managerResponse = new ManagerResponse();
        managerResponse.setId("manager123");

        userBaseResponse = new UserBaseResponse();
        userBaseResponse.setId("user123");

        createRequest = new CreateUserRequest();
        createRequest.setUserType("CLIENT");
        createRequest.setFirstName("John");
        createRequest.setLastName("Doe");
        createRequest.setEmail("newuser@email.com");
        createRequest.setPhone("+1234567890");
        createRequest.setPassword("password123");

        updateRequest = new UpdateUserRequest();
        updateRequest.setFirstName("Updated");
        updateRequest.setLastName("Name");
        updateRequest.setStatus("ACTIVE");

        filterRequest = new UserFilterRequest();
        filterRequest.setUserType("CLIENT");
        filterRequest.setStatus("ACTIVE");
    }

    @Test
    @DisplayName("Should get system admin by id successfully")
    void shouldGetSystemAdminByIdSuccessfully() {
        when(userRepository.findById("admin123")).thenReturn(Optional.of(admin));
        when(userMapper.toSystemAdminResponse(admin)).thenReturn(adminResponse);

        SystemAdminResponse result = systemAdminService.getUserById("admin123");

        assertNotNull(result);
        verify(userRepository, times(1)).findById("admin123");
    }

    @Test
    @DisplayName("Should throw exception when admin not found")
    void shouldThrowExceptionWhenAdminNotFound() {
        when(userRepository.findById("admin999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            systemAdminService.getUserById("admin999");
        });
    }

    @Test
    @DisplayName("Should update own profile successfully")
    void shouldUpdateOwnProfileSuccessfully() {
        when(userRepository.findById("admin123")).thenReturn(Optional.of(admin));
        doNothing().when(userMapper).updateDomain(admin, updateRequest);
        when(userRepository.save(admin)).thenReturn(admin);
        when(userMapper.toSystemAdminResponse(admin)).thenReturn(adminResponse);

        SystemAdminResponse result = systemAdminService.updateOwnProfile( updateRequest);

        assertNotNull(result);
        verify(userRepository, times(1)).save(admin);
    }

    @Test
    @DisplayName("Should change own password successfully")
    void shouldChangeOwnPasswordSuccessfully() {
        SystemAdmin adminSpy = spy(admin);
        ChangePasswordRequest passwordRequest = new ChangePasswordRequest();
        passwordRequest.setUserId("admin123");
        passwordRequest.setOldPassword("pass");
        passwordRequest.setNewPassword("newPass");

        when(userRepository.findById("admin123")).thenReturn(Optional.of(adminSpy));
        when(adminSpy.authenticate("pass")).thenReturn(true);
        when(userRepository.save(adminSpy)).thenReturn(adminSpy);
        when(userMapper.toSystemAdminResponse(adminSpy)).thenReturn(adminResponse);

        SystemAdminResponse result = systemAdminService.changeOwnPassword( passwordRequest);

        assertNotNull(result);
        verify(adminSpy, times(1)).changePassword("pass", "newPass");
    }

    @Test
    @DisplayName("Should throw exception when user ID mismatch")
    void shouldThrowExceptionWhenUserIdMismatch() {
        ChangePasswordRequest passwordRequest = new ChangePasswordRequest();
        passwordRequest.setUserId("otherAdmin");
        passwordRequest.setOldPassword("pass");
        passwordRequest.setNewPassword("newPass");

        assertThrows(DomainValidationException.class, () -> {
            systemAdminService.changeOwnPassword( passwordRequest);
        });
    }

    @Test
    @DisplayName("Should throw exception when old password is incorrect")
    void shouldThrowExceptionWhenOldPasswordIncorrect() {
        SystemAdmin adminSpy = spy(admin);
        ChangePasswordRequest passwordRequest = new ChangePasswordRequest();
        passwordRequest.setUserId("admin123");
        passwordRequest.setOldPassword("wrongPass");
        passwordRequest.setNewPassword("newPass");

        when(userRepository.findById("admin123")).thenReturn(Optional.of(adminSpy));
        when(adminSpy.authenticate("wrongPass")).thenReturn(false);

        assertThrows(DomainValidationException.class, () -> {
            systemAdminService.changeOwnPassword( passwordRequest);
        });
    }

    @Test
    @DisplayName("Should create client successfully")
    void shouldCreateClientSuccessfully() {
        when(userRepository.findById("admin123")).thenReturn(Optional.of(admin));
        when(userRepository.existsByEmail("newuser@email.com")).thenReturn(false);
        when(userMapper.toDomain(createRequest)).thenReturn(client);
        when(userRepository.save(any(User.class))).thenReturn(client);
        when(userMapper.toClientResponse(client)).thenReturn(clientResponse);
        when(userRepository.save(admin)).thenReturn(admin);

        UserBaseResponse result = systemAdminService.createUser(createRequest);

        assertNotNull(result);
        verify(userRepository, times(1)).save(client);
        verify(userRepository, times(1)).save(admin);
    }

    @Test
    @DisplayName("Should create manager successfully")
    void shouldCreateManagerSuccessfully() {
        createRequest.setUserType("MANAGER");
        createRequest.setEmployeeId("manager123");

        when(userRepository.findById("admin123")).thenReturn(Optional.of(admin));
        when(userRepository.existsByEmail("newuser@email.com")).thenReturn(false);
        when(userMapper.toDomain(createRequest)).thenReturn(manager);
        when(userRepository.save(any(User.class))).thenReturn(manager);
        when(userMapper.toManagerResponse(manager)).thenReturn(managerResponse);
        when(userRepository.save(admin)).thenReturn(admin);

        UserBaseResponse result = systemAdminService.createUser(createRequest);

        assertNotNull(result);
        verify(userRepository, times(1)).save(manager);
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        when(userRepository.findById("admin123")).thenReturn(Optional.of(admin));
        when(userRepository.existsByEmail("newuser@email.com")).thenReturn(true);

        assertThrows(DomainValidationException.class, () -> {
            systemAdminService.createUser(createRequest);
        });
    }

    @Test
    @DisplayName("Should update user successfully")
    void shouldUpdateUserSuccessfully() {
        when(userRepository.findById("admin123")).thenReturn(Optional.of(admin));
        when(userRepository.findById("user123")).thenReturn(Optional.of(client));
        doNothing().when(userMapper).updateDomain(client, updateRequest);
        when(userRepository.save(client)).thenReturn(client);
        when(userMapper.toClientResponse(client)).thenReturn(clientResponse);
        when(userRepository.save(admin)).thenReturn(admin);

        UserBaseResponse result = systemAdminService.updateUser("user123", updateRequest);

        assertNotNull(result);
        verify(userRepository, times(1)).save(client);
    }

    @Test
    @DisplayName("Should delete user successfully")
    void shouldDeleteUserSuccessfully() {
        when(userRepository.findById("admin123")).thenReturn(Optional.of(admin));
        doNothing().when(userRepository).delete("user123");

        System.out.println("=== BEFORE DELETE ===");
        System.out.println("Admin: " + admin.getId());
        System.out.println("Client: " + client.getId());

        systemAdminService.deleteUser("user123", "Cleanup");

        System.out.println("=== AFTER DELETE ===");

        verify(userRepository, times(1)).delete("user123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should block user successfully")
    void shouldBlockUserSuccessfully() {
        when(userRepository.findById("admin123")).thenReturn(Optional.of(admin));
        when(userRepository.findById("user123")).thenReturn(Optional.of(client));
        when(userRepository.save(client)).thenReturn(client);
        when(userMapper.toClientResponse(client)).thenReturn(clientResponse);
        when(userRepository.save(admin)).thenReturn(admin);

        UserBaseResponse result = systemAdminService.blockUser("user123", "Violation");

        assertNotNull(result);
        assertEquals(UserStatus.BLOCKED, client.getStatus());
    }

    @Test
    @DisplayName("Should unblock user successfully")
    void shouldUnblockUserSuccessfully() {
        client.block();

        when(userRepository.findById("admin123")).thenReturn(Optional.of(admin));
        when(userRepository.findById("user123")).thenReturn(Optional.of(client));
        when(userRepository.save(client)).thenReturn(client);
        when(userMapper.toClientResponse(client)).thenReturn(clientResponse);
        when(userRepository.save(admin)).thenReturn(admin);

        UserBaseResponse result = systemAdminService.unblockUser("user123");

        assertNotNull(result);
        assertEquals(UserStatus.ACTIVE, client.getStatus());
    }

    @Test
    @DisplayName("Should get all users")
    void shouldGetAllUsers() {
        when(userRepository.findById("admin123")).thenReturn(Optional.of(admin));
        when(userRepository.findAll()).thenReturn(List.of(client, manager));
        when(userMapper.toBaseResponse(client)).thenReturn(userBaseResponse);
        when(userMapper.toBaseResponse(manager)).thenReturn(userBaseResponse);

        List<UserBaseResponse> result = systemAdminService.getAllUsers();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should get users by type")
    void shouldGetUsersByType() {
        when(userRepository.findById("admin123")).thenReturn(Optional.of(admin));
        when(userRepository.findAllByRole(Client.class)).thenReturn(List.of(client));
        when(userMapper.toBaseResponse(client)).thenReturn(userBaseResponse);

        List<UserBaseResponse> result = systemAdminService.getUsersByType("CLIENT");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should get user details")
    void shouldGetUserDetails() {
        when(userRepository.findById("admin123")).thenReturn(Optional.of(admin));
        when(userRepository.findById("client123")).thenReturn(Optional.of(client));
        when(userMapper.toClientResponse(client)).thenReturn(clientResponse);

        UserBaseResponse result = systemAdminService.getUserDetails("client123");

        assertNotNull(result);
    }

    @Test
    @DisplayName("Should get users with filters")
    void shouldGetUsersWithFilters() {
        when(userRepository.findById("admin123")).thenReturn(Optional.of(admin));
        when(userRepository.findAll()).thenReturn(List.of(client));
        when(userMapper.toBaseResponse(client)).thenReturn(userBaseResponse);

        UserListResponse result = systemAdminService.getUsersWithFilters(filterRequest);

        assertNotNull(result);
        assertEquals(1, result.getTotalCount());
    }

    @Test
    @DisplayName("Should grant permission successfully")
    void shouldGrantPermissionSuccessfully() {
        SystemAdmin targetAdmin = new SystemAdmin("Target", "Admin", null, "target@email.com", "+222", "pass", "target123", AdminLevel.JUNIOR_ADMIN);

        when(userRepository.findById("admin123")).thenReturn(Optional.of(admin));
        when(userRepository.findById("target123")).thenReturn(Optional.of(targetAdmin));
        when(userRepository.save(targetAdmin)).thenReturn(targetAdmin);
        when(userMapper.toSystemAdminResponse(targetAdmin)).thenReturn(adminResponse);
        when(userRepository.save(admin)).thenReturn(admin);

        SystemAdminResponse result = systemAdminService.grantPermission("target123", SystemPermission.CREATE_USER);

        assertNotNull(result);
        verify(userRepository, times(1)).save(targetAdmin);
    }

    @Test
    @DisplayName("Should revoke permission successfully")
    void shouldRevokePermissionSuccessfully() {
        SystemAdmin targetAdmin = new SystemAdmin("Target", "Admin", null, "target@email.com", "+222", "pass", "target123", AdminLevel.JUNIOR_ADMIN);
        targetAdmin.addPermission(SystemPermission.CREATE_USER);

        when(userRepository.findById("admin123")).thenReturn(Optional.of(admin));
        when(userRepository.findById("target123")).thenReturn(Optional.of(targetAdmin));
        when(userRepository.save(targetAdmin)).thenReturn(targetAdmin);
        when(userMapper.toSystemAdminResponse(targetAdmin)).thenReturn(adminResponse);
        when(userRepository.save(admin)).thenReturn(admin);

        SystemAdminResponse result = systemAdminService.revokePermission("target123", SystemPermission.CREATE_USER);

        assertNotNull(result);
        assertFalse(targetAdmin.hasPermission(SystemPermission.CREATE_USER));
    }

    @Test
    @DisplayName("Should promote admin successfully")
    void shouldPromoteAdminSuccessfully() {
        SystemAdmin targetAdmin = new SystemAdmin("Target", "Admin", null, "target@email.com", "+222", "pass", "target123", AdminLevel.JUNIOR_ADMIN);

        when(userRepository.findById("admin123")).thenReturn(Optional.of(admin));
        when(userRepository.findById("target123")).thenReturn(Optional.of(targetAdmin));
        doNothing().when(userRepository).delete("target123");
        when(userRepository.save(any(SystemAdmin.class))).thenReturn(targetAdmin);
        when(userMapper.toSystemAdminResponse(any(SystemAdmin.class))).thenReturn(adminResponse);
        when(userRepository.save(admin)).thenReturn(admin);

        SystemAdminResponse result = systemAdminService.promoteAdmin("target123", "ADMIN");

        assertNotNull(result);
        verify(userRepository, times(1)).delete("target123");
    }

    @Test
    @DisplayName("Should promote manager successfully")
    void shouldPromoteManagerSuccessfully() {
        when(userRepository.findById("admin123")).thenReturn(Optional.of(admin));
        when(userRepository.findById("manager123")).thenReturn(Optional.of(manager));
        when(userRepository.save(manager)).thenReturn(manager);
        when(userRepository.save(admin)).thenReturn(admin);
        when(userMapper.toSystemAdminResponse(admin)).thenReturn(adminResponse);

        SystemAdminResponse result = systemAdminService.promoteManager("manager123", "SENIOR_MANAGER");

        assertNotNull(result);
        assertEquals(Position.SENIOR_MANAGER, manager.getPosition());
        verify(userMapper, times(1)).toSystemAdminResponse(admin);
    }

    @Test
    @DisplayName("Should promote warehouse admin successfully")
    void shouldPromoteWarehouseAdminSuccessfully() {
        when(userRepository.findById("admin123")).thenReturn(Optional.of(admin));
        when(userRepository.findById("warehouse123")).thenReturn(Optional.of(warehouseAdmin));
        when(userRepository.save(warehouseAdmin)).thenReturn(warehouseAdmin);
        when(userRepository.save(admin)).thenReturn(admin);
        when(userMapper.toSystemAdminResponse(admin)).thenReturn(adminResponse);

        SystemAdminResponse result = systemAdminService.promoteWarehouseAdmin("warehouse123", "SENIOR_WAREHOUSE_ADMIN");

        assertNotNull(result);
        verify(userMapper, times(1)).toSystemAdminResponse(admin);
    }

    @Test
    @DisplayName("Should get audit log")
    void shouldGetAuditLog() {
        admin.logAction("TEST", "Test action");

        when(userRepository.findById("admin123")).thenReturn(Optional.of(admin));

        List<OperationHistoryRequest> result = systemAdminService.getAuditLog();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should change system settings")
    void shouldChangeSystemSettings() {
        when(userRepository.findById("admin123")).thenReturn(Optional.of(admin));
        when(userRepository.save(admin)).thenReturn(admin);

        systemAdminService.changeSystemSettings(new Object());

        verify(userRepository, times(1)).save(admin);
    }
}