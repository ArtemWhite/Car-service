package applicationTest.sparePartServices.SparePartUserTest;

import application.dtos.response.spareResponse.SparePartResponse;
import application.mapper.SparePartMapper;
import application.services.sparePartService.manager.SparePartManagerServiceImpl;
import domain.exception.DomainValidationException;
import domain.exception.EntityNotFoundException;
import domain.models.car.Price;
import domain.models.sparePart.SparePart;
import domain.models.sparePart.SpareType;
import domain.models.users.manager.Manager;
import domain.repository.carRepository.CarRepository;
import domain.repository.sparePartRepository.SparePartRepository;
import domain.repository.userRepository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SparePartManagerService Tests")
class SparePartManagerServiceImplTest {

    @Mock
    private SparePartRepository sparePartRepository;

    @Mock
    private CarRepository carRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SparePartMapper sparePartMapper;

    @InjectMocks
    private SparePartManagerServiceImpl sparePartManagerService;

    private Manager manager;
    private SparePart sparePart;
    private SparePartResponse sparePartResponse;

    @BeforeEach
    void setUp() {
        manager = new Manager("John", "Doe", null, "john@email.com", "+123", "pass", "emp123");
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
        when(sparePartMapper.toResponse(any(SparePart.class))).thenReturn(sparePartResponse);

        List<SparePartResponse> result = sparePartManagerService.getLowStockParts(5);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(sparePartRepository, times(1)).findLowStock(5);
        verify(sparePartMapper, times(1)).toResponse(sparePart);
    }

    @Test
    @DisplayName("Should return empty list when no low stock parts")
    void shouldReturnEmptyListWhenNoLowStockParts() {
        when(sparePartRepository.findLowStock(5)).thenReturn(List.of());

        List<SparePartResponse> result = sparePartManagerService.getLowStockParts(5);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(sparePartRepository, times(1)).findLowStock(5);
        verify(sparePartMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("Should handle custom threshold for low stock")
    void shouldHandleCustomThresholdForLowStock() {
        when(sparePartRepository.findLowStock(10)).thenReturn(List.of(sparePart));
        when(sparePartMapper.toResponse(any(SparePart.class))).thenReturn(sparePartResponse);

        List<SparePartResponse> result = sparePartManagerService.getLowStockParts(10);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(sparePartRepository, times(1)).findLowStock(10);
    }

    @Test
    @DisplayName("Should get out of stock parts")
    void shouldGetOutOfStockParts() {
        when(sparePartRepository.findOutOfStock()).thenReturn(List.of(sparePart));
        when(sparePartMapper.toResponse(any(SparePart.class))).thenReturn(sparePartResponse);

        List<SparePartResponse> result = sparePartManagerService.getOutOfStockParts();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(sparePartRepository, times(1)).findOutOfStock();
        verify(sparePartMapper, times(1)).toResponse(sparePart);
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
        when(sparePartMapper.toResponse(any(SparePart.class))).thenReturn(sparePartResponse);

        List<SparePartResponse> result = sparePartManagerService.getOutOfStockParts();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(sparePartRepository, times(1)).findOutOfStock();
        verify(sparePartMapper, times(2)).toResponse(any(SparePart.class));
    }

    @Test
    @DisplayName("Should request restock successfully")
    void shouldRequestRestockSuccessfully() {
        when(userRepository.findById("manager123")).thenReturn(Optional.of(manager));
        when(sparePartRepository.findById("part123")).thenReturn(Optional.of(sparePart));
        when(sparePartRepository.getStockQuantity("part123")).thenReturn(5);

        sparePartManagerService.requestRestock("part123", 10);

        verify(userRepository, times(1)).findById("manager123");
        verify(sparePartRepository, times(1)).findById("part123");
        verify(sparePartRepository, times(1)).getStockQuantity("part123");
        verify(userRepository, times(1)).save(manager);
    }

    @Test
    @DisplayName("Should throw exception when manager not found for restock")
    void shouldThrowExceptionWhenManagerNotFoundForRestock() {
        when(userRepository.findById("manager999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            sparePartManagerService.requestRestock("part123", 10);
        });

        verify(userRepository, times(1)).findById("manager999");
        verify(sparePartRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Should throw exception when spare part not found for restock")
    void shouldThrowExceptionWhenSparePartNotFoundForRestock() {
        when(userRepository.findById("manager123")).thenReturn(Optional.of(manager));
        when(sparePartRepository.findById("part999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            sparePartManagerService.requestRestock("part999", 10);
        });

        verify(userRepository, times(1)).findById("manager123");
        verify(sparePartRepository, times(1)).findById("part999");
    }

    @Test
    @DisplayName("Should throw exception when restock quantity is zero")
    void shouldThrowExceptionWhenRestockQuantityIsZero() {
        when(userRepository.findById("manager123")).thenReturn(Optional.of(manager));
        when(sparePartRepository.findById("part123")).thenReturn(Optional.of(sparePart));

        assertThrows(DomainValidationException.class, () -> {
            sparePartManagerService.requestRestock("part123", 0);
        });
    }

    @Test
    @DisplayName("Should throw exception when restock quantity is negative")
    void shouldThrowExceptionWhenRestockQuantityIsNegative() {
        when(userRepository.findById("manager123")).thenReturn(Optional.of(manager));
        when(sparePartRepository.findById("part123")).thenReturn(Optional.of(sparePart));

        assertThrows(DomainValidationException.class, () -> {
            sparePartManagerService.requestRestock("part123", -5);
        });
    }

    @Test
    @DisplayName("Should update manager lastActive on restock request")
    void shouldUpdateManagerLastActiveOnRestockRequest() {
        when(userRepository.findById("manager123")).thenReturn(Optional.of(manager));
        when(sparePartRepository.findById("part123")).thenReturn(Optional.of(sparePart));
        when(sparePartRepository.getStockQuantity("part123")).thenReturn(5);

        sparePartManagerService.requestRestock("part123", 10);

        verify(userRepository, times(1)).save(manager);
    }

    @Test
    @DisplayName("Should handle restock when stock quantity is zero")
    void shouldHandleRestockWhenStockQuantityIsZero() {
        when(userRepository.findById("manager123")).thenReturn(Optional.of(manager));
        when(sparePartRepository.findById("part123")).thenReturn(Optional.of(sparePart));
        when(sparePartRepository.getStockQuantity("part123")).thenReturn(0);

        sparePartManagerService.requestRestock("part123", 10);

        verify(userRepository, times(1)).save(manager);
    }
}