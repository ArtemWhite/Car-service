package applicationTest.sparePartServices;

import application.mapper.SparePartMapper;
import application.services.sparePartService.BaseSparePartService;
import domain.exception.EntityNotFoundException;
import domain.models.car.CarModel;
import domain.models.car.types.CarBrand;
import domain.models.sparePart.SparePart;
import domain.models.sparePart.SpareType;
import domain.repository.carRepository.CarRepository;
import domain.repository.sparePartRepository.SparePartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import applicationTest.WithMockSecurityExtension;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith({MockitoExtension.class, WithMockSecurityExtension.class})
@DisplayName("BaseSparePartService Tests")
class BaseSparePartServiceTest {

    @Mock
    private SparePartRepository sparePartRepository;

    @Mock
    private CarRepository carRepository;

    @Mock
    private SparePartMapper sparePartMapper;

    private TestBaseSparePartService baseService;
    private SparePart sparePart;

    @BeforeEach
    void setUp() {
        baseService = new TestBaseSparePartService(
                sparePartRepository,
                carRepository,
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
    }

    private static class TestBaseSparePartService extends BaseSparePartService {
        public TestBaseSparePartService(
                SparePartRepository sparePartRepository,
                CarRepository carRepository,
                SparePartMapper sparePartMapper) {
            super(sparePartRepository, carRepository, sparePartMapper);
        }

        public SparePart testFindSparePartById(String id) {
            return findSparePartById(id);
        }

        public Set<CarModel> testFindCompatibleModels(Set<String> modelIds) {
            return findCompatibleModels(modelIds);
        }

        public int testGetStockQuantity(String partId) {
            return sparePartRepository.getStockQuantity(partId);
        }

        public void testSetStockQuantity(String partId, int quantity, String section, String location) {
            sparePartRepository.updateStock(partId, quantity, section, location);
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
