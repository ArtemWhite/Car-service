package applicationTest.carServices;

import application.mapper.CarMapper;
import application.services.carService.BaseCarService;
import domain.exception.EntityNotFoundException;
import domain.models.car.Car;
import domain.models.users.User;
import domain.repository.carRepository.CarRepository;
import domain.repository.userRepository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BaseCarService Tests")
class BaseCarServiceTest {

    @Mock
    private CarRepository carRepository;
    @Mock
    private CarMapper carMapper;

    @Mock
    private UserRepository userRepository;

    private TestBaseCarService baseCarService;

    @BeforeEach
    void setUp() {
        baseCarService = new TestBaseCarService(carRepository, userRepository, carMapper);
    }

    private static class TestBaseCarService extends BaseCarService {
        public TestBaseCarService(CarRepository carRepository, UserRepository userRepository, CarMapper carMapper) {
            super(carRepository, userRepository, carMapper);
        }

        public Car testFindCarById(String id) {
            return findCarById(id);
        }

        public User testFindUserById(String id) {
            return findUserById(id);
        }

        public Car testSaveCar(Car car) {
            return saveCar(car);
        }
    }

    @Test
    @DisplayName("Should find car by id successfully")
    void shouldFindCarByIdSuccessfully() {
        Car car = mock(Car.class);
        when(carRepository.findById("car123")).thenReturn(Optional.of(car));

        Car result = baseCarService.testFindCarById("car123");

        assertNotNull(result);
        assertEquals(car, result);
    }

    @Test
    @DisplayName("Should throw exception when car not found")
    void shouldThrowExceptionWhenCarNotFound() {
        when(carRepository.findById("car999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            baseCarService.testFindCarById("car999");
        });
    }

    @Test
    @DisplayName("Should find user by id successfully")
    void shouldFindUserByIdSuccessfully() {
        User user = mock(User.class);
        when(userRepository.findById("user123")).thenReturn(Optional.of(user));

        User result = baseCarService.testFindUserById("user123");

        assertNotNull(result);
        assertEquals(user, result);
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findById("user999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            baseCarService.testFindUserById("user999");
        });
    }

    @Test
    @DisplayName("Should save car successfully")
    void shouldSaveCarSuccessfully() {
        Car car = mock(Car.class);
        when(carRepository.save(car)).thenReturn(car);

        Car result = baseCarService.testSaveCar(car);

        assertNotNull(result);
        verify(carRepository, times(1)).save(car);
    }
}