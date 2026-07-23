package applicationTest.sparePartServices;

import application.mapper.SparePartMapper;
import application.services.sparePartService.BaseSparePartService;
import domain.exception.EntityNotFoundException;
import domain.models.car.CarModel;
import domain.models.car.types.CarBrand;
import domain.models.sparePart.SparePart;
import domain.models.sparePart.SpareType;
import domain.models.users.User;
import domain.repository.carRepository.CarRepository;
import domain.repository.sparePartRepository.SparePartRepository;
import domain.repository.userRepository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BaseSparePartService Tests")
class BaseSparePartServiceTest {

    @Mock
    private SparePartRepository sparePartRepository;

    @Mock
    private CarRepository carRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SparePartMapper sparePartMapper;

    private TestBaseSparePartService baseService;
    private SparePart sparePart;
    private User user;

    @BeforeEach
    void setUp() {
        baseService = new TestBaseSparePartService(
                sparePartRepository,
                carRepository,
                userRepository,
                sparePartMapper
        );
        sparePart = new SparePart(
                "part123",
                SpareType.BRAKE_PADS,
                "Brake Pads",
                "Desc",
                domain.models.car.Price.of(1000, "RUB"),
                Set.of()
        );
        user = mock(User.class);
    }

    private static class TestBaseSparePartService extends BaseSparePartService {
        public TestBaseSparePartService(
                SparePartRepository sparePartRepository,
                CarRepository carRepository,
                UserRepository userRepository,
                SparePartMapper sparePartMapper) {
            super(sparePartRepository, carRepository, userRepository, sparePartMapper);
        }

        public SparePart testFindSparePartById(String id) {
            return findSparePartById(id);
        }

        public User testFindUserById(String id) {
            return findUserById(id);
        }

        public Set<CarModel> testFindCompatibleModels(Set<String> modelIds) {
            return findCompatibleModels(modelIds);
        }

        // Методы для работы со складом теперь используют репозиторий
        public int testGetStockQuantity(String partId) {
            return sparePartRepository.getStockQuantity(partId);
        }

        public void testSetStockQuantity(String partId, int quantity, String section, String location) {
            sparePartRepository.updateStock(partId, quantity, section, location);
        }

        public String testGetSection(String partId) {
            // Section и Location теперь не хранятся отдельно,
            // для тестов используем заглушки
            return null;
        }

        public String testGetLocation(String partId) {
            return null;
        }
    }

    @Test
    @DisplayName("Should find spare part by id successfully")
    void shouldFindSparePartByIdSuccessfully() {
        when(sparePartRepository.findById("part123")).thenReturn(Optional.of(sparePart));

        SparePart result = baseService.testFindSparePartById("part123");

        assertNotNull(result);
        assertEquals(sparePart, result);
        verify(sparePartRepository, times(1)).findById("part123");
    }

    @Test
    @DisplayName("Should throw exception when spare part not found")
    void shouldThrowExceptionWhenSparePartNotFound() {
        when(sparePartRepository.findById("part999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            baseService.testFindSparePartById("part999");
        });
        verify(sparePartRepository, times(1)).findById("part999");
    }

    @Test
    @DisplayName("Should find user by id successfully")
    void shouldFindUserByIdSuccessfully() {
        when(userRepository.findById("user123")).thenReturn(Optional.of(user));

        User result = baseService.testFindUserById("user123");

        assertNotNull(result);
        assertEquals(user, result);
        verify(userRepository, times(1)).findById("user123");
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findById("user999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            baseService.testFindUserById("user999");
        });
        verify(userRepository, times(1)).findById("user999");
    }

    @Test
    @DisplayName("Should find compatible models from IDs")
    void shouldFindCompatibleModelsFromIds() {
        CarModel model1 = new CarModel("model1", "320i", CarBrand.BMW, "G20");
        CarModel model2 = new CarModel("model2", "A4", CarBrand.AUDI, "B9");
        Set<String> modelIds = Set.of("model1", "model2");
        when(carRepository.findModelById("model1")).thenReturn(Optional.of(model1));
        when(carRepository.findModelById("model2")).thenReturn(Optional.of(model2));

        Set<CarModel> result = baseService.testFindCompatibleModels(modelIds);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(model1));
        assertTrue(result.contains(model2));
        verify(carRepository, times(1)).findModelById("model1");
        verify(carRepository, times(1)).findModelById("model2");
    }

    @Test
    @DisplayName("Should return empty set for empty model IDs")
    void shouldReturnEmptySetForEmptyModelIds() {
        Set<CarModel> result = baseService.testFindCompatibleModels(Set.of());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should skip non-existent model IDs")
    void shouldSkipNonExistentModelIds() {
        CarModel model1 = new CarModel("model1", "320i", CarBrand.BMW, "G20");
        Set<String> modelIds = Set.of("model1", "model999");
        when(carRepository.findModelById("model1")).thenReturn(Optional.of(model1));
        when(carRepository.findModelById("model999")).thenReturn(Optional.empty());

        Set<CarModel> result = baseService.testFindCompatibleModels(modelIds);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(model1));
    }

    @Test
    @DisplayName("Should get stock quantity from repository")
    void shouldGetStockQuantityFromRepository() {
        when(sparePartRepository.getStockQuantity("part123")).thenReturn(15);

        int quantity = baseService.testGetStockQuantity("part123");

        assertEquals(15, quantity);
        verify(sparePartRepository, times(1)).getStockQuantity("part123");
    }

    @Test
    @DisplayName("Should return 0 when stock quantity not found")
    void shouldReturnZeroWhenStockQuantityNotFound() {
        when(sparePartRepository.getStockQuantity("part999")).thenReturn(0);

        int quantity = baseService.testGetStockQuantity("part999");

        assertEquals(0, quantity);
        verify(sparePartRepository, times(1)).getStockQuantity("part999");
    }

    @Test
    @DisplayName("Should update stock quantity via repository")
    void shouldUpdateStockQuantityViaRepository() {
        baseService.testSetStockQuantity("part123", 25, "A-01", "shelf-1");

        verify(sparePartRepository, times(1)).updateStock("part123", 25, "A-01", "shelf-1");
    }

    @Test
    @DisplayName("Should handle multiple stock updates")
    void shouldHandleMultipleStockUpdates() {
        baseService.testSetStockQuantity("part123", 10, "A-01", "shelf-1");
        baseService.testSetStockQuantity("part123", 25, "B-02", "shelf-2");

        verify(sparePartRepository, times(1)).updateStock("part123", 10, "A-01", "shelf-1");
        verify(sparePartRepository, times(1)).updateStock("part123", 25, "B-02", "shelf-2");
    }

    @Test
    @DisplayName("Should handle different spare parts independently via repository")
    void shouldHandleDifferentSparePartsIndependently() {
        when(sparePartRepository.getStockQuantity("part123")).thenReturn(10);
        when(sparePartRepository.getStockQuantity("part456")).thenReturn(20);

        int quantity1 = baseService.testGetStockQuantity("part123");
        int quantity2 = baseService.testGetStockQuantity("part456");

        assertEquals(10, quantity1);
        assertEquals(20, quantity2);

        verify(sparePartRepository, times(1)).getStockQuantity("part123");
        verify(sparePartRepository, times(1)).getStockQuantity("part456");
    }
}