package applicationTest.testDriveRequestService.testDriveRequestUserTest;

import application.dtos.request.testDriveRequest.UpdateTestDriveRequest;
import application.dtos.response.testDriveResponse.TestDriveResponse;
import application.mapper.TestDriveMapper;
import application.services.testDriveService.systemAdmin.TestDriveSystemAdminServiceImpl;
import domain.exception.DomainValidationException;
import domain.exception.EntityNotFoundException;
import domain.models.car.*;
import domain.models.car.engine.Engine;
import domain.models.car.engine.EngineDisplacement;
import domain.models.car.engine.EngineFuelType;
import domain.models.car.engine.EnginePower;
import domain.models.car.transmission.Transmission;
import domain.models.car.transmission.TransmissionType;
import domain.models.car.types.*;
import domain.models.testDriveRequest.TestDriveRequest;
import domain.models.testDriveRequest.TestDriveStatus;
import domain.models.users.client.Client;
import domain.models.users.manager.Manager;
import domain.models.users.systemAdmin.SystemAdmin;
import domain.models.users.systemAdmin.AdminLevel;
import domain.repository.carRepository.CarRepository;
import domain.repository.testDriveRequestRepository.TestDriveRequestRepository;
import domain.repository.userRepository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TestDriveSystemAdminService Tests")
class TestDriveSystemAdminServiceImplTest {

    @Mock
    private TestDriveRequestRepository testDriveRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CarRepository carRepository;

    @Mock
    private TestDriveMapper testDriveMapper;

    @InjectMocks
    private TestDriveSystemAdminServiceImpl testDriveAdminService;

    private SystemAdmin admin;
    private TestDriveRequest testDriveRequest;
    private UpdateTestDriveRequest updateRequest;
    private TestDriveResponse testDriveResponse;
    private LocalDateTime futureTime;
    private LocalDateTime newTime;
    private Car mockCar;
    private Client mockClient;

    @BeforeEach
    void setUp() {
        admin = new SystemAdmin("Admin", "User", null, "admin@email.com", "+123", "pass", "emp1", AdminLevel.ADMIN);
        futureTime = LocalDateTime.now().plusDays(2);
        newTime = LocalDateTime.now().plusDays(5);
        testDriveRequest = new TestDriveRequest("req123", "client123", "car123", futureTime);
        testDriveResponse = new TestDriveResponse();
        mockCar = createMockCar();
        mockClient = createMockClient();

        updateRequest = new UpdateTestDriveRequest();
        updateRequest.setStartTime(newTime);
        updateRequest.setStatus("CONFIRMED");
        updateRequest.setManagerId("manager123");
        updateRequest.setNotes("Updated notes");
    }

    private Car createMockCar() {
        Engine engine = new Engine("eng1", EngineFuelType.PETROL,
                new EngineDisplacement(2.0), new EnginePower(184));
        Transmission transmission = new Transmission(TransmissionType.AUTOMATIC, 8);
        Car car = new Car("car123", CarBrand.BMW,
                new CarModel("model1", "320i", CarBrand.BMW, "G20"),
                CarBody.SEDAN, CarColor.BLACK, DriveType.REAR, engine, transmission,
                new Price(BigDecimal.valueOf(3500000), Currency.getInstance("RUB"), false));
        car.markAsAvailable();
        car.addToTestDriveFleet();
        return car;
    }

    private Client createMockClient() {
        return new Client("client123", "John", "Doe", null, "john@email.com", "+1234567890", "password123");
    }

    @Test
    @DisplayName("Should log action when updating request")
    void shouldLogActionWhenUpdatingRequest() {
        Manager mockManager = new Manager("Jane", "Smith", null, "jane@email.com", "+9876543210", "password456", "manager123");

        when(userRepository.findById("admin123")).thenReturn(Optional.of(admin));
        when(userRepository.findById("manager123")).thenReturn(Optional.of(mockManager));
        when(testDriveRepository.findById("req123")).thenReturn(Optional.of(testDriveRequest));
        when(testDriveRepository.save(any(TestDriveRequest.class))).thenReturn(testDriveRequest);
        when(carRepository.findById("car123")).thenReturn(Optional.of(mockCar));
        when(userRepository.findById("client123")).thenReturn(Optional.of(mockClient));
        when(testDriveMapper.toResponse(any(), any(), any(), any())).thenReturn(testDriveResponse);

        testDriveAdminService.updateRequest("req123", updateRequest);

        assertFalse(admin.getAuditLog().isEmpty());
        assertEquals("UPDATE_TEST_DRIVE", admin.getAuditLog().get(0).getAction());
    }

    @Test
    @DisplayName("Should assign manager when updating")
    void shouldAssignManagerWhenUpdating() {
        UpdateTestDriveRequest assignRequest = new UpdateTestDriveRequest();
        assignRequest.setManagerId("newManager");

        Manager newManager = new Manager("New", "Manager", null, "new@email.com", "+123", "pass", "newManager");

        when(userRepository.findById("admin123")).thenReturn(Optional.of(admin));
        when(userRepository.findById("newManager")).thenReturn(Optional.of(newManager));
        when(testDriveRepository.findById("req123")).thenReturn(Optional.of(testDriveRequest));
        when(testDriveRepository.save(any(TestDriveRequest.class))).thenReturn(testDriveRequest);
        when(carRepository.findById("car123")).thenReturn(Optional.of(mockCar));
        when(userRepository.findById("client123")).thenReturn(Optional.of(mockClient));
        when(testDriveMapper.toResponse(any(), any(), any(), any())).thenReturn(testDriveResponse);

        testDriveAdminService.updateRequest("req123", assignRequest);

        assertEquals("newManager", testDriveRequest.getManagerId());
        verify(testDriveRepository, times(1)).save(testDriveRequest);
    }

    @Test
    @DisplayName("Should update status when updating")
    void shouldUpdateStatusWhenUpdating() {
        UpdateTestDriveRequest statusRequest = new UpdateTestDriveRequest();
        statusRequest.setStatus("CANCELLED");

        doAnswer(invocation -> {
            TestDriveRequest request = invocation.getArgument(0);
            request.cancel();
            return null;
        }).when(testDriveMapper).updateDomain(any(TestDriveRequest.class), any(UpdateTestDriveRequest.class));

        when(userRepository.findById("admin123")).thenReturn(Optional.of(admin));
        when(testDriveRepository.findById("req123")).thenReturn(Optional.of(testDriveRequest));
        when(testDriveRepository.save(any(TestDriveRequest.class))).thenReturn(testDriveRequest);
        when(carRepository.findById("car123")).thenReturn(Optional.of(mockCar));
        when(userRepository.findById("client123")).thenReturn(Optional.of(mockClient));
        when(testDriveMapper.toResponse(any(), any(), any(), any())).thenReturn(testDriveResponse);

        testDriveAdminService.updateRequest("req123", statusRequest);

        assertEquals(TestDriveStatus.CANCELLED, testDriveRequest.getStatus());
    }

    @Test
    @DisplayName("Should throw exception when request not found")
    void shouldThrowExceptionWhenRequestNotFound() {
        when(userRepository.findById("admin123")).thenReturn(Optional.of(admin));
        when(testDriveRepository.findById("req999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            testDriveAdminService.updateRequest("req999", updateRequest);
        });
    }

    @Test
    @DisplayName("Should throw exception when admin not found")
    void shouldThrowExceptionWhenAdminNotFound() {
        when(userRepository.findById("admin999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            testDriveAdminService.updateRequest("req123", updateRequest);
        });
    }

    @Test
    @DisplayName("Should delete request successfully")
    void shouldDeleteRequestSuccessfully() {
        when(userRepository.findById("admin123")).thenReturn(Optional.of(admin));
        when(testDriveRepository.findById("req123")).thenReturn(Optional.of(testDriveRequest));
        doNothing().when(testDriveRepository).delete("req123");

        testDriveAdminService.deleteRequest("req123", "Cleanup");

        verify(testDriveRepository, times(1)).delete("req123");
        verify(userRepository, times(1)).save(admin);
    }

    @Test
    @DisplayName("Should log action when deleting request")
    void shouldLogActionWhenDeletingRequest() {
        when(userRepository.findById("admin123")).thenReturn(Optional.of(admin));
        when(testDriveRepository.findById("req123")).thenReturn(Optional.of(testDriveRequest));
        doNothing().when(testDriveRepository).delete("req123");

        testDriveAdminService.deleteRequest("req123", "Cleanup");

        assertFalse(admin.getAuditLog().isEmpty());
        assertEquals("DELETE_TEST_DRIVE", admin.getAuditLog().get(0).getAction());
        assertTrue(admin.getAuditLog().get(0).getDetails().contains("Cleanup"));
    }

    @Test
    @DisplayName("Should throw exception when request not found for delete")
    void shouldThrowExceptionWhenRequestNotFoundForDelete() {
        when(userRepository.findById("admin123")).thenReturn(Optional.of(admin));
        when(testDriveRepository.findById("req999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            testDriveAdminService.deleteRequest("req999", "Cleanup");
        });
    }

    @Test
    @DisplayName("Should get requests by status")
    void shouldGetRequestsByStatus() {
        when(testDriveRepository.findByStatus(TestDriveStatus.PENDING)).thenReturn(List.of(testDriveRequest));
        when(carRepository.findById("car123")).thenReturn(Optional.of(mockCar));
        when(userRepository.findById("client123")).thenReturn(Optional.of(mockClient));
        when(testDriveMapper.toResponse(any(), any(), any(), any())).thenReturn(testDriveResponse);

        List<TestDriveResponse> result = testDriveAdminService.getRequestsByStatus("PENDING");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(testDriveRepository, times(1)).findByStatus(TestDriveStatus.PENDING);
    }

    @Test
    @DisplayName("Should return empty list when no requests with status")
    void shouldReturnEmptyListWhenNoRequestsWithStatus() {
        when(testDriveRepository.findByStatus(TestDriveStatus.PENDING)).thenReturn(List.of());

        List<TestDriveResponse> result = testDriveAdminService.getRequestsByStatus("PENDING");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should handle all status types")
    void shouldHandleAllStatusTypes() {
        when(testDriveRepository.findByStatus(TestDriveStatus.PENDING)).thenReturn(List.of(testDriveRequest));
        when(testDriveRepository.findByStatus(TestDriveStatus.CONFIRMED)).thenReturn(List.of());
        when(testDriveRepository.findByStatus(TestDriveStatus.COMPLETED)).thenReturn(List.of());
        when(testDriveRepository.findByStatus(TestDriveStatus.CANCELLED)).thenReturn(List.of());
        when(testDriveRepository.findByStatus(TestDriveStatus.NO_SHOW)).thenReturn(List.of());

        when(carRepository.findById(any())).thenReturn(Optional.of(mockCar));
        when(userRepository.findById(any())).thenReturn(Optional.of(mockClient));
        when(testDriveMapper.toResponse(any(), any(), any(), any())).thenReturn(testDriveResponse);

        assertNotNull(testDriveAdminService.getRequestsByStatus("PENDING"));
        assertNotNull(testDriveAdminService.getRequestsByStatus("CONFIRMED"));
        assertNotNull(testDriveAdminService.getRequestsByStatus("COMPLETED"));
        assertNotNull(testDriveAdminService.getRequestsByStatus("CANCELLED"));
        assertNotNull(testDriveAdminService.getRequestsByStatus("NO_SHOW"));
    }

    @Test
    @DisplayName("Should throw exception on invalid status")
    void shouldThrowExceptionOnInvalidStatus() {
        assertThrows(DomainValidationException.class, () -> {
            testDriveAdminService.getRequestsByStatus("INVALID_STATUS");
        });
    }
}