package applicationTest.carServices.carUsersTest;

import application.dtos.response.carResponse.CarResponse;
import application.dtos.response.carResponse.componentResponse.CarConfigurationResponse;
import application.mapper.CarMapper;
import application.services.carService.manager.CarManagerServiceImpl;
import domain.exception.DomainValidationException;
import domain.models.car.Car;
import domain.models.car.CarModel;
import domain.models.car.Price;
import domain.models.car.engine.Engine;
import domain.models.car.engine.EngineDisplacement;
import domain.models.car.engine.EngineFuelType;
import domain.models.car.engine.EnginePower;
import domain.models.car.transmission.Transmission;
import domain.models.car.transmission.TransmissionType;
import domain.models.car.types.*;
import domain.repository.carRepository.CarRepository;
import domain.repository.carRepository.ConfigurationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import applicationTest.WithMockSecurityExtension;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith({MockitoExtension.class, WithMockSecurityExtension.class})
@DisplayName("CarManagerService Tests")
class CarManagerServiceImplTest {

    @Mock
    private CarRepository carRepository;

    @Mock
    private ConfigurationRepository configurationRepository;

    @Mock
    private CarMapper carMapper;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CarManagerServiceImpl carManagerService;

    private Car car;

    @BeforeEach
    void setUp() {
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
        when(carRepository.findById("car123")).thenReturn(Optional.of(car));
        when(carRepository.save(any(Car.class))).thenReturn(car);

        carManagerService.addCarToTestDriveFleet("car123");

        assertEquals(CarStatus.TEST_DRIVE_AVAILABLE, car.getCarStatus());
        verify(carRepository, times(1)).save(car);
    }

    @Test
    @DisplayName("Should throw when adding non-available car to test drive fleet")
    void shouldThrowWhenAddingNonAvailableCarToTestDriveFleet() {
        car.markAsAvailable();
        car.markAsSold();
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

        when(carRepository.findById("car123")).thenReturn(Optional.of(car));
        when(carRepository.save(any(Car.class))).thenReturn(car);

        carManagerService.removeCarFromTestDriveFleet("car123");

        assertEquals(CarStatus.AVAILABLE, car.getCarStatus());
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
}
