package applicationTest.carServices.carUsersTest;

import application.dtos.request.carRequest.ApplyConfigurationRequest;
import application.dtos.response.carResponse.CarResponse;
import application.dtos.response.carResponse.componentResponse.CarConfigurationResponse;
import application.mapper.CarMapper;
import application.services.carService.client.CarClientServiceImpl;
import domain.exception.DomainValidationException;
import domain.exception.EntityNotFoundException;
import domain.models.car.Car;
import domain.models.car.CarConfiguration;
import domain.models.car.CarModel;
import domain.models.car.Price;
import domain.models.car.componentModels.Component;
import domain.models.car.componentModels.ComponentType;
import domain.models.car.engine.Engine;
import domain.models.car.engine.EngineDisplacement;
import domain.models.car.engine.EngineFuelType;
import domain.models.car.engine.EnginePower;
import domain.models.car.transmission.Transmission;
import domain.models.car.transmission.TransmissionType;
import domain.models.car.types.*;
import domain.models.order.Order;
import domain.models.testDriveRequest.TestDriveRequest;
import domain.models.users.client.Client;
import domain.repository.carRepository.CarRepository;
import domain.repository.carRepository.ConfigurationRepository;
import domain.repository.testDriveRequestRepository.TestDriveRequestRepository;
import domain.repository.orderRepository.OrderRepository;
import domain.repository.userRepository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CarClientService Tests")
class CarClientServiceImplTest {

    @Mock
    private CarRepository carRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ConfigurationRepository configurationRepository;

    @Mock
    private TestDriveRequestRepository testDriveRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CarMapper carMapper;

    @InjectMocks
    private CarClientServiceImpl carClientService;

    private Client client;
    private Car car;
    private CarConfiguration configuration;
    private ApplyConfigurationRequest applyRequest;

    @BeforeEach
    void setUp() {
        client = new Client("emp123", "John", "Doe", null, "john@email.com", "+1234567890", "password123");
        car = createTestCar();
        car.markAsAvailable();

        configuration = createTestConfiguration();

        applyRequest = new ApplyConfigurationRequest();
        applyRequest.setCarId("car123");
        applyRequest.setConfigurationId("config123");
        applyRequest.setClientId("client123");
        applyRequest.setSelectedComponents(Map.of("WHEELS", "wheel1"));
    }

    private Car createTestCar() {
        Engine engine = new Engine("eng1", EngineFuelType.PETROL,
                new EngineDisplacement(2.0), new EnginePower(184));
        Transmission transmission = new Transmission(TransmissionType.AUTOMATIC, 8);
        return new Car("car123", CarBrand.BMW, new CarModel("ddd","320i", CarBrand.BMW, "G20"),
                CarBody.SEDAN, CarColor.BLACK, DriveType.REAR, engine, transmission,
                new Price(BigDecimal.valueOf(3500000), Currency.getInstance("RUB"), false));
    }

    private CarConfiguration createTestConfiguration() {
        return new CarConfiguration("config123", "Sport",
                new CarModel("cdf","320i", CarBrand.BMW, "G20"),
                Map.of(), new Price(BigDecimal.valueOf(3700000), Currency.getInstance("RUB"), false));
    }

    @Test
    @DisplayName("Should apply configuration successfully")
    void shouldApplyConfigurationSuccessfully() {
        CarModel carModel = new CarModel("model1", "320i", CarBrand.BMW, "G20");

        Component wheelComponent = new Component(
                "wheel1",
                ComponentType.WHEELS,
                "Sport Wheels",
                "19 inch alloy wheels",
                Price.of(50000.0, "RUB"),
                Set.of(carModel)
        );

        Map<ComponentType, Component> baseComponents = Map.of(ComponentType.WHEELS, wheelComponent);
        CarConfiguration configurationWithComponents = new CarConfiguration(
                "config123",
                "Sport",
                carModel,
                baseComponents,
                Price.of(3700000.0, "RUB")
        );

        when(carRepository.findById(applyRequest.getCarId())).thenReturn(Optional.of(car));
        when(configurationRepository.findById(applyRequest.getConfigurationId()))
                .thenReturn(Optional.of(configurationWithComponents));
        when(carRepository.save(any(Car.class))).thenReturn(car);
        when(carMapper.toResponse(any(Car.class))).thenReturn(new CarResponse());

        CarResponse result = carClientService.applyConfiguration(applyRequest);

        assertNotNull(result);
        verify(carRepository, times(1)).save(car);
    }

    @Test
    @DisplayName("Should throw exception when applying to non-existent car")
    void shouldThrowExceptionWhenApplyingToNonExistentCar() {
        when(carRepository.findById(applyRequest.getCarId())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            carClientService.applyConfiguration(applyRequest);
        });
    }

    @Test
    @DisplayName("Should throw exception when configuration not found")
    void shouldThrowExceptionWhenConfigurationNotFound() {
        when(carRepository.findById(applyRequest.getCarId())).thenReturn(Optional.of(car));
        when(configurationRepository.findById(applyRequest.getConfigurationId())).thenReturn(Optional.empty());

        assertThrows(DomainValidationException.class, () -> {
            carClientService.applyConfiguration(applyRequest);
        });
    }

    @Test
    @DisplayName("Should get configurations for model")
    void shouldGetConfigurationsForModel() {
        when(configurationRepository.findByModelId("model123")).thenReturn(List.of(configuration));
        when(carMapper.toConfigurationResponse(any(CarConfiguration.class))).thenReturn(new CarConfigurationResponse());

        List<CarConfigurationResponse> result = carClientService.getConfigurationsForModel("model123");

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(carMapper, times(1)).toConfigurationResponse(any(CarConfiguration.class));
    }

    @Test
    @DisplayName("Should send test drive request successfully")
    void shouldSendTestDriveRequestSuccessfully() {
        car.addToTestDriveFleet();
        when(userRepository.findById("client123")).thenReturn(Optional.of(client));
        when(carRepository.findById("car123")).thenReturn(Optional.of(car));
        when(testDriveRepository.save(any(TestDriveRequest.class))).thenAnswer(i -> i.getArgument(0));
        when(userRepository.save(any(Client.class))).thenReturn(client);

        carClientService.sendTestDriveRequest("car123",  LocalDateTime.now().plusDays(2));

        verify(testDriveRepository, times(1)).save(any(TestDriveRequest.class));
        verify(userRepository, times(1)).save(client);
    }

    @Test
    @DisplayName("Should throw when car not available for test drive")
    void shouldThrowWhenCarNotAvailableForTestDrive() {
        when(userRepository.findById("client123")).thenReturn(Optional.of(client));
        when(carRepository.findById("car123")).thenReturn(Optional.of(car));

        assertThrows(DomainValidationException.class, () -> {
            carClientService.sendTestDriveRequest("car123", LocalDateTime.now().plusDays(1));
        });
    }

    @Test
    @DisplayName("Should throw when test drive time is in past")
    void shouldThrowWhenTestDriveTimeInPast() {
        car.addToTestDriveFleet();
        when(userRepository.findById("client123")).thenReturn(Optional.of(client));
        when(carRepository.findById("car123")).thenReturn(Optional.of(car));

        assertThrows(DomainValidationException.class, () -> {
            carClientService.sendTestDriveRequest("car123",  LocalDateTime.now().minusDays(1));
        });
    }

    @Test
    @DisplayName("Should make order on car successfully")
    void shouldMakeOrderOnCarSuccessfully() {
        when(userRepository.findById("client123")).thenReturn(Optional.of(client));
        when(carRepository.findById("car123")).thenReturn(Optional.of(car));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
        when(userRepository.save(any(Client.class))).thenReturn(client);
        when(carRepository.save(any(Car.class))).thenReturn(car);

        carClientService.makeOrderOnCar("car123");

        verify(orderRepository, times(1)).save(any(Order.class));
        verify(userRepository, times(1)).save(client);
        verify(carRepository, times(1)).save(car);
    }

    @Test
    @DisplayName("Should throw when car not available for purchase")
    void shouldThrowWhenCarNotAvailableForPurchase() {
        car.markAsSold();
        when(userRepository.findById("client123")).thenReturn(Optional.of(client));
        when(carRepository.findById("car123")).thenReturn(Optional.of(car));

        assertThrows(DomainValidationException.class, () -> {
            carClientService.makeOrderOnCar("car123");
        });
    }
}