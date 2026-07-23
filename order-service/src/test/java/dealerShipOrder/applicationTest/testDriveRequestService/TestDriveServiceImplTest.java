package dealerShipOrder.applicationTest.testDriveRequestService;

import dealerShipOrder.application.dtos.request.testDriveRequest.TestDriveFilterRequest;
import dealerShipOrder.application.dtos.response.testDriveResponse.TestDriveListResponse;
import dealerShipOrder.application.dtos.response.testDriveResponse.TestDriveResponse;
import dealerShipOrder.application.mapper.TestDriveMapper;
import dealerShipOrder.application.services.testDriveService.TestDriveServiceImpl;
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
import dealerShipOrder.domain.models.users.client.Client;
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
@DisplayName("TestDriveService Tests")
class TestDriveServiceImplTest {

    @Mock
    private TestDriveRequestRepository testDriveRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CarRepository carRepository;

    @Mock
    private TestDriveMapper testDriveMapper;

    @InjectMocks
    private TestDriveServiceImpl testDriveService;

    private TestDriveRequest testDriveRequest;
    private TestDriveResponse testDriveResponse;
    private Car mockCar;
    private Client mockClient;

    @BeforeEach
    void setUp() {
        LocalDateTime futureTime = LocalDateTime.now().plusDays(2);
        testDriveRequest = new TestDriveRequest("req123", "test-user-id", "car123", futureTime);
        testDriveResponse = new TestDriveResponse();
        testDriveResponse.setId("req123");
        testDriveResponse.setStatus("PENDING");

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
        return new Client("test-user-id", "John", "Doe", null, "john@email.com", "+1234567890", "password123");
    }

    @Test
    @DisplayName("Should get test drive by id successfully")
    void shouldGetTestDriveByIdSuccessfully() {
        when(testDriveRepository.findById("req123")).thenReturn(Optional.of(testDriveRequest));
        when(carRepository.findById("car123")).thenReturn(Optional.of(mockCar));
        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(mockClient));
        when(testDriveMapper.toResponse(any(), any(), any(), any())).thenReturn(testDriveResponse);

        TestDriveResponse result = testDriveService.getTestDriveById("req123");

        assertNotNull(result);
        verify(testDriveRepository, times(1)).findById("req123");
        verify(carRepository, times(1)).findById("car123");
        verify(userRepository, times(1)).findById("test-user-id");
        verify(testDriveMapper, times(1)).toResponse(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should throw exception when test drive not found")
    void shouldThrowExceptionWhenTestDriveNotFound() {
        when(testDriveRepository.findById("req999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            testDriveService.getTestDriveById("req999");
        });
    }

    @Test
    @DisplayName("Should get all test drives successfully")
    void shouldGetAllTestDrivesSuccessfully() {
        when(testDriveRepository.findAll()).thenReturn(List.of(testDriveRequest));
        when(carRepository.findById("car123")).thenReturn(Optional.of(mockCar));
        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(mockClient));
        when(testDriveMapper.toResponse(any(), any(), any(), any())).thenReturn(testDriveResponse);

        List<TestDriveResponse> result = testDriveService.getAllTestDrives();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(testDriveRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no test drives")
    void shouldReturnEmptyListWhenNoTestDrives() {
        when(testDriveRepository.findAll()).thenReturn(List.of());

        List<TestDriveResponse> result = testDriveService.getAllTestDrives();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(testDriveRepository, times(1)).findAll();
        verify(testDriveMapper, never()).toResponse(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should filter test drives by client")
    void shouldFilterTestDrivesByClient() {
        TestDriveFilterRequest filter = new TestDriveFilterRequest();
        filter.setClientId("test-user-id");

        when(testDriveRepository.findAll()).thenReturn(List.of(testDriveRequest));
        when(carRepository.findById("car123")).thenReturn(Optional.of(mockCar));
        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(mockClient));
        when(testDriveMapper.toResponse(any(), any(), any(), any())).thenReturn(testDriveResponse);

        TestDriveListResponse result = testDriveService.getTestDrivesWithFilters(filter);

        assertNotNull(result);
        assertEquals(1, result.getTestDrives().size());
        assertEquals(1, result.getTotalCount());
        verify(testDriveRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should filter test drives by status")
    void shouldFilterTestDrivesByStatus() {
        TestDriveFilterRequest filter = new TestDriveFilterRequest();
        filter.setStatus("PENDING");

        when(testDriveRepository.findAll()).thenReturn(List.of(testDriveRequest));
        when(carRepository.findById("car123")).thenReturn(Optional.of(mockCar));
        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(mockClient));
        when(testDriveMapper.toResponse(any(), any(), any(), any())).thenReturn(testDriveResponse);

        TestDriveListResponse result = testDriveService.getTestDrivesWithFilters(filter);

        assertNotNull(result);
        assertEquals(1, result.getTestDrives().size());
        assertEquals(1, result.getPendingCount());
    }

    @Test
    @DisplayName("Should filter test drives by date range")
    void shouldFilterTestDrivesByDateRange() {
        TestDriveFilterRequest filter = new TestDriveFilterRequest();
        filter.setDateFrom(LocalDateTime.now().minusDays(1));
        filter.setDateTo(LocalDateTime.now().plusDays(7));

        when(testDriveRepository.findAll()).thenReturn(List.of(testDriveRequest));
        when(carRepository.findById("car123")).thenReturn(Optional.of(mockCar));
        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(mockClient));
        when(testDriveMapper.toResponse(any(), any(), any(), any())).thenReturn(testDriveResponse);

        TestDriveListResponse result = testDriveService.getTestDrivesWithFilters(filter);

        assertNotNull(result);
        assertEquals(1, result.getTestDrives().size());
    }

    @Test
    @DisplayName("Should filter test drives by upcoming")
    void shouldFilterTestDrivesByUpcoming() {
        TestDriveFilterRequest filter = new TestDriveFilterRequest();
        filter.setUpcoming(true);

        when(testDriveRepository.findAll()).thenReturn(List.of(testDriveRequest));
        when(carRepository.findById("car123")).thenReturn(Optional.of(mockCar));
        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(mockClient));
        when(testDriveMapper.toResponse(any(), any(), any(), any())).thenReturn(testDriveResponse);

        TestDriveListResponse result = testDriveService.getTestDrivesWithFilters(filter);

        assertNotNull(result);
        assertEquals(1, result.getTestDrives().size());
    }

    @Test
    @DisplayName("Should combine multiple filters")
    void shouldCombineMultipleFilters() {
        TestDriveFilterRequest filter = new TestDriveFilterRequest();
        filter.setClientId("test-user-id");
        filter.setStatus("PENDING");
        filter.setUpcoming(true);

        when(testDriveRepository.findAll()).thenReturn(List.of(testDriveRequest));
        when(carRepository.findById("car123")).thenReturn(Optional.of(mockCar));
        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(mockClient));
        when(testDriveMapper.toResponse(any(), any(), any(), any())).thenReturn(testDriveResponse);

        TestDriveListResponse result = testDriveService.getTestDrivesWithFilters(filter);

        assertNotNull(result);
        assertEquals(1, result.getTestDrives().size());
        assertEquals(1, result.getTotalCount());
        assertEquals(1, result.getPendingCount());
    }

    @Test
    @DisplayName("Should return empty list when no matches")
    void shouldReturnEmptyListWhenNoMatches() {
        TestDriveFilterRequest filter = new TestDriveFilterRequest();
        filter.setClientId("otherClient");

        when(testDriveRepository.findAll()).thenReturn(List.of(testDriveRequest));

        TestDriveListResponse result = testDriveService.getTestDrivesWithFilters(filter);

        assertNotNull(result);
        assertTrue(result.getTestDrives().isEmpty());
        assertEquals(0, result.getTotalCount());
    }

    @Test
    @DisplayName("Should handle empty filter")
    void shouldHandleEmptyFilter() {
        TestDriveFilterRequest filter = new TestDriveFilterRequest();

        when(testDriveRepository.findAll()).thenReturn(List.of(testDriveRequest));
        when(carRepository.findById("car123")).thenReturn(Optional.of(mockCar));
        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(mockClient));
        when(testDriveMapper.toResponse(any(), any(), any(), any())).thenReturn(testDriveResponse);

        TestDriveListResponse result = testDriveService.getTestDrivesWithFilters(filter);

        assertNotNull(result);
        assertEquals(1, result.getTestDrives().size());
    }
}