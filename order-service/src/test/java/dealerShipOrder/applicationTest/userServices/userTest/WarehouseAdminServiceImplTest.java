package dealerShipOrder.applicationTest.userServices.userTest;

import dealerShipOrder.application.dtos.request.userRequest.UpdateUserRequest;
import dealerShipOrder.application.dtos.request.userRequest.ChangePasswordRequest;
import dealerShipOrder.application.dtos.request.userRequest.OperationHistoryRequest;
import dealerShipOrder.application.dtos.response.userResponse.users.WarehouseAdminResponse;
import dealerShipOrder.application.mapper.UserMapper;
import dealerShipOrder.application.services.userService.warehouseAdmin.WarehouseAdminServiceImpl;
import dealerShipOrder.domain.models.expection.DomainValidationException;
import dealerShipOrder.domain.models.expection.EntityNotFoundException;
import dealerShipOrder.domain.models.users.User;
import dealerShipOrder.domain.models.users.warehouseAdmin.WarehouseAdmin;
import dealerShipOrder.domain.models.users.warehouseAdmin.StockOperation;
import dealerShipOrder.domain.models.users.warehouseAdmin.ItemType;
import dealerShipOrder.domain.repository.usersRepository.userRepository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import dealerShipOrder.applicationTest.WithMockSecurityExtension;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith({MockitoExtension.class, WithMockSecurityExtension.class})
@DisplayName("WarehouseAdminService Tests")
class WarehouseAdminServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private WarehouseAdminServiceImpl warehouseAdminService;

    private WarehouseAdmin warehouseAdmin;
    private WarehouseAdminResponse adminResponse;
    private UpdateUserRequest updateRequest;
    private ChangePasswordRequest changePasswordRequest;

    @BeforeEach
    void setUp() {
        warehouseAdmin = new WarehouseAdmin("Bob", "Johnson", null, "bob@email.com", "+1111111111",  "test-user-id", "password123");
        adminResponse = new WarehouseAdminResponse();
        adminResponse.setId("test-user-id");

        updateRequest = new UpdateUserRequest();
        updateRequest.setFirstName("Robert");
        updateRequest.setLastName("Johnson");
        updateRequest.setEmail("newemail@email.com");
        updateRequest.setPhone("+9999999999");
        updateRequest.setStatus("ACTIVE");

        changePasswordRequest = new ChangePasswordRequest();
        changePasswordRequest.setUserId("test-user-id");
        changePasswordRequest.setOldPassword("password123");
        changePasswordRequest.setNewPassword("newPassword456");
    }

    @Test
    @DisplayName("Should get warehouse admin by id successfully")
    void shouldGetWarehouseAdminByIdSuccessfully() {
        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(warehouseAdmin));
        when(userMapper.toWarehouseAdminResponse(warehouseAdmin)).thenReturn(adminResponse);

        WarehouseAdminResponse result = warehouseAdminService.getUserById("test-user-id");

        assertNotNull(result);
        verify(userRepository, times(1)).findById("test-user-id");
    }

    @Test
    @DisplayName("Should throw exception when warehouse admin not found")
    void shouldThrowExceptionWhenWarehouseAdminNotFound() {
        when(userRepository.findById("admin999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            warehouseAdminService.getUserById("admin999");
        });
    }

    @Test
    @DisplayName("Should throw exception when user is not warehouse admin")
    void shouldThrowExceptionWhenUserIsNotWarehouseAdmin() {
        User nonAdmin = mock(User.class);
        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(nonAdmin));

        assertThrows(DomainValidationException.class, () -> {
            warehouseAdminService.getUserById("test-user-id");
        });
    }

    @Test
    @DisplayName("Should update own profile successfully")
    void shouldUpdateOwnProfileSuccessfully() {
        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(warehouseAdmin));
        doNothing().when(userMapper).updateDomain(warehouseAdmin, updateRequest);
        when(userRepository.save(warehouseAdmin)).thenReturn(warehouseAdmin);
        when(userMapper.toWarehouseAdminResponse(warehouseAdmin)).thenReturn(adminResponse);

        WarehouseAdminResponse result = warehouseAdminService.updateOwnProfile( updateRequest);

        assertNotNull(result);
        verify(userRepository, times(1)).save(warehouseAdmin);
    }

    @Test
    @DisplayName("Should not update warehouse position when updating own profile")
    void shouldNotUpdateWarehousePositionWhenUpdatingOwnProfile() {
        updateRequest.setWarehousePosition("SENIOR_WAREHOUSE_ADMIN");
        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(warehouseAdmin));
        doAnswer(invocation -> {
            UpdateUserRequest req = invocation.getArgument(1);
            assertNull(req.getWarehousePosition());
            return null;
        }).when(userMapper).updateDomain(warehouseAdmin, updateRequest);
        when(userRepository.save(warehouseAdmin)).thenReturn(warehouseAdmin);
        when(userMapper.toWarehouseAdminResponse(warehouseAdmin)).thenReturn(adminResponse);

        warehouseAdminService.updateOwnProfile( updateRequest);

        verify(userMapper, times(1)).updateDomain(warehouseAdmin, updateRequest);
    }

    @Test
    @DisplayName("Should change own password successfully")
    void shouldChangeOwnPasswordSuccessfully() {
        WarehouseAdmin adminSpy = spy(warehouseAdmin);

        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(adminSpy));
        when(adminSpy.authenticate("password123")).thenReturn(true);
        when(userRepository.save(adminSpy)).thenReturn(adminSpy);
        when(userMapper.toWarehouseAdminResponse(adminSpy)).thenReturn(adminResponse);

        WarehouseAdminResponse result = warehouseAdminService.changeOwnPassword(changePasswordRequest);

        assertNotNull(result);
        verify(adminSpy, times(1)).changePassword("password123", "newPassword456");
        verify(userRepository, times(1)).save(adminSpy);
    }

    @Test
    @DisplayName("Should throw exception when old password is incorrect")
    void shouldThrowExceptionWhenOldPasswordIncorrect() {
        WarehouseAdmin adminSpy = spy(warehouseAdmin);

        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(adminSpy));
        when(adminSpy.authenticate("password123")).thenReturn(false);

        assertThrows(DomainValidationException.class, () -> {
            warehouseAdminService.changeOwnPassword( changePasswordRequest);
        });
    }

    @Test
    @DisplayName("Should assign to section successfully")
    void shouldAssignToSectionSuccessfully() {
        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(warehouseAdmin));
        when(userRepository.save(warehouseAdmin)).thenReturn(warehouseAdmin);
        when(userMapper.toWarehouseAdminResponse(warehouseAdmin)).thenReturn(adminResponse);

        WarehouseAdminResponse result = warehouseAdminService.assignToSection("sectionA");

        assertNotNull(result);
        assertTrue(warehouseAdmin.getManagedSectionIds().contains("sectionA"));
        verify(userRepository, times(1)).save(warehouseAdmin);
    }

    @Test
    @DisplayName("Should remove from section successfully")
    void shouldRemoveFromSectionSuccessfully() {
        warehouseAdmin.assignToSection("sectionA");
        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(warehouseAdmin));
        when(userRepository.save(warehouseAdmin)).thenReturn(warehouseAdmin);
        when(userMapper.toWarehouseAdminResponse(warehouseAdmin)).thenReturn(adminResponse);

        WarehouseAdminResponse result = warehouseAdminService.removeFromSection("sectionA");

        assertNotNull(result);
        assertFalse(warehouseAdmin.getManagedSectionIds().contains("sectionA"));
        verify(userRepository, times(1)).save(warehouseAdmin);
    }

    @Test
    @DisplayName("Should throw exception when assigning with null section ID")
    void shouldThrowExceptionWhenAssigningWithNullSectionId() {
        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(warehouseAdmin));

        assertThrows(DomainValidationException.class, () -> {
            warehouseAdminService.assignToSection(null);
        });
    }

    @Test
    @DisplayName("Should get managed sections")
    void shouldGetManagedSections() {
        warehouseAdmin.assignToSection("sectionA");
        warehouseAdmin.assignToSection("sectionB");
        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(warehouseAdmin));

        Set<String> result = warehouseAdminService.getManagedSections();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("sectionA"));
        assertTrue(result.contains("sectionB"));
    }

    @Test
    @DisplayName("Should start shift successfully")
    void shouldStartShiftSuccessfully() {
        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(warehouseAdmin));
        when(userRepository.save(warehouseAdmin)).thenReturn(warehouseAdmin);
        when(userMapper.toWarehouseAdminResponse(warehouseAdmin)).thenReturn(adminResponse);

        WarehouseAdminResponse result = warehouseAdminService.startShift();

        assertNotNull(result);
        assertTrue(warehouseAdmin.isOnDuty());
        verify(userRepository, times(1)).save(warehouseAdmin);
    }

    @Test
    @DisplayName("Should end shift successfully")
    void shouldEndShiftSuccessfully() {
        warehouseAdmin.startShift();
        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(warehouseAdmin));
        when(userRepository.save(warehouseAdmin)).thenReturn(warehouseAdmin);
        when(userMapper.toWarehouseAdminResponse(warehouseAdmin)).thenReturn(adminResponse);

        WarehouseAdminResponse result = warehouseAdminService.endShift();

        assertNotNull(result);
        assertFalse(warehouseAdmin.isOnDuty());
        verify(userRepository, times(1)).save(warehouseAdmin);
    }

    @Test
    @DisplayName("Should check isOnDuty")
    void shouldCheckIsOnDuty() {
        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(warehouseAdmin));

        boolean isOnDuty = warehouseAdminService.isOnDuty();
        assertFalse(isOnDuty);

        warehouseAdmin.startShift();
        isOnDuty = warehouseAdminService.isOnDuty();
        assertTrue(isOnDuty);
    }

    @Test
    @DisplayName("Should get operation history")
    void shouldGetOperationHistory() {
        StockOperation operation = StockOperation.createUpdate(
                warehouseAdmin.getId(), "item1", ItemType.SPARE_PART, "sectionA", "loc1", "Test"
        );
        warehouseAdmin.addOperation(operation);
        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(warehouseAdmin));

        List<OperationHistoryRequest> result = warehouseAdminService.getOperationHistory();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should return empty list when no operations")
    void shouldReturnEmptyListWhenNoOperations() {
        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(warehouseAdmin));

        List<OperationHistoryRequest> result = warehouseAdminService.getOperationHistory();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should filter operations by date")
    void shouldFilterOperationsByDate() {
        StockOperation op1 = StockOperation.createUpdate(warehouseAdmin.getId(), "item1", ItemType.SPARE_PART, "secA", "loc1", "test1");
        StockOperation op2 = StockOperation.createUpdate(warehouseAdmin.getId(), "item2", ItemType.SPARE_PART, "secB", "loc2", "test2");
        warehouseAdmin.addOperation(op1);
        warehouseAdmin.addOperation(op2);

        String from = LocalDateTime.now().minusSeconds(10).toString();
        String to = LocalDateTime.now().plusSeconds(10).toString();

        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(warehouseAdmin));

        List<OperationHistoryRequest> result = warehouseAdminService.getOperationsByDate(from, to);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should filter operations by type")
    void shouldFilterOperationsByType() {
        StockOperation updateOp = StockOperation.createUpdate(warehouseAdmin.getId(), "item1", ItemType.SPARE_PART, "secA", "loc1", "update");
        StockOperation arrivalOp = StockOperation.createArrival(warehouseAdmin.getId(), "item2", ItemType.SPARE_PART, "secB", "loc2", 5);
        warehouseAdmin.addOperation(updateOp);
        warehouseAdmin.addOperation(arrivalOp);

        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(warehouseAdmin));

        List<OperationHistoryRequest> updates = warehouseAdminService.getOperationsByType("UPDATE");
        List<OperationHistoryRequest> arrivals = warehouseAdminService.getOperationsByType("ARRIVAL");

        assertEquals(1, updates.size());
        assertEquals(1, arrivals.size());
    }

    @Test
    @DisplayName("Should throw exception when invalid operation type")
    void shouldThrowExceptionWhenInvalidOperationType() {
        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(warehouseAdmin));

        assertThrows(IllegalArgumentException.class, () -> {
            warehouseAdminService.getOperationsByType("INVALID_TYPE");
        });
    }
}