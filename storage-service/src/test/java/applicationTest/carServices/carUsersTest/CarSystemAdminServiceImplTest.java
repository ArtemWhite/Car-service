package applicationTest.carServices.carUsersTest;

import application.dtos.request.carRequest.CreateCarRequest;
import application.dtos.request.carRequest.UpdateCarRequest;
import application.dtos.response.carResponse.CarResponse;
import application.mapper.CarMapper;
import application.services.carService.adminSystem.CarSystemAdminServiceImpl;
import domain.exception.DomainValidationException;
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
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith({MockitoExtension.class, WithMockSecurityExtension.class})
@DisplayName("CarSystemAdminService Tests")
class CarSystemAdminServiceImplTest {

    @Mock
    private CarRepository carRepository;

    @Mock
    private CarMapper carMapper;

    @InjectMocks
    private CarSystemAdminServiceImpl carAdminService;

    private Car car;
    private CreateCarRequest createRequest;
    private UpdateCarRequest updateRequest;
    private CarResponse carResponse;

    @BeforeEach
    void setUp() {
        car = createTestCar();

        createRequest = new CreateCarRequest();
        createRequest.setBrand("BMW");
        createRequest.setModel("320i");
        createRequest.setBodyType("SEDAN");
        createRequest.setColor("BLACK");
        createRequest.setDriveType("REAR");
        createRequest.setEngineFuelType("PETROL");
        createRequest.setEnginePower(184.0);
        createRequest.setEngineDisplacement(2.0);
        createRequest.setTransmissionGears(8);
        createRequest.setTransmissionType("AUTOMATIC");
        createRequest.setPrice(3500000.0);

        updateRequest = new UpdateCarRequest();
        updateRequest.setPrice(4000000.0);

        carResponse = new CarResponse();
        carResponse.setId("car123");
        carResponse.setBrand("BMW");
    }

    private Car createTestCar() {
        Engine engine = new Engine("eng1", EngineFuelType.PETROL,
                new EngineDisplacement(2.0), new EnginePower(184));
        Transmission transmission = new Transmission(TransmissionType.AUTOMATIC, 8);
        Car newCar = new Car("car123", CarBrand.BMW, new CarModel("ccc","320i", CarBrand.BMW, "G20"),
                CarBody.SEDAN, CarColor.BLACK, DriveType.REAR, engine, transmission,
                new Price(BigDecimal.valueOf(3500000), Currency.getInstance("RUB"), false));
        newCar.markAsAvailable();
        return newCar;
    }

    @Test
    @DisplayName("Should create car successfully")
    void shouldCreateCarSuccessfully() {
        when(carRepository.findModelByNameAndBrand(eq("320i"), eq(CarBrand.BMW))).thenReturn(Optional.empty());
        when(carMapper.toDomain(any(CreateCarRequest.class), isNull())).thenReturn(car);
        when(carRepository.save(any(Car.class))).thenReturn(car);
        when(carMapper.toResponse(car)).thenReturn(carResponse);

        CarResponse result = carAdminService.createCar(createRequest);

        assertNotNull(result);
        assertEquals("car123", result.getId());
        verify(carRepository, times(1)).save(car);
    }

    @Test
    @DisplayName("Should create car with AVAILABLE status by default")
    void shouldCreateCarWithAvailableStatus() {
        when(carRepository.findModelByNameAndBrand(eq("320i"), eq(CarBrand.BMW))).thenReturn(Optional.empty());
        when(carMapper.toDomain(any(CreateCarRequest.class), isNull())).thenReturn(car);
        when(carRepository.save(any(Car.class))).thenReturn(car);
        when(carMapper.toResponse(car)).thenReturn(carResponse);

        carAdminService.createCar(createRequest);

        verify(carRepository).save(argThat(savedCar ->
                savedCar.getCarStatus() == CarStatus.AVAILABLE
        ));
    }

    @Test
    @DisplayName("Should update car successfully")
    void shouldUpdateCarSuccessfully() {
        when(carRepository.findById("car123")).thenReturn(Optional.of(car));
        when(carRepository.save(any(Car.class))).thenReturn(car);
        when(carMapper.toResponse(car)).thenReturn(carResponse);

        CarResponse result = carAdminService.updateCar("car123", updateRequest);

        assertNotNull(result);
        verify(carRepository, times(1)).save(car);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent car")
    void shouldThrowExceptionWhenUpdatingNonExistentCar() {
        when(carRepository.findById("car999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            carAdminService.updateCar("car999", updateRequest);
        });
    }

    @Test
    @DisplayName("Should throw exception when updating sold car")
    void shouldThrowExceptionWhenUpdatingSoldCar() {
        car.markAsSold();
        when(carRepository.findById("car123")).thenReturn(Optional.of(car));

        assertThrows(DomainValidationException.class, () -> {
            carAdminService.updateCar("car123", updateRequest);
        });
    }

    @Test
    @DisplayName("Should delete unavailable car successfully")
    void shouldDeleteUnavailableCarSuccessfully() {
        car.markAsUnavailable();
        when(carRepository.findById("car123")).thenReturn(Optional.of(car));
        doNothing().when(carRepository).delete("car123");

        carAdminService.deleteCar("car123", "Too old");

        verify(carRepository, times(1)).delete("car123");
    }

    @Test
    @DisplayName("Should NOT throw exception when deleting unavailable car")
    void shouldNotThrowExceptionWhenDeletingUnavailableCar() {
        Car unavailableCar = createTestCar();
        unavailableCar.markAsUnavailable();

        when(carRepository.findById("car123")).thenReturn(Optional.of(unavailableCar));
        doNothing().when(carRepository).delete("car123");

        assertDoesNotThrow(() -> carAdminService.deleteCar("car123", "Reason"));

        verify(carRepository, times(1)).delete("car123");
    }

    @Test
    @DisplayName("Should change car status successfully")
    void shouldChangeCarStatusSuccessfully() {
        when(carRepository.findById("car123")).thenReturn(Optional.of(car));
        when(carRepository.save(any(Car.class))).thenReturn(car);
        when(carMapper.toResponse(car)).thenReturn(carResponse);

        CarResponse result = carAdminService.changeCarStatus("car123", "SOLD");

        assertNotNull(result);
        verify(carRepository, times(1)).save(car);
    }

    @Test
    @DisplayName("Should get all cars successfully")
    void shouldGetAllCarsSuccessfully() {
        when(carRepository.findAll()).thenReturn(List.of(car));
        when(carMapper.toResponseList(List.of(car))).thenReturn(List.of(carResponse));

        List<CarResponse> result = carAdminService.getAllCars();

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
