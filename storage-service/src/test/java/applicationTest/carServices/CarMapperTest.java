package applicationTest.carServices;

import application.dtos.request.carRequest.CreateCarRequest;
import application.dtos.request.carRequest.UpdateCarRequest;
import application.dtos.request.carRequest.CarFilterRequest;
import application.dtos.response.carResponse.CarResponse;
import application.dtos.response.carResponse.componentResponse.CarConfigurationResponse;
import application.dtos.response.carResponse.componentResponse.ComponentResponse;
import application.mapper.CarMapper;
import domain.models.car.*;
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

import java.math.BigDecimal;
import java.util.*;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith({MockitoExtension.class, WithMockSecurityExtension.class})
@DisplayName("CarMapper Tests")
class CarMapperTest {

    @Mock
    private CarRepository carRepository;

    @InjectMocks
    private CarMapper carMapper;

    private Car car;
    private CarConfiguration configuration;

    @BeforeEach
    void setUp() {
        // РСЃРїРѕР»СЊР·СѓРµРј lenient() С‡С‚РѕР±С‹ РёР·Р±РµР¶Р°С‚СЊ UnnecessaryStubbingException
        lenient().when(carRepository.findEngineByFuelTypePowerAndDisplacement(any(), anyDouble(), anyDouble()))
                .thenReturn(Optional.empty());

        lenient().when(carRepository.saveEngine(any(Engine.class)))
                .thenAnswer(invocation -> {
                    Engine engine = invocation.getArgument(0);
                    return new Engine(
                            UUID.randomUUID().toString(),
                            engine.getEngineFuelType(),
                            engine.getEngineDisplacement(),
                            engine.getEnginePower()
                    );
                });

        lenient().when(carRepository.findTransmissionByTypeAndGears(any(), anyInt()))
                .thenReturn(Optional.empty());

        lenient().when(carRepository.saveTransmission(any(Transmission.class)))
                .thenAnswer(invocation -> {
                    Transmission transmission = invocation.getArgument(0);
                    Transmission saved = new Transmission(
                            transmission.getTransmissionType(),
                            transmission.getGears()
                    );
                    saved.setId(UUID.randomUUID().toString());
                    return saved;
                });

        Engine engine = new Engine("eng1", EngineFuelType.PETROL,
                new EngineDisplacement(2.0), new EnginePower(184));
        Transmission transmission = new Transmission(TransmissionType.AUTOMATIC, 8);
        car = new Car("car123", CarBrand.BMW, new CarModel("dsw", "320i", CarBrand.BMW, "G20"),
                CarBody.SEDAN, CarColor.BLACK, DriveType.REAR, engine, transmission,
                new Price(BigDecimal.valueOf(3500000), Currency.getInstance("RUB"), false));

        car.markAsAvailable();

        CarModel carModel = new CarModel("123", "320i", CarBrand.BMW, "G20");
        Component wheelComponent = new Component(
                "wheel1",
                ComponentType.WHEELS,
                "19'' M-Sport",
                "Sport wheels",
                new Price(BigDecimal.valueOf(95000), Currency.getInstance("RUB"), false),
                Set.of(carModel)
        );

        configuration = new CarConfiguration(
                "config123",
                "Sport",
                carModel,
                Map.of(ComponentType.WHEELS, wheelComponent),
                new Price(BigDecimal.valueOf(3700000), Currency.getInstance("RUB"), false)
        );
    }

    @Test
    @DisplayName("Should convert CreateCarRequest to Car")
    void shouldConvertCreateCarRequestToCar() {
        CreateCarRequest request = new CreateCarRequest();
        request.setBrand("BMW");
        request.setModel("320i");
        request.setBodyType("SEDAN");
        request.setColor("BLACK");
        request.setDriveType("REAR");
        request.setEngineFuelType("PETROL");
        request.setEnginePower(184.0);
        request.setEngineDisplacement(2.0);
        request.setTransmissionGears(8);
        request.setTransmissionType("AUTOMATIC");
        request.setPrice(3500000.0);

        Car result = carMapper.toDomain(request);

        assertNotNull(result);
        assertEquals(CarBrand.BMW, result.getBrand());
        assertEquals("320i", result.getModel().getName());
        assertEquals(CarBody.SEDAN, result.getBody());
        assertEquals(CarColor.BLACK, result.getColor());
        assertEquals(DriveType.REAR, result.getDriveType());

        assertNotNull(result.getEngine(), "Engine should not be null");
        assertEquals(EngineFuelType.PETROL, result.getEngine().getEngineFuelType());
        assertEquals(184, result.getEngine().getEnginePower().getHorsePower());
        assertEquals(2.0, result.getEngine().getEngineDisplacement().getLiters());

        assertNotNull(result.getTransmission(), "Transmission should not be null");
        assertEquals(TransmissionType.AUTOMATIC, result.getTransmission().getTransmissionType());
        assertEquals(8, result.getTransmission().getGears());

        assertEquals(3500000, result.getPrice().getAmount().doubleValue());

        // РџСЂРѕРІРµСЂСЏРµРј С‚РѕР»СЊРєРѕ С‡С‚Рѕ РјРµС‚РѕРґ Р±С‹Р» РІС‹Р·РІР°РЅ, Р±РµР· verify РґР»СЏ save РјРµС‚РѕРґРѕРІ
        verify(carRepository, atLeastOnce()).findEngineByFuelTypePowerAndDisplacement(any(), anyDouble(), anyDouble());
        verify(carRepository, atLeastOnce()).findTransmissionByTypeAndGears(any(), anyInt());
    }

    // Р’СЃРµ РѕСЃС‚Р°Р»СЊРЅС‹Рµ С‚РµСЃС‚С‹ РѕСЃС‚Р°СЋС‚СЃСЏ Р±РµР· РёР·РјРµРЅРµРЅРёР№
    @Test
    @DisplayName("Should update car from UpdateCarRequest")
    void shouldUpdateCarFromUpdateCarRequest() {
        UpdateCarRequest request = new UpdateCarRequest();
        request.setPrice(4000000.0);
        request.setStatus("SOLD");

        carMapper.updateDomain(car, request);

        assertEquals(4000000, car.getPrice().getAmount().doubleValue());
        assertEquals(CarStatus.SOLD, car.getCarStatus());
    }

    @Test
    @DisplayName("Should ignore null fields in UpdateCarRequest")
    void shouldIgnoreNullFieldsInUpdateCarRequest() {
        UpdateCarRequest request = new UpdateCarRequest();
        request.setPrice(null);
        request.setStatus(null);
        double originalPrice = car.getPrice().getAmount().doubleValue();

        carMapper.updateDomain(car, request);

        assertEquals(originalPrice, car.getPrice().getAmount().doubleValue());
        assertEquals(CarStatus.AVAILABLE, car.getCarStatus());
    }

    @Test
    @DisplayName("Should update car status to AVAILABLE")
    void shouldUpdateCarStatusToAvailable() {
        carMapper.updateCarStatus(car, "AVAILABLE");

        assertEquals(CarStatus.AVAILABLE, car.getCarStatus());
    }

    @Test
    @DisplayName("Should update car status to SOLD")
    void shouldUpdateCarStatusToSold() {
        carMapper.updateCarStatus(car, "SOLD");

        assertEquals(CarStatus.SOLD, car.getCarStatus());
    }

    @Test
    @DisplayName("Should update car status to TEST_DRIVE_AVAILABLE")
    void shouldUpdateCarStatusToTestDriveAvailable() {
        carMapper.updateCarStatus(car, "TEST_DRIVE_AVAILABLE");

        assertEquals(CarStatus.TEST_DRIVE_AVAILABLE, car.getCarStatus());
    }

    @Test
    @DisplayName("Should update car status to IN_SERVICE")
    void shouldUpdateCarStatusToInService() {
        carMapper.updateCarStatus(car, "IN_SERVICE");

        assertEquals(CarStatus.IN_SERVICE, car.getCarStatus());
    }

    @Test
    @DisplayName("Should update car status to RESERVED")
    void shouldUpdateCarStatusToReserved() {
        carMapper.updateCarStatus(car, "RESERVED");

        assertEquals(CarStatus.RESERVED, car.getCarStatus());
    }

    @Test
    @DisplayName("Should update car status to UNAVAILABLE")
    void shouldUpdateCarStatusToUnavailable() {
        carMapper.updateCarStatus(car, "UNAVAILABLE");

        assertEquals(CarStatus.UNAVAILABLE, car.getCarStatus());
    }

    @Test
    @DisplayName("Should not change status when updating to invalid status")
    void shouldNotChangeStatusWhenUpdatingToInvalidStatus() {
        CarStatus originalStatus = car.getCarStatus();

        carMapper.updateCarStatus(car, "INVALID_STATUS");

        assertEquals(originalStatus, car.getCarStatus());
    }

    @Test
    @DisplayName("Should convert CarFilterRequest to CarFilter")
    void shouldConvertCarFilterRequestToCarFilter() {
        CarFilterRequest request = new CarFilterRequest();
        request.setBrand("BMW");
        request.setModel("320i");
        request.setBodyType("SEDAN");
        request.setColor("BLACK");
        request.setDriveType("REAR");
        request.setMinPrice(3000000.0);
        request.setMaxPrice(4000000.0);

        CarMapper.CarFilter result = carMapper.toDomainFilter(request);

        assertEquals(CarBrand.BMW, result.getBrand());
        assertEquals("320i", result.getModel().getName());
        assertEquals(CarBody.SEDAN, result.getBodyType());
        assertEquals(CarColor.BLACK, result.getColor());
        assertEquals(DriveType.REAR, result.getDriveType());
        assertEquals(3000000, result.getMinPrice().getAmount().doubleValue());
        assertEquals(4000000, result.getMaxPrice().getAmount().doubleValue());
    }

    @Test
    @DisplayName("Should handle null fields in CarFilterRequest")
    void shouldHandleNullFieldsInCarFilterRequest() {
        CarFilterRequest request = new CarFilterRequest();

        CarMapper.CarFilter result = carMapper.toDomainFilter(request);

        assertNull(result.getBrand());
        assertNull(result.getModel());
        assertNull(result.getBodyType());
        assertNull(result.getColor());
        assertNull(result.getDriveType());
        assertNull(result.getMinPrice());
        assertNull(result.getMaxPrice());
    }

    @Test
    @DisplayName("Should convert Car to CarResponse")
    void shouldConvertCarToCarResponse() {
        CarResponse result = carMapper.toResponse(car);

        assertNotNull(result);
        assertEquals("car123", result.getId());
        assertEquals("BMW", result.getBrand());
        assertEquals("320i", result.getModel());
        assertEquals("SEDAN", result.getBodyType());
        assertEquals("BLACK", result.getColor());
        assertEquals("REAR", result.getDriveType());
        assertEquals(3500000, result.getPrice());
        assertNotNull(result.getPriceFormatted());
        assertTrue(result.isAvailableForPurchase());
    }

    @Test
    @DisplayName("Should format price correctly")
    void shouldFormatPriceCorrectly() {
        CarResponse result = carMapper.toResponse(car);

        assertNotNull(result.getPriceFormatted());
        assertTrue(result.getPriceFormatted().replace(" ", " ").contains("3 500 000"));
        assertTrue(result.getPriceFormatted().contains("₽"));
    }

    @Test
    @DisplayName("Should set available flags correctly")
    void shouldSetAvailableFlagsCorrectly() {
        CarResponse result = carMapper.toResponse(car);

        assertTrue(result.isAvailableForPurchase());
        assertFalse(result.isAvailableForTestDrive());

        car.addToTestDriveFleet();
        CarResponse testDriveResponse = carMapper.toResponse(car);

        assertFalse(testDriveResponse.isAvailableForPurchase());
        assertTrue(testDriveResponse.isAvailableForTestDrive());
    }

    @Test
    @DisplayName("Should convert list of Cars to list of CarResponses")
    void shouldConvertListOfCarsToListOfCarResponses() {
        List<Car> cars = Arrays.asList(car, car);

        List<CarResponse> result = carMapper.toResponseList(cars);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should return empty list for empty input")
    void shouldReturnEmptyListForEmptyInput() {
        List<Car> cars = Collections.emptyList();

        List<CarResponse> result = carMapper.toResponseList(cars);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should convert CarConfiguration to CarConfigurationResponse")
    void shouldConvertCarConfigurationToCarConfigurationResponse() {
        CarConfigurationResponse result = carMapper.toConfigurationResponse(configuration);

        assertNotNull(result);
        assertEquals("config123", result.getId());
        assertEquals("Sport", result.getName());
        assertEquals("320i", result.getModelName());
        assertEquals(
                "3 700 000 ₽".replace(" ", " "),
                result.getBasePrice().replace(" ", " ")
        );
        assertNotNull(result.getBaseComponents());
        assertEquals(1, result.getBaseComponents().size());
    }

    @Test
    @DisplayName("Should convert configuration with selected components")
    void shouldConvertConfigurationWithSelectedComponents() {
        Map<ComponentType, Component> selectedComponents = configuration.getBaseComponents();

        CarConfigurationResponse result = carMapper.toConfigurationResponse(configuration, selectedComponents);

        assertNotNull(result);
        assertEquals("config123", result.getId());
        assertNotNull(result.getTotalPrice());
    }

    @Test
    @DisplayName("Should handle empty component list in configuration")
    void shouldHandleEmptyComponentList() {
        CarConfiguration emptyConfig = new CarConfiguration(
                "empty",
                "Empty",
                new CarModel("dsf", "320i", CarBrand.BMW, "G20"),
                Collections.emptyMap(),
                new Price(BigDecimal.valueOf(3500000), Currency.getInstance("RUB"), false)
        );

        CarConfigurationResponse result = carMapper.toConfigurationResponse(emptyConfig);

        assertNotNull(result);
        assertTrue(result.getBaseComponents().isEmpty());
    }

    @Test
    @DisplayName("Should convert Component to ComponentResponse")
    void shouldConvertComponentToComponentResponse() {
        Component component = configuration.getBaseComponents().get(ComponentType.WHEELS);

        ComponentResponse result = carMapper.toComponentResponse(component, true);

        assertNotNull(result);
        assertEquals("wheel1", result.getId());
        assertEquals("WHEELS", result.getType());
        assertEquals("Колёса", result.getTypeDisplayName());
        assertEquals("19'' M-Sport", result.getName());
        assertEquals("Sport wheels", result.getDescription());
        assertEquals(
                "95 000 ₽".replace(" ", " "),
                result.getPrice().replace(" ", " ")
        );
        assertTrue(result.isSelected());
    }

    @Test
    @DisplayName("Should set selected flag to false")
    void shouldSetSelectedFlagToFalse() {
        Component component = configuration.getBaseComponents().get(ComponentType.WHEELS);

        ComponentResponse result = carMapper.toComponentResponse(component, false);

        assertFalse(result.isSelected());
    }

    @Test
    @DisplayName("Should format price with ruble symbol")
    void shouldFormatPriceWithRubleSymbol() {
        Price price = new Price(BigDecimal.valueOf(1500000), Currency.getInstance("RUB"), false);

        String result = carMapper.formatPrice(price);

        assertTrue(result.replace(" ", " ").contains("1 500 000"));
        assertTrue(result.contains("₽"));
    }

    @Test
    @DisplayName("Should format price with kopecks")
    void shouldFormatPriceWithKopecks() {
        Price price = new Price(BigDecimal.valueOf(1500000.50), Currency.getInstance("RUB"), false);

        String result = carMapper.formatPrice(price);

        assertTrue(result.replace(" ", " ").contains("1 500 001"));
    }

    @Test
    @DisplayName("Should format large numbers with spaces")
    void shouldFormatLargeNumbersWithSpaces() {
        Price price = new Price(BigDecimal.valueOf(1234567890), Currency.getInstance("RUB"), false);

        String result = carMapper.formatPrice(price);

        assertTrue(result.replace(" ", " ").contains("1 234 567 890"));
    }
}