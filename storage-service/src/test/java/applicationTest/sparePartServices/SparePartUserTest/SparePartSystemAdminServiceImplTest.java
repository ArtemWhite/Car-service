package applicationTest.sparePartServices.SparePartUserTest;

import application.dtos.request.spareRequest.CreateSparePartRequest;
import application.dtos.request.spareRequest.UpdateSparePartRequest;
import application.dtos.response.spareResponse.SparePartResponse;
import application.mapper.SparePartMapper;
import application.services.sparePartService.systemAdmin.SparePartSystemAdminServiceImpl;
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

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith({MockitoExtension.class, WithMockSecurityExtension.class})
@DisplayName("SparePartSystemAdminService Tests")
class SparePartSystemAdminServiceImplTest {

    @Mock
    private SparePartRepository sparePartRepository;

    @Mock
    private CarRepository carRepository;

    @Mock
    private SparePartMapper sparePartMapper;

    @InjectMocks
    private SparePartSystemAdminServiceImpl sparePartAdminService;

    private SparePart sparePart;
    private CreateSparePartRequest createRequest;
    private UpdateSparePartRequest updateRequest;
    private SparePartResponse sparePartResponse;

    @BeforeEach
    void setUp() {
        sparePart = new SparePart(
                "part123",
                SpareType.BRAKE_PADS,
                "Brake Pads",
                "High-quality brake pads",
                Price.of(5000, "RUB"),
                new HashSet<>()
        );
        sparePartResponse = new SparePartResponse();
        sparePartResponse.setId("part123");
        sparePartResponse.setName("Brake Pads");

        createRequest = new CreateSparePartRequest();
        createRequest.setSpareType("BRAKE_PADS");
        createRequest.setName("Brake Pads Premium");
        createRequest.setPrice(5000.0);
        createRequest.setQuantity(10);
        createRequest.setSectionId("A-01");
        createRequest.setLocation("shelf-1");

        updateRequest = new UpdateSparePartRequest();
        updateRequest.setName("Updated Name");
        updateRequest.setPrice(6000.0);
    }

    @Test
    @DisplayName("Should create spare part successfully")
    void shouldCreateSparePartSuccessfully() {
        when(sparePartMapper.toDomain(any(), any())).thenReturn(sparePart);
        when(sparePartRepository.save(any(SparePart.class))).thenReturn(sparePart);
        when(sparePartMapper.toResponse(any(SparePart.class), anyInt(), any(), any())).thenReturn(sparePartResponse);

        SparePartResponse result = sparePartAdminService.createSparePart(createRequest);

        assertNotNull(result);
        verify(sparePartRepository, times(1)).save(sparePart);
    }

    @Test
    @DisplayName("Should update spare part successfully")
    void shouldUpdateSparePartSuccessfully() {
        when(sparePartRepository.findById("part123")).thenReturn(Optional.of(sparePart));
        when(sparePartRepository.save(any(SparePart.class))).thenReturn(sparePart);
        when(sparePartMapper.toResponse(any(SparePart.class), anyInt(), any(), any())).thenReturn(sparePartResponse);

        SparePartResponse result = sparePartAdminService.updateSparePart("part123", updateRequest);

        assertNotNull(result);
        verify(sparePartRepository, times(1)).save(sparePart);
    }

    @Test
    @DisplayName("Should throw exception when spare part not found for update")
    void shouldThrowExceptionWhenSparePartNotFoundForUpdate() {
        when(sparePartRepository.findById("part999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            sparePartAdminService.updateSparePart("part999", updateRequest);
        });
    }

    @Test
    @DisplayName("Should delete spare part successfully")
    void shouldDeleteSparePartSuccessfully() {
        when(sparePartRepository.findById("part123")).thenReturn(Optional.of(sparePart));
        doNothing().when(sparePartRepository).delete("part123");

        sparePartAdminService.deleteSparePart("part123", "Too old");

        verify(sparePartRepository, times(1)).delete("part123");
    }

    @Test
    @DisplayName("Should throw exception when spare part not found for delete")
    void shouldThrowExceptionWhenSparePartNotFoundForDelete() {
        when(sparePartRepository.findById("part999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            sparePartAdminService.deleteSparePart("part999", "Reason");
        });
    }

    @Test
    @DisplayName("Should add compatible model successfully")
    void shouldAddCompatibleModelSuccessfully() {
        CarModel model = new CarModel("model1", "320i", CarBrand.BMW, "G20");
        when(sparePartRepository.findById("part123")).thenReturn(Optional.of(sparePart));
        when(carRepository.findModelById("model1")).thenReturn(Optional.of(model));
        when(sparePartRepository.save(any(SparePart.class))).thenReturn(sparePart);

        sparePartAdminService.addCompatibleModel("part123", "model1");

        verify(sparePartRepository, times(1)).save(sparePart);
        assertTrue(sparePart.isCompatibleWith(model));
    }

    @Test
    @DisplayName("Should throw exception when model not found for adding")
    void shouldThrowExceptionWhenModelNotFoundForAdding() {
        when(sparePartRepository.findById("part123")).thenReturn(Optional.of(sparePart));
        when(carRepository.findModelById("model999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            sparePartAdminService.addCompatibleModel("part123", "model999");
        });
    }

    @Test
    @DisplayName("Should throw exception when spare part not found for adding")
    void shouldThrowExceptionWhenSparePartNotFoundForAdding() {
        when(sparePartRepository.findById("part999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            sparePartAdminService.addCompatibleModel("part999", "model1");
        });
    }

    @Test
    @DisplayName("Should remove compatible model successfully")
    void shouldRemoveCompatibleModelSuccessfully() {
        CarModel model = new CarModel("model1", "320i", CarBrand.BMW, "G20");
        Set<CarModel> models = new HashSet<>();
        models.add(model);

        SparePart sparePartWithModel = new SparePart(
                "part123",
                SpareType.BRAKE_PADS,
                "Brake Pads",
                "Desc",
                Price.of(5000, "RUB"),
                models
        );

        when(sparePartRepository.findById("part123")).thenReturn(Optional.of(sparePartWithModel));
        when(sparePartRepository.save(any(SparePart.class))).thenReturn(sparePartWithModel);

        assertTrue(sparePartWithModel.isCompatibleWith(model));

        sparePartAdminService.removeCompatibleModel("part123", "model1");

        verify(sparePartRepository, times(1)).save(sparePartWithModel);
        assertFalse(sparePartWithModel.isCompatibleWith(model));
    }

    @Test
    @DisplayName("Should throw exception when spare part not found for removal")
    void shouldThrowExceptionWhenSparePartNotFoundForRemoval() {
        when(sparePartRepository.findById("part999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            sparePartAdminService.removeCompatibleModel("part999", "model1");
        });
    }
}
