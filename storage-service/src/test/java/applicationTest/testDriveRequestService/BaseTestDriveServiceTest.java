package applicationTest.testDriveRequestService;

import application.mapper.TestDriveMapper;
import application.services.testDriveService.BaseTestDriveService;
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
import domain.models.users.User;
import domain.models.users.client.Client;
import domain.models.users.manager.Manager;
import domain.repository.carRepository.CarRepository;
import domain.repository.testDriveRequestRepository.TestDriveRequestRepository;
import domain.repository.userRepository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BaseTestDriveService Tests")
class BaseTestDriveServiceTest {

    @Mock
    private TestDriveRequestRepository testDriveRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CarRepository carRepository;

    @Mock
    private TestDriveMapper testDriveMapper;

    private TestBaseTestDriveService baseService;
    private TestDriveRequest testDriveRequest;
    private Client client;
    private Manager manager;
    private Car car;

    @BeforeEach
    void setUp() {
        baseService = new TestBaseTestDriveService(
                testDriveRepository,
                userRepository,
                carRepository,
                testDriveMapper
        );

        LocalDateTime futureTime = LocalDateTime.now().plusDays(2);
        testDriveRequest = new TestDriveRequest("req123", "client123", "car123", futureTime);

        client = new Client("emp123", "John", "Doe", null, "john@email.com", "+1234567890", "password123");
        manager = new Manager("Jane", "Smith", null, "jane@email.com", "+9876543210", "password456", "emp456");
        car = createTestCar();
    }

    private static class TestBaseTestDriveService extends BaseTestDriveService {
        public TestBaseTestDriveService(
                TestDriveRequestRepository testDriveRepository,
                UserRepository userRepository,
                CarRepository carRepository,
                TestDriveMapper testDriveMapper) {
            super(testDriveRepository, userRepository, carRepository, testDriveMapper);
        }

        public TestDriveRequest testFindRequestById(String id) {
            return findRequestById(id);
        }

        public User testFindUserById(String id) {
            return findUserById(id);
        }

        public Client testFindClientById(String id) {
            return findClientById(id);
        }

        public Manager testFindManagerById(String id) {
            return findManagerById(id);
        }

        public Car testFindCarById(String id) {
            return findCarById(id);
        }

        public void testCheckCarAvailableForTestDrive(String carId) {
            checkCarAvailableForTestDrive(carId);
        }

        public void testCheckRequestBelongsToClient(TestDriveRequest request, String clientId) {
            checkRequestBelongsToClient(request, clientId);
        }

        public void testCheckRequestAssignedToManager(TestDriveRequest request, String managerId) {
            checkRequestAssignedToManager(request, managerId);
        }

        public String testGetClientName(String clientId) {
            return getClientName(clientId);
        }

        public String testGetManagerName(String managerId) {
            return getManagerName(managerId);
        }

        public TestDriveRequest testSaveRequest(TestDriveRequest request) {
            return saveRequest(request);
        }
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
    @DisplayName("Should find request by id successfully")
    void shouldFindRequestByIdSuccessfully() {
        when(testDriveRepository.findById("req123")).thenReturn(Optional.of(testDriveRequest));

        TestDriveRequest result = baseService.testFindRequestById("req123");

        assertNotNull(result);
        assertEquals(testDriveRequest, result);
    }

    @Test
    @DisplayName("Should throw exception when request not found")
    void shouldThrowExceptionWhenRequestNotFound() {
        when(testDriveRepository.findById("req999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            baseService.testFindRequestById("req999");
        });
    }

    @Test
    @DisplayName("Should find user by id successfully")
    void shouldFindUserByIdSuccessfully() {
        when(userRepository.findById("user123")).thenReturn(Optional.of(client));

        User result = baseService.testFindUserById("user123");

        assertNotNull(result);
        assertEquals(client, result);
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findById("user999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            baseService.testFindUserById("user999");
        });
    }

    @Test
    @DisplayName("Should find client by id successfully")
    void shouldFindClientByIdSuccessfully() {
        when(userRepository.findById("client123")).thenReturn(Optional.of(client));

        Client result = baseService.testFindClientById("client123");

        assertNotNull(result);
        assertEquals(client, result);
    }

    @Test
    @DisplayName("Should throw exception when user is not a client")
    void shouldThrowExceptionWhenUserIsNotClient() {
        when(userRepository.findById("manager123")).thenReturn(Optional.of(manager));

        assertThrows(DomainValidationException.class, () -> {
            baseService.testFindClientById("manager123");
        });
    }

    @Test
    @DisplayName("Should find manager by id successfully")
    void shouldFindManagerByIdSuccessfully() {
        when(userRepository.findById("manager123")).thenReturn(Optional.of(manager));

        Manager result = baseService.testFindManagerById("manager123");

        assertNotNull(result);
        assertEquals(manager, result);
    }

    @Test
    @DisplayName("Should throw exception when user is not a manager")
    void shouldThrowExceptionWhenUserIsNotManager() {
        when(userRepository.findById("client123")).thenReturn(Optional.of(client));

        assertThrows(DomainValidationException.class, () -> {
            baseService.testFindManagerById("client123");
        });
    }

    @Test
    @DisplayName("Should find car by id successfully")
    void shouldFindCarByIdSuccessfully() {
        when(carRepository.findById("car123")).thenReturn(Optional.of(car));

        Car result = baseService.testFindCarById("car123");

        assertNotNull(result);
        assertEquals(car, result);
    }

    @Test
    @DisplayName("Should throw exception when car not found")
    void shouldThrowExceptionWhenCarNotFound() {
        when(carRepository.findById("car999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            baseService.testFindCarById("car999");
        });
    }

    @Test
    @DisplayName("Should check car available for test drive successfully")
    void shouldCheckCarAvailableForTestDriveSuccessfully() {
        Car availableCar = createTestCar();
        availableCar.markAsAvailable();
        availableCar.addToTestDriveFleet();
        when(carRepository.findById("car123")).thenReturn(Optional.of(availableCar));

        assertDoesNotThrow(() -> {
            baseService.testCheckCarAvailableForTestDrive("car123");
        });
    }

    @Test
    @DisplayName("Should throw exception when car not available for test drive")
    void shouldThrowExceptionWhenCarNotAvailableForTestDrive() {
        Car unavailableCar = createTestCar();
        unavailableCar.markAsAvailable();
        unavailableCar.markAsSold();
        when(carRepository.findById("car123")).thenReturn(Optional.of(unavailableCar));

        assertThrows(DomainValidationException.class, () -> {
            baseService.testCheckCarAvailableForTestDrive("car123");
        });
    }

    @Test
    @DisplayName("Should check request belongs to client successfully")
    void shouldCheckRequestBelongsToClientSuccessfully() {
        assertDoesNotThrow(() -> {
            baseService.testCheckRequestBelongsToClient(testDriveRequest, "client123");
        });
    }

    @Test
    @DisplayName("Should throw exception when request does not belong to client")
    void shouldThrowExceptionWhenRequestDoesNotBelongToClient() {
        assertThrows(DomainValidationException.class, () -> {
            baseService.testCheckRequestBelongsToClient(testDriveRequest, "otherClient");
        });
    }

    @Test
    @DisplayName("Should check request assigned to manager successfully")
    void shouldCheckRequestAssignedToManagerSuccessfully() {
        testDriveRequest.assignManager("manager123");

        assertDoesNotThrow(() -> {
            baseService.testCheckRequestAssignedToManager(testDriveRequest, "manager123");
        });
    }

    @Test
    @DisplayName("Should throw exception when request not assigned to manager")
    void shouldThrowExceptionWhenRequestNotAssignedToManager() {
        assertThrows(DomainValidationException.class, () -> {
            baseService.testCheckRequestAssignedToManager(testDriveRequest, "manager123");
        });
    }

    @Test
    @DisplayName("Should get client name")
    void shouldGetClientName() {
        when(userRepository.findById("client123")).thenReturn(Optional.of(client));

        String name = baseService.testGetClientName("client123");

        assertEquals("John Doe null", name);
    }

    @Test
    @DisplayName("Should throw exception when client not found")
    void shouldThrowExceptionWhenClientNotFound() {
        when(userRepository.findById("client999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            baseService.testGetClientName("client999");
        });
    }

    @Test
    @DisplayName("Should get manager name")
    void shouldGetManagerName() {
        when(userRepository.findById("manager123")).thenReturn(Optional.of(manager));

        String name = baseService.testGetManagerName("manager123");

        assertEquals("Jane Smith null", name);
    }

    @Test
    @DisplayName("Should return null for null manager id")
    void shouldReturnNullForNullManagerId() {
        String name = baseService.testGetManagerName(null);

        assertNull(name);
    }

    @Test
    @DisplayName("Should throw exception when manager not found")
    void shouldThrowExceptionWhenManagerNotFound() {
        when(userRepository.findById("manager999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            baseService.testGetManagerName("manager999");
        });
    }

    @Test
    @DisplayName("Should save request successfully")
    void shouldSaveRequestSuccessfully() {
        when(testDriveRepository.save(testDriveRequest)).thenReturn(testDriveRequest);

        TestDriveRequest result = baseService.testSaveRequest(testDriveRequest);

        assertNotNull(result);
        verify(testDriveRepository, times(1)).save(testDriveRequest);
    }
}