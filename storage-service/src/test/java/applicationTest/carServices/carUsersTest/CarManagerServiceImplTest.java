package applicationTest.carServices.carUsersTest;

import application.dtos.response.carResponse.CarResponse;
import application.dtos.response.carResponse.componentResponse.CarConfigurationResponse;
import application.mapper.CarMapper;
import application.services.carService.manager.CarManagerServiceImpl;
import domain.exception.DomainValidationException;
import domain.models.car.Car;
import domain.models.car.CarConfiguration;
import domain.models.car.CarModel;
import domain.models.car.Price;
import domain.models.car.engine.Engine;
import domain.models.car.engine.EngineDisplacement;
import domain.models.car.engine.EngineFuelType;
import domain.models.car.engine.EnginePower;
import domain.models.car.transmission.Transmission;
import domain.models.car.transmission.TransmissionType;
import domain.models.car.types.*;
import domain.models.order.Order;
import domain.models.users.manager.Manager;
import domain.repository.carRepository.CarRepository;
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
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import domain.repository.carRepository.ConfigurationRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("CarManagerService Tests")
class CarManagerServiceImplTest {

    @Mock
    private CarRepository carRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ConfigurationRepository configurationRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CarMapper carMapper;

    @InjectMocks
    private CarManagerServiceImpl carManagerService;

    private Manager manager;
    private Car car;

    @BeforeEach
    void setUp() {
        manager = new Manager("John", "Doe", "Michael", "john@email.com", "+1234567890", "password123", "emp123");
        car = createTestCar();
    }

    private Car createTestCar() {
        Engine engine = new Engine("eng1", EngineFuelType.PETROL,
                new EngineDisplacement(2.0), new EnginePower(184));
        Transmission transmission = new Transmission(TransmissionType.AUTOMATIC, 8);
        return new Car("car123", CarBrand.BMW, new CarModel("dsf","320i", CarBrand.BMW, "G20"),
                CarBody.SEDAN, CarColor.BLACK, DriveType.REAR, engine, transmission,
                new Price(BigDecimal.valueOf(3500000), Currency.getInstance("RUB"), false));
    }

    @Test
    @DisplayName("Should add car to test drive fleet successfully")
    void shouldAddCarToTestDriveFleetSuccessfully() {
        car.markAsAvailable();
        when(userRepository.findById("manager123")).thenReturn(Optional.of(manager));
        when(carRepository.findById("car123")).thenReturn(Optional.of(car));
        when(carRepository.save(any(Car.class))).thenReturn(car);
        when(userRepository.save(any(Manager.class))).thenReturn(manager);

        carManagerService.addCarToTestDriveFleet("car123");

        assertEquals(CarStatus.TEST_DRIVE_AVAILABLE, car.getCarStatus());
        assertTrue(manager.getTestDriveFleet().contains("car123"));
        verify(carRepository, times(1)).save(car);
        verify(userRepository, times(1)).save(manager);
    }

    @Test
    @DisplayName("Should throw when adding non-available car to test drive fleet")
    void shouldThrowWhenAddingNonAvailableCarToTestDriveFleet() {
        car.markAsAvailable();
        car.markAsSold();
        when(userRepository.findById("manager123")).thenReturn(Optional.of(manager));
        when(carRepository.findById("car123")).thenReturn(Optional.of(car));

        assertThrows(DomainValidationException.class, () -> {
            carManagerService.addCarToTestDriveFleet("car123");
        });
    }

    @Test
    @DisplayName("Should remove car from test drive fleet successfully")
    void shouldRemoveCarFromTestDriveFleetSuccessfully() {
        car.markAsAvailable();
        car.addToTestDriveFleet();
        manager.addCarToTestDriveFleet("car123");

        when(userRepository.findById("manager123")).thenReturn(Optional.of(manager));
        when(carRepository.findById("car123")).thenReturn(Optional.of(car));
        when(carRepository.save(any(Car.class))).thenReturn(car);
        when(userRepository.save(any(Manager.class))).thenReturn(manager);

        carManagerService.removeCarFromTestDriveFleet("car123");

        assertEquals(CarStatus.AVAILABLE, car.getCarStatus());
        assertFalse(manager.getTestDriveFleet().contains("car123"));
    }

    @Test
    @DisplayName("Should get test drive fleet")
    void shouldGetTestDriveFleet() {
        when(carRepository.findCarsForTestDrive()).thenReturn(List.of(car));
        when(carMapper.toResponseList(List.of(car))).thenReturn(List.of(new CarResponse()));

        List<CarResponse> result = carManagerService.getTestDriveFleet();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should get orders on available cars")
    void shouldGetOrdersOnAvailableCars() {
        Order order = Order.createInStockOrder("order1", "client123", "car123");
        order.assignManager("manager123");
        order.awaitPayment();
        order.markAsPaid();

        when(orderRepository.findAll()).thenReturn(List.of(order));
        when(carRepository.findById("car123")).thenReturn(Optional.of(car));
        when(carMapper.toResponse(any(Car.class))).thenReturn(new CarResponse());

        List<CarResponse> result = carManagerService.getOrdersOnAvailableCars();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should filter only paid and ready for pickup orders")
    void shouldFilterOnlyPaidAndReadyOrders() {
        Order pendingOrder = Order.createInStockOrder("order1", "client123", "car123");

        Order paidOrder = Order.createInStockOrder("order2", "client124", "car124");
        paidOrder.assignManager("manager123");
        paidOrder.awaitPayment();
        paidOrder.markAsPaid();

        when(orderRepository.findAll()).thenReturn(List.of(pendingOrder, paidOrder));
        when(carRepository.findById("car124")).thenReturn(Optional.of(car));
        when(carMapper.toResponse(any(Car.class))).thenReturn(new CarResponse());

        List<CarResponse> result = carManagerService.getOrdersOnAvailableCars();

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should get orders on configuration cars")
    void shouldGetOrdersOnConfigurationCars() {
        Order customOrder = Order.createCustomOrder("rr","client123", "config123", "model123");
        customOrder.confirmByStock();

        when(orderRepository.findAll()).thenReturn(List.of(customOrder));

        CarConfiguration config = new CarConfiguration(
                "config123",
                "Sport",
                new CarModel("model123", "320i", CarBrand.BMW, "G20"),
                Map.of(),
                Price.of(3700000.0, "RUB")
        );
        when(configurationRepository.findById("config123")).thenReturn(Optional.of(config));

        when(carMapper.toConfigurationResponse(any(CarConfiguration.class)))
                .thenReturn(new CarConfigurationResponse());

        List<CarConfigurationResponse> result = carManagerService.getOrdersOnConfigurationCars();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should filter only stock confirmed and awaiting delivery orders")
    void shouldFilterOnlyStockConfirmedAndAwaitingOrders() {
        Order pendingOrder = Order.createCustomOrder(
                "order1",
                "client123",
                "config123",
                "model123"
        );

        Order confirmedOrder = Order.createCustomOrder(
                "order2",
                "client124",
                "config124",
                "model124"
        );
        confirmedOrder.confirmByStock();

        when(orderRepository.findAll()).thenReturn(List.of(pendingOrder, confirmedOrder));

        CarConfiguration configForConfirmed = createTestConfiguration("2");
        when(configurationRepository.findById("config124")).thenReturn(Optional.of(configForConfirmed));

        when(carMapper.toConfigurationResponse(any(CarConfiguration.class)))
                .thenReturn(new CarConfigurationResponse());

        List<CarConfigurationResponse> result = carManagerService.getOrdersOnConfigurationCars();

        assertEquals(1, result.size());

        verify(configurationRepository, never()).findById("config123");
        verify(configurationRepository, times(1)).findById("config124");
    }

    private CarConfiguration createTestConfiguration(String id) {
        return new CarConfiguration("config123", "Sport",
                new CarModel(id,"320i", CarBrand.BMW, "G20"),
                Map.of(), new Price(BigDecimal.valueOf(3700000), Currency.getInstance("RUB"), false));
    }
}