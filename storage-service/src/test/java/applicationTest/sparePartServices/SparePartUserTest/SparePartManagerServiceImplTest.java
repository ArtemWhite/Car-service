package applicationTest.sparePartServices.SparePartUserTest;

import application.dtos.response.spareResponse.SparePartResponse;
import application.mapper.SparePartMapper;
import application.services.sparePartService.manager.SparePartManagerServiceImpl;
import domain.exception.DomainValidationException;
import domain.exception.EntityNotFoundException;
import domain.models.car.Price;
import domain.models.sparePart.SparePart;
import domain.models.sparePart.SpareType;
import domain.repository.carRepository.CarRepository;
import domain.repository.sparePartRepository.SparePartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import applicationTest.WithMockSecurityExtension;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith({MockitoExtension.class, WithMockSecurityExtension.class})
@DisplayName("SparePartManagerService Tests")
class SparePartManagerServiceImplTest {

    @Mock
    private SparePartRepository sparePartRepository;

    @Mock
    private CarRepository carRepository;

    @Mock
    private SparePartMapper sparePartMapper;

    @InjectMocks
    private SparePartManagerServiceImpl sparePartManagerService;

    private SparePart sparePart;
    private SparePartResponse sparePartResponse;

    @BeforeEach
    void setUp() {
        sparePart = new SparePart(
                "part123",
                SpareType.BRAKE_PADS,
                "Brake Pads",
                "High-quality brake pads",
                Price.of(5000, "RUB"),
                Set.of()
        );
        sparePartResponse = new SparePartResponse();
        sparePartResponse.setId("part123");
        sparePartResponse.setName("Brake Pads");
    }

    @Test
    @DisplayName("Should get low stock parts")
    void shouldGetLowStockParts() {
        when(sparePartRepository.findLowStock(5)).thenReturn(List.of(sparePart));
        when(sparePartRepository.getStockQuantity("part123")).thenReturn(3);
        when(sparePartMapper.toResponse(any(SparePart.class), anyInt(), isNull(), isNull())).thenReturn(sparePartResponse);

        List<SparePartResponse> result = sparePartManagerService.getLowStockParts(5);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(sparePartRepository, times(1)).findLowStock(5);
    }

    @Test
    @DisplayName("Should return empty list when no low stock parts")
    void shouldReturnEmptyListWhenNoLowStockParts() {
        when(sparePartRepository.findLowStock(5)).thenReturn(List.of());

        List<SparePartResponse> result = sparePartManagerService.getLowStockParts(5);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(sparePartRepository, times(1)).findLowStock(5);
    }

    @Test
    @DisplayName("Should handle custom threshold for low stock")
    void shouldHandleCustomThresholdForLowStock() {
        when(sparePartRepository.findLowStock(10)).thenReturn(List.of(sparePart));
        when(sparePartRepository.getStockQuantity("part123")).thenReturn(5);
        when(sparePartMapper.toResponse(any(SparePart.class), anyInt(), isNull(), isNull())).thenReturn(sparePartResponse);

        List<SparePartResponse> result = sparePartManagerService.getLowStockParts(10);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(sparePartRepository, times(1)).findLowStock(10);
    }

    @Test
    @DisplayName("Should get out of stock parts")
    void shouldGetOutOfStockParts() {
        when(sparePartRepository.findOutOfStock()).thenReturn(List.of(sparePart));
        when(sparePartRepository.getStockQuantity("part123")).thenReturn(0);
        when(sparePartMapper.toResponse(any(SparePart.class), anyInt(), isNull(), isNull())).thenReturn(sparePartResponse);

        List<SparePartResponse> result = sparePartManagerService.getOutOfStockParts();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(sparePartRepository, times(1)).findOutOfStock();
    }

    @Test
    @DisplayName("Should return empty list when no out of stock parts")
    void shouldReturnEmptyListWhenNoOutOfStockParts() {
        when(sparePartRepository.findOutOfStock()).thenReturn(List.of());

        List<SparePartResponse> result = sparePartManagerService.getOutOfStockParts();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(sparePartRepository, times(1)).findOutOfStock();
    }

    @Test
    @DisplayName("Should handle multiple out of stock parts")
    void shouldHandleMultipleOutOfStockParts() {
        SparePart sparePart2 = new SparePart(
                "part456",
                SpareType.OIL_FILTER,
                "Oil Filter",
                "Premium oil filter",
                Price.of(1000, "RUB"),
                Set.of()
        );

        when(sparePartRepository.findOutOfStock()).thenReturn(List.of(sparePart, sparePart2));
        when(sparePartRepository.getStockQuantity("part123")).thenReturn(0);
        when(sparePartRepository.getStockQuantity("part456")).thenReturn(0);
        when(sparePartMapper.toResponse(any(SparePart.class), anyInt(), isNull(), isNull())).thenReturn(sparePartResponse);

        List<SparePartResponse> result = sparePartManagerService.getOutOfStockParts();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(sparePartRepository, times(1)).findOutOfStock();
    }

    @Test
    @DisplayName("Should throw exception when restock quantity is zero")
    void shouldThrowExceptionWhenRestockQuantityIsZero() {
        when(sparePartRepository.findById("part123")).thenReturn(Optional.of(sparePart));

        assertThrows(DomainValidationException.class, () -> {
            sparePartManagerService.requestRestock("part123", 0);
        });
    }

    @Test
    @DisplayName("Should throw exception when restock quantity is negative")
    void shouldThrowExceptionWhenRestockQuantityIsNegative() {
        when(sparePartRepository.findById("part123")).thenReturn(Optional.of(sparePart));

        assertThrows(DomainValidationException.class, () -> {
            sparePartManagerService.requestRestock("part123", -5);
        });
    }

    @Test
    @DisplayName("Should throw exception when spare part not found for restock")
    void shouldThrowExceptionWhenSparePartNotFoundForRestock() {
        when(sparePartRepository.findById("part999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            sparePartManagerService.requestRestock("part999", 10);
        });

        verify(sparePartRepository, times(1)).findById("part999");
    }

    @Test
    @DisplayName("Should request restock successfully")
    void shouldRequestRestockSuccessfully() {
        when(sparePartRepository.findById("part123")).thenReturn(Optional.of(sparePart));
        when(sparePartRepository.getStockQuantity("part123")).thenReturn(5);

        sparePartManagerService.requestRestock("part123", 10);

        verify(sparePartRepository, times(1)).findById("part123");
        verify(sparePartRepository, times(1)).getStockQuantity("part123");
    }
}
