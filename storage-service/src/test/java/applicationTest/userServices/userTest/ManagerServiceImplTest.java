package applicationTest.userServices.userTest;

import application.dtos.request.userRequest.UpdateUserRequest;
import application.dtos.request.userRequest.ChangePasswordRequest;
import application.dtos.response.userResponse.users.ManagerResponse;
import application.mapper.UserMapper;
import application.services.userService.manager.ManagerServiceImpl;
import domain.exception.DomainValidationException;
import domain.exception.EntityNotFoundException;
import domain.models.users.User;
import domain.models.users.manager.Manager;
import domain.models.users.manager.Position;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ManagerService Tests")
class ManagerServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private ManagerServiceImpl managerService;

    private Manager manager;
    private ManagerResponse managerResponse;
    private UpdateUserRequest updateRequest;
    private ChangePasswordRequest changePasswordRequest;

    @BeforeEach
    void setUp() {
        manager = new Manager("John", "Doe", null, "john@email.com", "+1234567890", "password123", "manager123");
        managerResponse = new ManagerResponse();
        managerResponse.setId("manager123");
        managerResponse.setFirstName("John");
        managerResponse.setLastName("Doe");

        updateRequest = new UpdateUserRequest();
        updateRequest.setFirstName("Jonathan");
        updateRequest.setLastName("Smith");
        updateRequest.setEmail("newemail@email.com");
        updateRequest.setPhone("+9876543210");
        updateRequest.setStatus("ACTIVE");
        updateRequest.setAvailable(true);

        changePasswordRequest = new ChangePasswordRequest();
        changePasswordRequest.setUserId("manager123");
        changePasswordRequest.setOldPassword("password123");
        changePasswordRequest.setNewPassword("newPassword456");
    }

    @Test
    @DisplayName("Should get manager by id successfully")
    void shouldGetManagerByIdSuccessfully() {
        when(userRepository.findById("manager123")).thenReturn(Optional.of(manager));
        when(userMapper.toManagerResponse(manager)).thenReturn(managerResponse);

        ManagerResponse result = managerService.getUserById("manager123");

        assertNotNull(result);
        verify(userRepository, times(1)).findById("manager123");
        verify(userMapper, times(1)).toManagerResponse(manager);
    }

    @Test
    @DisplayName("Should throw exception when manager not found")
    void shouldThrowExceptionWhenManagerNotFound() {
        when(userRepository.findById("manager999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            managerService.getUserById("manager999");
        });
    }

    @Test
    @DisplayName("Should throw exception when user is not a manager")
    void shouldThrowExceptionWhenUserIsNotManager() {
        User nonManager = mock(User.class);
        when(userRepository.findById("user123")).thenReturn(Optional.of(nonManager));

        assertThrows(DomainValidationException.class, () -> {
            managerService.getUserById("user123");
        });
    }

    @Test
    @DisplayName("Should update manager profile successfully")
    void shouldUpdateManagerProfileSuccessfully() {
        when(userRepository.findById("manager123")).thenReturn(Optional.of(manager));
        doNothing().when(userMapper).updateDomain(manager, updateRequest);
        when(userRepository.save(manager)).thenReturn(manager);
        when(userMapper.toManagerResponse(manager)).thenReturn(managerResponse);

        ManagerResponse result = managerService.updateOwnProfile( updateRequest);

        assertNotNull(result);
        verify(userRepository, times(1)).save(manager);
        verify(userMapper, times(1)).updateDomain(manager, updateRequest);
    }

    @Test
    @DisplayName("Should not update position when updating own profile")
    void shouldNotUpdatePositionWhenUpdatingOwnProfile() {
        updateRequest.setPosition("SENIOR_MANAGER");

        when(userRepository.findById("manager123")).thenReturn(Optional.of(manager));
        doAnswer(invocation -> {
            UpdateUserRequest req = invocation.getArgument(1);
            assertNull(req.getPosition());
            return null;
        }).when(userMapper).updateDomain(manager, updateRequest);
        when(userRepository.save(manager)).thenReturn(manager);
        when(userMapper.toManagerResponse(manager)).thenReturn(managerResponse);

        managerService.updateOwnProfile( updateRequest);

        verify(userMapper, times(1)).updateDomain(manager, updateRequest);
    }

    @Test
    @DisplayName("Should change password successfully")
    void shouldChangePasswordSuccessfully() {
        Manager managerSpy = spy(manager);

        when(userRepository.findById("manager123")).thenReturn(Optional.of(managerSpy));
        when(managerSpy.authenticate("password123")).thenReturn(true);
        when(userRepository.save(managerSpy)).thenReturn(managerSpy);
        when(userMapper.toManagerResponse(managerSpy)).thenReturn(managerResponse);

        ManagerResponse result = managerService.changeOwnPassword( changePasswordRequest);

        assertNotNull(result);
        verify(managerSpy, times(1)).changePassword("password123", "newPassword456");
        verify(userRepository, times(1)).save(managerSpy);
    }

    @Test
    @DisplayName("Should throw exception when user ID mismatch")
    void shouldThrowExceptionWhenUserIdMismatch() {
        changePasswordRequest.setUserId("otherManager");

        assertThrows(DomainValidationException.class, () -> {
            managerService.changeOwnPassword( changePasswordRequest);
        });
    }

    @Test
    @DisplayName("Should throw exception when old password is incorrect")
    void shouldThrowExceptionWhenOldPasswordIncorrect() {
        Manager managerSpy = spy(manager);

        when(userRepository.findById("manager123")).thenReturn(Optional.of(managerSpy));
        when(managerSpy.authenticate("password123")).thenReturn(false);

        assertThrows(DomainValidationException.class, () -> {
            managerService.changeOwnPassword( changePasswordRequest);
        });
    }

    @Test
    @DisplayName("Should set availability to true")
    void shouldSetAvailabilityToTrue() {
        when(userRepository.findById("manager123")).thenReturn(Optional.of(manager));
        when(userRepository.save(manager)).thenReturn(manager);
        when(userMapper.toManagerResponse(manager)).thenReturn(managerResponse);

        ManagerResponse result = managerService.setAvailability(true);

        assertNotNull(result);
        assertTrue(manager.isAvailable());
        verify(userRepository, times(1)).save(manager);
    }

    @Test
    @DisplayName("Should set availability to false")
    void shouldSetAvailabilityToFalse() {
        when(userRepository.findById("manager123")).thenReturn(Optional.of(manager));
        when(userRepository.save(manager)).thenReturn(manager);
        when(userMapper.toManagerResponse(manager)).thenReturn(managerResponse);

        ManagerResponse result = managerService.setAvailability(false);

        assertNotNull(result);
        assertFalse(manager.isAvailable());
        verify(userRepository, times(1)).save(manager);
    }

    @Test
    @DisplayName("Should get all managers")
    void shouldGetAllManagers() {
        when(userRepository.findAllByRole(Manager.class)).thenReturn(List.of(manager));
        when(userMapper.toManagerResponse(manager)).thenReturn(managerResponse);

        List<ManagerResponse> result = managerService.getAllManagers();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRepository, times(1)).findAllByRole(Manager.class);
    }

    @Test
    @DisplayName("Should return empty list when no managers")
    void shouldReturnEmptyListWhenNoManagers() {
        when(userRepository.findAllByRole(Manager.class)).thenReturn(List.of());

        List<ManagerResponse> result = managerService.getAllManagers();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should get available managers")
    void shouldGetAvailableManagers() {
        manager.setAvailable(true);
        when(userRepository.findAvailableManagers()).thenReturn(List.of(manager));
        when(userMapper.toManagerResponse(manager)).thenReturn(managerResponse);

        List<ManagerResponse> result = managerService.getAvailableManagers();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should return empty list when no available managers")
    void shouldReturnEmptyListWhenNoAvailableManagers() {
        manager.setAvailable(false);
        when(userRepository.findAvailableManagers()).thenReturn(List.of());

        List<ManagerResponse> result = managerService.getAvailableManagers();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should promote manager to senior")
    void shouldPromoteManagerToSenior() {
        when(userRepository.findById("manager123")).thenReturn(Optional.of(manager));
        when(userRepository.save(manager)).thenReturn(manager);
        when(userMapper.toManagerResponse(manager)).thenReturn(managerResponse);

        ManagerResponse result = managerService.promote("SENIOR_MANAGER");

        assertNotNull(result);
        assertEquals(Position.SENIOR_MANAGER, manager.getPosition());
        assertEquals(15, manager.getMaxConcurrentOrders());
        verify(userRepository, times(1)).save(manager);
    }

    @Test
    @DisplayName("Should promote manager to lead")
    void shouldPromoteManagerToLead() {
        when(userRepository.findById("manager123")).thenReturn(Optional.of(manager));
        when(userRepository.save(manager)).thenReturn(manager);
        when(userMapper.toManagerResponse(manager)).thenReturn(managerResponse);

        ManagerResponse result = managerService.promote("LEAD_MANAGER");

        assertNotNull(result);
        assertEquals(Position.LEAD_MANAGER, manager.getPosition());
        assertEquals(20, manager.getMaxConcurrentOrders());
        verify(userRepository, times(1)).save(manager);
    }

    @Test
    @DisplayName("Should throw exception when promoting with invalid position")
    void shouldThrowExceptionWhenPromotingWithInvalidPosition() {
        when(userRepository.findById("manager123")).thenReturn(Optional.of(manager));

        assertThrows(IllegalArgumentException.class, () -> {
            managerService.promote( "INVALID_POSITION");
        });
    }

    @Test
    @DisplayName("Should throw exception when manager not found for promotion")
    void shouldThrowExceptionWhenManagerNotFoundForPromotion() {
        when(userRepository.findById("manager999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            managerService.promote( "SENIOR_MANAGER");
        });
    }
}