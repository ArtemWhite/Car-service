package applicationTest.carServices;

import application.dtos.request.carRequest.CarFilterRequest;
import application.dtos.response.carResponse.CarResponse;
import application.mapper.CarMapper;
import application.services.carService.CarServiceImpl;
import domain.exception.EntityNotFoundException;
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
import domain.repository.orderRepository.OrderRepository;
import domain.repository.testDriveRequestRepository.TestDriveRequestRepository;
import domain.repository.userRepository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CarService Tests")
class CarServiceImplTest {

    @Mock
    private CarRepository carRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CarMapper carMapper;

    @Mock
    private ConfigurationRepository configurationRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private TestDriveRequestRepository testDriveRepository;

    @InjectMocks
    private CarServiceImpl carService;
    private Car car;

    @BeforeEach
    void setUp() {
        carService = new CarServiceImpl(
                carRepository,
                userRepository,
                carMapper
        );
        car = createTestCar();
    }

    private Car createTestCar() {
        Engine engine = new Engine("eng1", EngineFuelType.PETROL,
                new EngineDisplacement(2.0), new EnginePower(184));
        Transmission transmission = new Transmission(TransmissionType.AUTOMATIC, 8);
        return new Car("car123", CarBrand.BMW,
                new CarModel("model1", "320i", CarBrand.BMW, "G20"),
                CarBody.SEDAN, CarColor.BLACK, DriveType.REAR, engine, transmission,
                Price.of(3500000.0, "RUB"));
    }

    @Test
    @DisplayName("Should get car by id successfully")
    void shouldGetCarByIdSuccessfully() {
        when(carRepository.findById("car123")).thenReturn(Optional.of(car));
        when(carMapper.toResponse(car)).thenReturn(new CarResponse());

        CarResponse result = carService.getCarById("car123");

        assertNotNull(result);
        verify(carRepository).findById("car123");
        verify(carMapper).toResponse(car);
    }

    @Test
    @DisplayName("Should throw exception when car not found")
    void shouldThrowExceptionWhenCarNotFound() {
        when(carRepository.findById("car999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            carService.getCarById("car999");
        });
    }

    @Test
    @DisplayName("Should get all available cars")
    void shouldGetAllAvailableCars() {
        when(carRepository.findAvailableCars()).thenReturn(List.of(car));
        when(carMapper.toResponseList(List.of(car))).thenReturn(List.of(new CarResponse()));

        List<CarResponse> result = carService.getAvailableCars();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should return empty list when no available cars")
    void shouldReturnEmptyListWhenNoAvailableCars() {
        when(carRepository.findAvailableCars()).thenReturn(List.of());
        when(carMapper.toResponseList(List.of())).thenReturn(List.of());

        List<CarResponse> result = carService.getAvailableCars();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should filter cars by brand")
    void shouldFilterCarsByBrand() {
        CarFilterRequest filter = new CarFilterRequest();
        filter.setBrand("BMW");

        CarMapper.CarFilter domainFilter = mock(CarMapper.CarFilter.class);
        when(domainFilter.getBrand()).thenReturn(CarBrand.BMW);
        when(domainFilter.getModel()).thenReturn(null);
        when(domainFilter.getBodyType()).thenReturn(null);
        when(domainFilter.getColor()).thenReturn(null);
        when(domainFilter.getDriveType()).thenReturn(null);
        when(domainFilter.getMinPrice()).thenReturn(null);
        when(domainFilter.getMaxPrice()).thenReturn(null);

        when(carMapper.toDomainFilter(filter)).thenReturn(domainFilter);

        when(carRepository.findCarsByFilters(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(car));
        when(carMapper.toResponseList(anyList())).thenReturn(List.of(new CarResponse()));

        List<CarResponse> result = carService.getCarsWithFilters(filter);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should filter cars by price range")
    void shouldFilterCarsByPriceRange() {
        CarFilterRequest filter = new CarFilterRequest();
        filter.setMinPrice(3000000.0);
        filter.setMaxPrice(4000000.0);

        CarMapper.CarFilter domainFilter = new CarMapper.CarFilter(
                null, null, null, null, null,
                Price.of(3000000.0, "RUB"),
                Price.of(4000000.0, "RUB")
        );
        when(carMapper.toDomainFilter(filter)).thenReturn(domainFilter);

        when(carRepository.findCarsByFilters(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(car));
        when(carMapper.toResponseList(anyList())).thenReturn(List.of(new CarResponse()));

        List<CarResponse> result = carService.getCarsWithFilters(filter);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should combine multiple filters")
    void shouldCombineMultipleFilters() {
        CarFilterRequest filter = new CarFilterRequest();
        filter.setBrand("BMW");
        filter.setModel("320i");
        filter.setMinPrice(3000000.0);
        filter.setMaxPrice(4000000.0);
        filter.setFuelType("PETROL");

        CarMapper.CarFilter domainFilter = mock(CarMapper.CarFilter.class);
        when(carMapper.toDomainFilter(any(CarFilterRequest.class))).thenReturn(domainFilter);

        when(carRepository.findCarsByFilters(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(car));
        when(carMapper.toResponseList(anyList())).thenReturn(List.of(new CarResponse()));

        List<CarResponse> result = carService.getCarsWithFilters(filter);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should return empty list when no cars match filters")
    void shouldReturnEmptyListWhenNoCarsMatchFilters() {
        CarFilterRequest filter = new CarFilterRequest();
        filter.setBrand("NonExistentBrand");

        when(carMapper.toDomainFilter(any(CarFilterRequest.class)))
                .thenReturn(mock(CarMapper.CarFilter.class));

        when(carRepository.findCarsByFilters(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of());
        when(carMapper.toResponseList(anyList())).thenReturn(List.of());

        List<CarResponse> result = carService.getCarsWithFilters(filter);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should filter by engine characteristics")
    void shouldFilterByEngineCharacteristics() {
        CarFilterRequest filter = new CarFilterRequest();
        filter.setMinPower(150);
        filter.setMaxPower(200);
        filter.setMinEngineVolume(1.8);
        filter.setMaxEngineVolume(2.2);
        filter.setFuelType("PETROL");

        when(carMapper.toDomainFilter(any(CarFilterRequest.class)))
                .thenReturn(mock(CarMapper.CarFilter.class));

        when(carRepository.findCarsByFilters(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(car));
        when(carMapper.toResponseList(anyList())).thenReturn(List.of(new CarResponse()));

        List<CarResponse> result = carService.getCarsWithFilters(filter);

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}