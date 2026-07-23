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
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith({MockitoExtension.class, WithMockSecurityExtension.class})
@DisplayName("CarClientService Tests")
class CarClientServiceImplTest {

    @Mock
    private CarRepository carRepository;

    @Mock
    private ConfigurationRepository configurationRepository;

    @Mock
    private CarMapper carMapper;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CarClientServiceImpl carClientService;

    private Car car;
    private CarConfiguration configuration;
    private ApplyConfigurationRequest applyRequest;

    @BeforeEach
    void setUp() {
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
}
