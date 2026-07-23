package applicationTest.sparePartServices.SparePartUserTest;

import application.dtos.request.spareRequest.UpdateStockRequest;
import application.dtos.response.spareResponse.SparePartResponse;
import application.mapper.SparePartMapper;
import application.services.sparePartService.warehouseAdmin.SparePartWarehouseAdminServiceImpl;
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

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith({MockitoExtension.class, WithMockSecurityExtension.class})
@DisplayName("SparePartWarehouseAdminService Tests")
class SparePartWarehouseAdminServiceImplTest {

    @Mock
    private SparePartRepository sparePartRepository;

    @Mock
    private CarRepository carRepository;

    @Mock
    private SparePartMapper sparePartMapper;

    @InjectMocks
    private SparePartWarehouseAdminServiceImpl sparePartWarehouseService;

    private SparePart sparePart;
    private UpdateStockRequest updateStockRequest;
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

        updateStockRequest = new UpdateStockRequest();
        updateStockRequest.setSparePartId("part123");
        updateStockRequest.setNewQuantity(20);
        updateStockRequest.setSectionId("A-01");
        updateStockRequest.setLocation("shelf-3");
        updateStockRequest.setReason("Stock adjustment");
    }

    @Test
    @DisplayName("Should update stock successfully")
    void shouldUpdateStockSuccessfully() {
        when(sparePartRepository.findById("part123")).thenReturn(Optional.of(sparePart));
        when(sparePartRepository.getStockQuantity("part123")).thenReturn(10);
        when(sparePartMapper.toResponse(any(SparePart.class), anyInt(), any(), any())).thenReturn(sparePartResponse);

        SparePartResponse result = sparePartWarehouseService.updateStock(updateStockRequest);

        assertNotNull(result);
        verify(sparePartRepository, times(1)).updateStock("part123", 20, "A-01", "shelf-3");
    }

    @Test
    @DisplayName("Should throw exception when spare part not found for update")
    void shouldThrowExceptionWhenSparePartNotFoundForUpdate() {
        when(sparePartRepository.findById(updateStockRequest.getSparePartId())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            sparePartWarehouseService.updateStock(updateStockRequest);
        });
    }

    @Test
    @DisplayName("Should receive shipment successfully")
    void shouldReceiveShipmentSuccessfully() {
        when(sparePartRepository.findById("part123")).thenReturn(Optional.of(sparePart));
        when(sparePartRepository.getStockQuantity("part123")).thenReturn(10);
        when(sparePartMapper.toResponse(any(SparePart.class), anyInt(), isNull(), isNull())).thenReturn(sparePartResponse);

        SparePartResponse result = sparePartWarehouseService.receiveShipment("part123", 5);

        assertNotNull(result);
        verify(sparePartRepository, times(1)).updateStock("part123", 15, null, null);
    }

    @Test
    @DisplayName("Should throw exception when receiving zero quantity")
    void shouldThrowExceptionWhenReceivingZeroQuantity() {
        when(sparePartRepository.findById("part123")).thenReturn(Optional.of(sparePart));

        assertThrows(DomainValidationException.class, () -> {
            sparePartWarehouseService.receiveShipment("part123", 0);
        });
    }

    @Test
    @DisplayName("Should throw exception when receiving negative shipment quantity")
    void shouldThrowExceptionWhenReceivingNegativeShipmentQuantity() {
        when(sparePartRepository.findById("part123")).thenReturn(Optional.of(sparePart));

        assertThrows(DomainValidationException.class, () -> {
            sparePartWarehouseService.receiveShipment("part123", -5);
        });
    }

    @Test
    @DisplayName("Should move to location successfully")
    void shouldMoveToLocationSuccessfully() {
        when(sparePartRepository.findById("part123")).thenReturn(Optional.of(sparePart));
        when(sparePartRepository.getStockQuantity("part123")).thenReturn(10);
        when(sparePartMapper.toResponse(any(SparePart.class), anyInt(), any(), any())).thenReturn(sparePartResponse);

        SparePartResponse result = sparePartWarehouseService.moveToLocation("part123", "B-02", "shelf-5");

        assertNotNull(result);
        verify(sparePartRepository, times(1)).updateStock("part123", 10, "B-02", "shelf-5");
    }

    @Test
    @DisplayName("Should write off successfully")
    void shouldWriteOffSuccessfully() {
        when(sparePartRepository.findById("part123")).thenReturn(Optional.of(sparePart));
        when(sparePartRepository.getStockQuantity("part123")).thenReturn(10);
        when(sparePartMapper.toResponse(any(SparePart.class), anyInt(), isNull(), isNull())).thenReturn(sparePartResponse);

        SparePartResponse result = sparePartWarehouseService.writeOff("part123", 3, "Damaged");

        assertNotNull(result);
        verify(sparePartRepository, times(1)).updateStock("part123", 7, null, null);
    }

    @Test
    @DisplayName("Should throw exception when writing off more than available")
    void shouldThrowExceptionWhenWritingOffMoreThanAvailable() {
        when(sparePartRepository.findById("part123")).thenReturn(Optional.of(sparePart));
        when(sparePartRepository.getStockQuantity("part123")).thenReturn(10);

        assertThrows(DomainValidationException.class, () -> {
            sparePartWarehouseService.writeOff("part123", 20, "Reason");
        });

        verify(sparePartRepository, never()).updateStock(any(), anyInt(), any(), any());
    }

    @Test
    @DisplayName("Should throw exception when writing off zero quantity")
    void shouldThrowExceptionWhenWritingOffZeroQuantity() {
        when(sparePartRepository.findById("part123")).thenReturn(Optional.of(sparePart));

        assertThrows(DomainValidationException.class, () -> {
            sparePartWarehouseService.writeOff("part123", 0, "Reason");
        });
    }

    @Test
    @DisplayName("Should throw exception when writing off negative quantity")
    void shouldThrowExceptionWhenWritingOffNegativeQuantity() {
        when(sparePartRepository.findById("part123")).thenReturn(Optional.of(sparePart));

        assertThrows(DomainValidationException.class, () -> {
            sparePartWarehouseService.writeOff("part123", -3, "Reason");
        });
    }

    @Test
    @DisplayName("Should throw exception when spare part not found for write off")
    void shouldThrowExceptionWhenSparePartNotFoundForWriteOff() {
        when(sparePartRepository.findById("part999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            sparePartWarehouseService.writeOff("part999", 5, "Reason");
        });
    }
}
