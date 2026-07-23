package applicationTest.testDriveRequestService.testDriveRequestUserTest;

import application.dtos.request.testDriveRequest.CreateTestDriveRequest;
import application.dtos.response.testDriveResponse.TestDriveResponse;
import application.mapper.TestDriveMapper;
import application.services.testDriveService.client.TestDriveClientServiceImpl;
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
@DisplayName("TestDriveClientService Tests")
class TestDriveClientServiceImplTest {

    @Mock
    private TestDriveRequestRepository testDriveRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CarRepository carRepository;

    @Mock
    private TestDriveMapper testDriveMapper;

    @InjectMocks
    private TestDriveClientServiceImpl testDriveClientService;

    private Client client;
    private Car car;
    private TestDriveRequest testDriveRequest;
    private CreateTestDriveRequest createRequest;
    private TestDriveResponse testDriveResponse;
    private LocalDateTime futureTime;

    @BeforeEach
    void setUp() {
        client = new Client("emp123", "John", "Doe", null, "john@email.com", "+1234567890", "password123");
        car = createTestCar();

        futureTime = LocalDateTime.now().plusDays(2);
        testDriveRequest = new TestDriveRequest("req123", "client123", "car123", futureTime);
        testDriveResponse = new TestDriveResponse();

        createRequest = new CreateTestDriveRequest();
        createRequest.setClientId("client123");
        createRequest.setCarId("car123");
        createRequest.setStartTime(futureTime);
        createRequest.setNotes("Test notes");
    }

    private Car createTestCar() {
        Engine engine = new Engine("eng1", EngineFuelType.PETROL,
                new EngineDisplacement(2.0), new EnginePower(184));
        Transmission transmission = new Transmission(TransmissionType.AUTOMATIC, 8);
        return new Car("car123", CarBrand.BMW,
                new CarModel("model1", "320i", CarBrand.BMW, "G20"),
                CarBody.SEDAN, CarColor.BLACK, DriveType.REAR, engine, transmission,
                new Price(BigDecimal.valueOf(3500000), Currency.getInstance("RUB"), false));
    }

    @Test
    @DisplayName("Should create test drive request successfully")
    void shouldCreateTestDriveRequestSuccessfully() {
        Car availableCar = createTestCar();
        availableCar.markAsAvailable();
        availableCar.addToTestDriveFleet();

        when(userRepository.findById("client123")).thenReturn(Optional.of(client));
        when(carRepository.findById("car123")).thenReturn(Optional.of(availableCar));
        when(testDriveRepository.hasConflict("car123", futureTime)).thenReturn(false);
        when(testDriveMapper.toDomain(createRequest)).thenReturn(testDriveRequest);
        when(testDriveRepository.save(any(TestDriveRequest.class))).thenReturn(testDriveRequest);
        when(testDriveMapper.toResponse(any(), any(), any(), any())).thenReturn(testDriveResponse);
        when(userRepository.save(any(Client.class))).thenReturn(client);

        TestDriveResponse result = testDriveClientService.createRequest(createRequest);

        assertNotNull(result);
        verify(testDriveRepository, times(1)).save(testDriveRequest);
        verify(userRepository, times(1)).save(client);
        assertTrue(client.getTestDriveRequests().contains("req123"));
    }

    @Test
    @DisplayName("Should throw exception when car not available for test drive")
    void shouldThrowExceptionWhenCarNotAvailable() {
        Car unavailableCar = createTestCar();
        // ✅ Сначала делаем AVAILABLE, потом продаём
        unavailableCar.markAsAvailable();
        unavailableCar.markAsSold();

        when(userRepository.findById("client123")).thenReturn(Optional.of(client));
        when(carRepository.findById("car123")).thenReturn(Optional.of(unavailableCar));

        assertThrows(DomainValidationException.class, () -> {
            testDriveClientService.createRequest(createRequest);
        });
    }

    @Test
    @DisplayName("Should throw exception when time slot is already booked")
    void shouldThrowExceptionWhenTimeSlotBooked() {
        Car availableCar = createTestCar();
        availableCar.markAsAvailable();
        availableCar.addToTestDriveFleet();

        when(userRepository.findById("client123")).thenReturn(Optional.of(client));
        when(carRepository.findById("car123")).thenReturn(Optional.of(availableCar));
        when(testDriveRepository.hasConflict("car123", futureTime)).thenReturn(true);

        assertThrows(DomainValidationException.class, () -> {
            testDriveClientService.createRequest(createRequest);
        });
    }

    @Test
    @DisplayName("Should throw exception when start time is in past")
    void shouldThrowExceptionWhenStartTimeInPast() {
        createRequest.setStartTime(LocalDateTime.now().minusDays(1));

        assertThrows(EntityNotFoundException.class, () -> {
            testDriveClientService.createRequest(createRequest);
        });
    }

    @Test
    @DisplayName("Should throw exception when client not found")
    void shouldThrowExceptionWhenClientNotFound() {
        when(userRepository.findById("client999")).thenReturn(Optional.empty());
        createRequest.setClientId("client999");

        assertThrows(EntityNotFoundException.class, () -> {
            testDriveClientService.createRequest(createRequest);
        });
    }

    @Test
    @DisplayName("Should get client requests successfully")
    void shouldGetClientRequestsSuccessfully() {
        Car availableCar = createTestCar();
        availableCar.markAsAvailable();
        availableCar.addToTestDriveFleet();

        when(userRepository.findById("client123")).thenReturn(Optional.of(client));
        when(testDriveRepository.findByClientId("client123")).thenReturn(List.of(testDriveRequest));
        when(carRepository.findById("car123")).thenReturn(Optional.of(availableCar));
        when(testDriveMapper.toResponse(any(), any(), any(), any())).thenReturn(testDriveResponse);

        List<TestDriveResponse> result = testDriveClientService.getMyRequests();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should return empty list when client has no requests")
    void shouldReturnEmptyListWhenClientHasNoRequests() {
        when(userRepository.findById("client123")).thenReturn(Optional.of(client));
        when(testDriveRepository.findByClientId("client123")).thenReturn(List.of());

        List<TestDriveResponse> result = testDriveClientService.getMyRequests();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should throw exception when client not found for get requests")
    void shouldThrowExceptionWhenClientNotFoundForGetRequests() {
        when(userRepository.findById("client999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            testDriveClientService.getMyRequests();
        });
    }

    @Test
    @DisplayName("Should cancel request successfully")
    void shouldCancelRequestSuccessfully() {
        Car availableCar = createTestCar();
        availableCar.markAsAvailable();
        availableCar.addToTestDriveFleet();

        when(userRepository.findById("client123")).thenReturn(Optional.of(client));
        when(testDriveRepository.findById("req123")).thenReturn(Optional.of(testDriveRequest));
        when(carRepository.findById("car123")).thenReturn(Optional.of(availableCar));
        when(testDriveRepository.save(any(TestDriveRequest.class))).thenReturn(testDriveRequest);
        when(testDriveMapper.toResponse(any(), any(), any(), any())).thenReturn(testDriveResponse);

        TestDriveResponse result = testDriveClientService.cancelRequest("req123", "Changed mind");

        assertNotNull(result);
        assertEquals(TestDriveStatus.CANCELLED, testDriveRequest.getStatus());
        verify(testDriveRepository, times(1)).save(testDriveRequest);
    }

    @Test
    @DisplayName("Should throw exception when request does not belong to client")
    void shouldThrowExceptionWhenRequestDoesNotBelongToClient() {
        when(userRepository.findById("otherClient")).thenReturn(Optional.of(mock(Client.class)));
        when(testDriveRepository.findById("req123")).thenReturn(Optional.of(testDriveRequest));

        assertThrows(DomainValidationException.class, () -> {
            testDriveClientService.cancelRequest("req123", "Reason");
        });
    }

    @Test
    @DisplayName("Should throw exception when request not found for cancel")
    void shouldThrowExceptionWhenRequestNotFoundForCancel() {
        when(userRepository.findById("client123")).thenReturn(Optional.of(client));
        when(testDriveRepository.findById("req999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            testDriveClientService.cancelRequest("req999", "Reason");
        });
    }


    @Test
    @DisplayName("Should reschedule request successfully")
    void shouldRescheduleRequestSuccessfully() {
        LocalDateTime newTime = LocalDateTime.now().plusDays(5);
        Car availableCar = createTestCar();
        availableCar.markAsAvailable();
        availableCar.addToTestDriveFleet();

        when(userRepository.findById("client123")).thenReturn(Optional.of(client));
        when(testDriveRepository.findById("req123")).thenReturn(Optional.of(testDriveRequest));
        when(testDriveRepository.hasConflict("car123", newTime)).thenReturn(false);
        when(testDriveRepository.save(any(TestDriveRequest.class))).thenReturn(testDriveRequest);
        when(carRepository.findById("car123")).thenReturn(Optional.of(availableCar));
        when(testDriveMapper.toResponse(any(), any(), any(), any())).thenReturn(testDriveResponse);

        TestDriveResponse result = testDriveClientService.rescheduleRequest("req123", newTime);

        assertNotNull(result);
        assertEquals(newTime, testDriveRequest.getRequestedTime());
        assertNull(testDriveRequest.getConfirmedTime());
        assertEquals(TestDriveStatus.PENDING, testDriveRequest.getStatus());
        verify(testDriveRepository, times(1)).save(testDriveRequest);
    }

    @Test
    @DisplayName("Should throw exception when rescheduling to conflicting time")
    void shouldThrowExceptionWhenReschedulingToConflictingTime() {
        LocalDateTime newTime = LocalDateTime.now().plusDays(5);
        Car availableCar = createTestCar();
        availableCar.markAsAvailable();
        availableCar.addToTestDriveFleet();

        when(userRepository.findById("client123")).thenReturn(Optional.of(client));
        when(testDriveRepository.findById("req123")).thenReturn(Optional.of(testDriveRequest));
        when(testDriveRepository.hasConflict("car123", newTime)).thenReturn(true);

        assertThrows(DomainValidationException.class, () -> {
            testDriveClientService.rescheduleRequest("req123", newTime);
        });
    }

    @Test
    @DisplayName("Should throw exception when rescheduling to past time")
    void shouldThrowExceptionWhenReschedulingToPastTime() {
        LocalDateTime pastTime = LocalDateTime.now().minusDays(1);
        when(userRepository.findById("client123")).thenReturn(Optional.of(client));
        when(testDriveRepository.findById("req123")).thenReturn(Optional.of(testDriveRequest));

        assertThrows(DomainValidationException.class, () -> {
            testDriveClientService.rescheduleRequest("req123", pastTime);
        });
    }

    @Test
    @DisplayName("Should throw exception when rescheduling request not found")
    void shouldThrowExceptionWhenReschedulingRequestNotFound() {
        LocalDateTime newTime = LocalDateTime.now().plusDays(5);
        when(userRepository.findById("client123")).thenReturn(Optional.of(client));
        when(testDriveRepository.findById("req999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            testDriveClientService.rescheduleRequest("req999", newTime);
        });
    }
}