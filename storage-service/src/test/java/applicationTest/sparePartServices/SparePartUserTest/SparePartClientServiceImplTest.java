package applicationTest.sparePartServices.SparePartUserTest;

import application.dtos.response.spareResponse.SparePartResponse;
import application.mapper.SparePartMapper;
import application.services.sparePartService.client.SparePartClientServiceImpl;
import domain.exception.EntityNotFoundException;
import domain.models.car.CarModel;
import domain.models.car.Price;
import domain.models.car.types.CarBrand;
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

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith({MockitoExtension.class, WithMockSecurityExtension.class})
@DisplayName("SparePartClientService Tests")
class SparePartClientServiceImplTest {

    @Mock
    private SparePartRepository sparePartRepository;

    @Mock
    private CarRepository carRepository;

    @Mock
    private SparePartMapper sparePartMapper;

    @InjectMocks
    private SparePartClientServiceImpl sparePartClientService;

    private CarModel bmwModel;
    private SparePart sparePart;
    private SparePartResponse sparePartResponse;

    @BeforeEach
    void setUp() {
        bmwModel = new CarModel("model1", "320i", CarBrand.BMW, "G20");
        sparePart = new SparePart(
                "part123",
                SpareType.BRAKE_PADS,
                "Brake Pads",
                "High-quality brake pads",
                Price.of(5000, "RUB"),
                Set.of(bmwModel)
        );
        sparePartResponse = new SparePartResponse();
        sparePartResponse.setId("part123");
        sparePartResponse.setName("Brake Pads");
    }

    @Test
    @DisplayName("Should find compatible spare parts successfully")
    void shouldFindCompatibleSparePartsSuccessfully() {
        when(carRepository.findModelById("model1")).thenReturn(Optional.of(bmwModel));
        when(sparePartRepository.findByCompatibleModelWithStock(eq(bmwModel), anyMap(), anyMap(), anyMap()))
                .thenReturn(List.of(sparePart));
        when(sparePartMapper.toResponse(any(SparePart.class), anyInt(), any(), any())).thenReturn(sparePartResponse);

        List<SparePartResponse> result = sparePartClientService.findCompatibleSpareParts("model1");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(sparePartRepository, times(1)).findByCompatibleModelWithStock(eq(bmwModel), anyMap(), anyMap(), anyMap());
    }

    @Test
    @DisplayName("Should return empty list when no compatible spare parts")
    void shouldReturnEmptyListWhenNoCompatibleSpareParts() {
        when(carRepository.findModelById("model1")).thenReturn(Optional.of(bmwModel));
        when(sparePartRepository.findByCompatibleModelWithStock(eq(bmwModel), anyMap(), anyMap(), anyMap()))
                .thenReturn(List.of());

        List<SparePartResponse> result = sparePartClientService.findCompatibleSpareParts("model1");

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(sparePartRepository, times(1)).findByCompatibleModelWithStock(eq(bmwModel), anyMap(), anyMap(), anyMap());
        verify(sparePartMapper, never()).toResponse(any(), anyInt(), any(), any());
    }

    @Test
    @DisplayName("Should throw exception when model not found")
    void shouldThrowExceptionWhenModelNotFound() {
        when(carRepository.findModelById("model999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            sparePartClientService.findCompatibleSpareParts("model999");
        });
        verify(carRepository, times(1)).findModelById("model999");
    }

    @Test
    @DisplayName("Should get spare part details successfully")
    void shouldGetSparePartDetailsSuccessfully() {
        when(sparePartRepository.findById("part123")).thenReturn(Optional.of(sparePart));
        when(sparePartRepository.getStockQuantity("part123")).thenReturn(10);
        when(sparePartRepository.getSectionId("part123")).thenReturn("A-01");
        when(sparePartRepository.getLocation("part123")).thenReturn("shelf-1");
        when(sparePartMapper.toResponse(any(SparePart.class), anyInt(), any(), any())).thenReturn(sparePartResponse);

        SparePartResponse result = sparePartClientService.getSparePartDetails("part123");

        assertNotNull(result);
        verify(sparePartRepository, times(1)).findById("part123");
        verify(sparePartMapper, times(1)).toResponse(any(SparePart.class), anyInt(), any(), any());
    }

    @Test
    @DisplayName("Should throw exception when spare part not found")
    void shouldThrowExceptionWhenSparePartNotFound() {
        when(sparePartRepository.findById("part999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            sparePartClientService.getSparePartDetails("part999");
        });
        verify(sparePartRepository, times(1)).findById("part999");
    }

    @Test
    @DisplayName("Should search spare parts by name")
    void shouldSearchSparePartsByName() {
        when(sparePartRepository.findByNameContaining("Brake")).thenReturn(List.of(sparePart));
        when(sparePartRepository.getStockQuantity(anyString())).thenReturn(10);
        when(sparePartMapper.toResponse(any(SparePart.class), anyInt(), any(), any())).thenReturn(sparePartResponse);

        List<SparePartResponse> result = sparePartClientService.searchSpareParts("Brake");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(sparePartRepository, times(1)).findByNameContaining("Brake");
    }

    @Test
    @DisplayName("Should search spare parts by partial name")
    void shouldSearchSparePartsByPartialName() {
        when(sparePartRepository.findByNameContaining("Pad")).thenReturn(List.of(sparePart));
        when(sparePartRepository.getStockQuantity(anyString())).thenReturn(10);
        when(sparePartMapper.toResponse(any(SparePart.class), anyInt(), any(), any())).thenReturn(sparePartResponse);

        List<SparePartResponse> result = sparePartClientService.searchSpareParts("Pad");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should return empty list when search returns no results")
    void shouldReturnEmptyListWhenSearchReturnsNoResults() {
        when(sparePartRepository.findByNameContaining("NonExistent")).thenReturn(List.of());

        List<SparePartResponse> result = sparePartClientService.searchSpareParts("NonExistent");

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(sparePartMapper, never()).toResponse(any(), anyInt(), any(), any());
    }

    @Test
    @DisplayName("Should search with empty query")
    void shouldSearchWithEmptyQuery() {
        when(sparePartRepository.findByNameContaining("")).thenReturn(List.of(sparePart));
        when(sparePartRepository.getStockQuantity(anyString())).thenReturn(10);
        when(sparePartMapper.toResponse(any(SparePart.class), anyInt(), any(), any())).thenReturn(sparePartResponse);

        List<SparePartResponse> result = sparePartClientService.searchSpareParts("");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should search with null query")
    void shouldSearchWithNullQuery() {
        when(sparePartRepository.findByNameContaining(null)).thenReturn(List.of(sparePart));
        when(sparePartRepository.getStockQuantity(anyString())).thenReturn(10);
        when(sparePartMapper.toResponse(any(SparePart.class), anyInt(), any(), any())).thenReturn(sparePartResponse);

        List<SparePartResponse> result = sparePartClientService.searchSpareParts(null);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should handle multiple compatible spare parts")
    void shouldHandleMultipleCompatibleSpareParts() {
        SparePart sparePart2 = new SparePart(
                "part456",
                SpareType.OIL_FILTER,
                "Oil Filter",
                "Premium oil filter",
                Price.of(1000, "RUB"),
                Set.of(bmwModel)
        );

        when(carRepository.findModelById("model1")).thenReturn(Optional.of(bmwModel));
        when(sparePartRepository.findByCompatibleModelWithStock(eq(bmwModel), anyMap(), anyMap(), anyMap()))
                .thenReturn(List.of(sparePart, sparePart2));
        when(sparePartMapper.toResponse(any(SparePart.class), anyInt(), any(), any())).thenReturn(sparePartResponse);

        List<SparePartResponse> result = sparePartClientService.findCompatibleSpareParts("model1");

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should handle spare part with null description")
    void shouldHandleSparePartWithNullDescription() {
        SparePart sparePartNoDesc = new SparePart(
                "part789",
                SpareType.BRAKE_PADS,
                "Brake Pads",
                null,
                Price.of(5000, "RUB"),
                Set.of(bmwModel)
        );

        when(carRepository.findModelById("model1")).thenReturn(Optional.of(bmwModel));
        when(sparePartRepository.findByCompatibleModelWithStock(eq(bmwModel), anyMap(), anyMap(), anyMap()))
                .thenReturn(List.of(sparePartNoDesc));
        when(sparePartMapper.toResponse(any(SparePart.class), anyInt(), any(), any())).thenReturn(sparePartResponse);

        List<SparePartResponse> result = sparePartClientService.findCompatibleSpareParts("model1");

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
