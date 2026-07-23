package dealerShipOrder.applicationTest.testDriveRequestService.testDriveRequestUserTest;

import dealerShipOrder.application.dtos.response.testDriveResponse.TestDriveResponse;
import dealerShipOrder.application.mapper.TestDriveMapper;
import dealerShipOrder.application.services.testDriveService.manager.TestDriveManagerServiceImpl;
import dealerShipOrder.domain.models.expection.DomainValidationException;
import dealerShipOrder.domain.models.expection.EntityNotFoundException;
import domain.models.car.*;
import domain.models.car.engine.Engine;
import domain.models.car.engine.EngineDisplacement;
import domain.models.car.engine.EngineFuelType;
import domain.models.car.engine.EnginePower;
import domain.models.car.transmission.Transmission;
import domain.models.car.transmission.TransmissionType;
import domain.models.car.types.*;
import dealerShipOrder.domain.models.testDriveRequest.TestDriveRequest;
import dealerShipOrder.domain.models.testDriveRequest.TestDriveStatus;
import dealerShipOrder.domain.models.users.client.Client;
import dealerShipOrder.domain.models.users.manager.Manager;
import domain.repository.carRepository.CarRepository;
import dealerShipOrder.domain.repository.testDriveRequestRepository.testDriveRequestRepository.TestDriveRequestRepository;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith({MockitoExtension.class, WithMockSecurityExtension.class})
@DisplayName("TestDriveManagerService Tests")
class TestDriveManagerServiceImplTest {

    @Mock
    private TestDriveRequestRepository testDriveRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CarRepository carRepository;

    @Mock
    private TestDriveMapper testDriveMapper;

    @InjectMocks
    private TestDriveManagerServiceImpl testDriveManagerService;

    private Manager manager;
    private TestDriveRequest testDriveRequest;
    private TestDriveResponse testDriveResponse;
    private LocalDateTime futureTime;
    private Car mockCar;
    private Client mockClient;

    @BeforeEach
    void setUp() {
        manager = new Manager("Jane", "Smith", null, "jane@email.com", "+9876543210", "password456", "emp456");
        futureTime = LocalDateTime.now().plusDays(2);
        testDriveRequest = new TestDriveRequest("req123", "client123", "car123", futureTime);
        testDriveResponse = new TestDriveResponse();
        mockCar = createMockCar();
        mockClient = createMockClient();
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
    @DisplayName("Should assign manager to request successfully")
    void shouldAssignManagerToRequestSuccessfully() {
        Car mockCar = createMockCar();

        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(manager));
        when(testDriveRepository.findById("req123")).thenReturn(Optional.of(testDriveRequest));
        when(carRepository.findById("car123")).thenReturn(Optional.of(mockCar));
        when(userRepository.findById("client123")).thenReturn(Optional.of(mock(Client.class)));
        when(testDriveRepository.save(any(TestDriveRequest.class))).thenReturn(testDriveRequest);
        when(userRepository.save(any(Manager.class))).thenReturn(manager);
        when(testDriveMapper.toResponse(any(), any(), any(), any())).thenReturn(testDriveResponse);

        TestDriveResponse result = testDriveManagerService.assignManager("req123");

        assertNotNull(result);
        assertEquals("test-user-id", testDriveRequest.getManagerId());
        assertEquals(TestDriveStatus.CONFIRMED, testDriveRequest.getStatus());
        assertTrue(manager.getManagedTestDrives().contains("req123"));
        verify(testDriveRepository, times(1)).save(testDriveRequest);
        verify(userRepository, times(1)).save(manager);
    }

    @Test
    @DisplayName("Should throw exception when manager already assigned")
    void shouldThrowExceptionWhenManagerAlreadyAssigned() {
        testDriveRequest.assignManager("otherManager");
        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(manager));
        when(testDriveRepository.findById("req123")).thenReturn(Optional.of(testDriveRequest));

        assertThrows(DomainValidationException.class, () -> {
            testDriveManagerService.assignManager("req123");
        });
    }

    @Test
    @DisplayName("Should throw exception when manager not found")
    void shouldThrowExceptionWhenManagerNotFound() {
        when(userRepository.findById("manager999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            testDriveManagerService.assignManager("req123");
        });
    }

    @Test
    @DisplayName("Should throw exception when request not found")
    void shouldThrowExceptionWhenRequestNotFound() {
        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(manager));
        when(testDriveRepository.findById("req999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            testDriveManagerService.assignManager("req999");
        });
    }

    @Test
    @DisplayName("Should get manager requests successfully")
    void shouldGetManagerRequestsSuccessfully() {
        testDriveRequest.assignManager("test-user-id");

        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(manager));
        when(testDriveRepository.findByManagerId("test-user-id")).thenReturn(List.of(testDriveRequest));
        when(carRepository.findById("car123")).thenReturn(Optional.of(mockCar));
        when(userRepository.findById("client123")).thenReturn(Optional.of(mockClient));
        when(testDriveMapper.toResponse(any(), any(), any(), any())).thenReturn(testDriveResponse);

        List<TestDriveResponse> result = testDriveManagerService.getMyRequests();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(testDriveRepository, times(1)).findByManagerId("test-user-id");
    }

    @Test
    @DisplayName("Should return empty list when manager has no requests")
    void shouldReturnEmptyListWhenManagerHasNoRequests() {
        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(manager));
        when(testDriveRepository.findByManagerId("test-user-id")).thenReturn(List.of());

        List<TestDriveResponse> result = testDriveManagerService.getMyRequests();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should get pending requests successfully")
    void shouldGetPendingRequestsSuccessfully() {
        when(testDriveRepository.findByStatus(TestDriveStatus.PENDING)).thenReturn(List.of(testDriveRequest));
        when(carRepository.findById("car123")).thenReturn(Optional.of(mockCar));
        when(userRepository.findById("client123")).thenReturn(Optional.of(mockClient));
        when(testDriveMapper.toResponse(any(), any(), any(), any())).thenReturn(testDriveResponse);

        List<TestDriveResponse> result = testDriveManagerService.getPendingRequests();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should return empty list when no pending requests")
    void shouldReturnEmptyListWhenNoPendingRequests() {
        when(testDriveRepository.findByStatus(TestDriveStatus.PENDING)).thenReturn(List.of());

        List<TestDriveResponse> result = testDriveManagerService.getPendingRequests();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should confirm request successfully")
    void shouldConfirmRequestSuccessfully() {
        testDriveRequest.assignManager("test-user-id");
        LocalDateTime confirmTime = LocalDateTime.now().plusDays(3);

        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(manager));
        when(testDriveRepository.findById("req123")).thenReturn(Optional.of(testDriveRequest));
        when(testDriveRepository.hasConflict("car123", confirmTime)).thenReturn(false);
        when(testDriveRepository.save(any(TestDriveRequest.class))).thenReturn(testDriveRequest);
        when(carRepository.findById("car123")).thenReturn(Optional.of(mockCar));
        when(userRepository.findById("client123")).thenReturn(Optional.of(mockClient));
        when(testDriveMapper.toResponse(any(), any(), any(), any())).thenReturn(testDriveResponse);

        TestDriveResponse result = testDriveManagerService.confirmRequest("req123", confirmTime);

        assertNotNull(result);
        assertEquals(confirmTime, testDriveRequest.getConfirmedTime());
        assertEquals(TestDriveStatus.CONFIRMED, testDriveRequest.getStatus());
    }

    @Test
    @DisplayName("Should throw exception when request not assigned to manager")
    void shouldThrowExceptionWhenRequestNotAssignedToManager() {
        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(manager));
        when(testDriveRepository.findById("req123")).thenReturn(Optional.of(testDriveRequest));

        assertThrows(DomainValidationException.class, () -> {
            testDriveManagerService.confirmRequest("req123", futureTime);
        });
    }

    @Test
    @DisplayName("Should throw exception when time slot is already booked")
    void shouldThrowExceptionWhenTimeSlotBookedForConfirmation() {
        testDriveRequest.assignManager("test-user-id");
        LocalDateTime confirmTime = LocalDateTime.now().plusDays(3);

        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(manager));
        when(testDriveRepository.findById("req123")).thenReturn(Optional.of(testDriveRequest));
        when(testDriveRepository.hasConflict("car123", confirmTime)).thenReturn(true);

        assertThrows(DomainValidationException.class, () -> {
            testDriveManagerService.confirmRequest("req123", confirmTime);
        });
    }

    @Test
    @DisplayName("Should complete request successfully")
    void shouldCompleteRequestSuccessfully() throws Exception {
        LocalDateTime pastTime = LocalDateTime.now().minusHours(1);

        TestDriveRequest pastRequest = new TestDriveRequest("req123", "client123", "car123", LocalDateTime.now().plusDays(1));

        java.lang.reflect.Field requestedTimeField = TestDriveRequest.class.getDeclaredField("requestedTime");
        requestedTimeField.setAccessible(true);
        requestedTimeField.set(pastRequest, pastTime);

        java.lang.reflect.Field confirmedTimeField = TestDriveRequest.class.getDeclaredField("confirmedTime");
        confirmedTimeField.setAccessible(true);
        confirmedTimeField.set(pastRequest, pastTime);

        java.lang.reflect.Field statusField = TestDriveRequest.class.getDeclaredField("status");
        statusField.setAccessible(true);
        statusField.set(pastRequest, TestDriveStatus.CONFIRMED);

        java.lang.reflect.Field managerIdField = TestDriveRequest.class.getDeclaredField("managerId");
        managerIdField.setAccessible(true);
        managerIdField.set(pastRequest, "test-user-id");

        manager.assignToTestDrive("req123");

        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(manager));
        when(testDriveRepository.findById("req123")).thenReturn(Optional.of(pastRequest));
        when(testDriveRepository.save(any(TestDriveRequest.class))).thenReturn(pastRequest);
        when(carRepository.findById("car123")).thenReturn(Optional.of(mockCar));
        when(userRepository.findById("client123")).thenReturn(Optional.of(mockClient));
        when(testDriveMapper.toResponse(any(), any(), any(), any())).thenReturn(testDriveResponse);

        TestDriveResponse result = testDriveManagerService.completeRequest("req123");

        assertNotNull(result);
        assertEquals(TestDriveStatus.COMPLETED, pastRequest.getStatus());
        verify(testDriveRepository, times(1)).save(pastRequest);
    }

    @Test
    @DisplayName("Should throw exception when completing future request")
    void shouldThrowExceptionWhenCompletingFutureRequest() {
        testDriveRequest.assignManager("test-user-id");
        testDriveRequest.confirmTime(futureTime);

        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(manager));
        when(testDriveRepository.findById("req123")).thenReturn(Optional.of(testDriveRequest));

        assertThrows(DomainValidationException.class, () -> {
            testDriveManagerService.completeRequest("req123");
        });
    }

    @Test
    @DisplayName("Should mark request as no-show successfully")
    void shouldMarkRequestAsNoShowSuccessfully() throws Exception {
        LocalDateTime pastTime = LocalDateTime.now().minusHours(1);

        TestDriveRequest pastRequest = new TestDriveRequest("req123", "client123", "car123", LocalDateTime.now().plusDays(1));

        java.lang.reflect.Field requestedTimeField = TestDriveRequest.class.getDeclaredField("requestedTime");
        requestedTimeField.setAccessible(true);
        requestedTimeField.set(pastRequest, pastTime);

        java.lang.reflect.Field confirmedTimeField = TestDriveRequest.class.getDeclaredField("confirmedTime");
        confirmedTimeField.setAccessible(true);
        confirmedTimeField.set(pastRequest, pastTime);

        java.lang.reflect.Field statusField = TestDriveRequest.class.getDeclaredField("status");
        statusField.setAccessible(true);
        statusField.set(pastRequest, TestDriveStatus.CONFIRMED);

        java.lang.reflect.Field managerIdField = TestDriveRequest.class.getDeclaredField("managerId");
        managerIdField.setAccessible(true);
        managerIdField.set(pastRequest, "test-user-id");

        manager.assignToTestDrive("req123");

        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(manager));
        when(testDriveRepository.findById("req123")).thenReturn(Optional.of(pastRequest));
        when(testDriveRepository.save(any(TestDriveRequest.class))).thenReturn(pastRequest);
        when(carRepository.findById("car123")).thenReturn(Optional.of(mockCar));
        when(userRepository.findById("client123")).thenReturn(Optional.of(mockClient));
        when(testDriveMapper.toResponse(any(), any(), any(), any())).thenReturn(testDriveResponse);

        TestDriveResponse result = testDriveManagerService.markNoShow("req123");

        assertNotNull(result);
        assertEquals(TestDriveStatus.NO_SHOW, pastRequest.getStatus());
    }

    @Test
    @DisplayName("Should throw exception when marking no-show for request not assigned to manager")
    void shouldThrowExceptionWhenMarkingNoShowForUnassignedRequest() {
        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(manager));
        when(testDriveRepository.findById("req123")).thenReturn(Optional.of(testDriveRequest));

        assertThrows(DomainValidationException.class, () -> {
            testDriveManagerService.markNoShow("req123");
        });
    }
}