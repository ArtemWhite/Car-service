package applicationTest.sparePartServices.SparePartUserTest;

import application.dtos.request.spareRequest.CreateSparePartRequest;
import application.dtos.request.spareRequest.UpdateSparePartRequest;
import application.dtos.response.spareResponse.SparePartResponse;
import application.mapper.SparePartMapper;
import application.services.sparePartService.systemAdmin.SparePartSystemAdminServiceImpl;
import domain.exception.DomainValidationException;
import domain.exception.EntityNotFoundException;
import domain.models.car.CarModel;
import domain.models.car.Price;
import domain.models.car.types.CarBrand;
import domain.models.sparePart.SparePart;
import domain.models.sparePart.SpareType;
import domain.models.users.User;
import domain.models.users.systemAdmin.SystemAdmin;
import domain.models.users.systemAdmin.AdminLevel;
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

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SparePartSystemAdminService Tests")
class SparePartSystemAdminServiceImplTest {

    @Mock
    private SparePartRepository sparePartRepository;

    @Mock
    private CarRepository carRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SparePartMapper sparePartMapper;

    @InjectMocks
    private SparePartSystemAdminServiceImpl sparePartAdminService;

    private SystemAdmin admin;
    private SparePart sparePart;
    private CreateSparePartRequest createRequest;
    private UpdateSparePartRequest updateRequest;
    private SparePartResponse sparePartResponse;

    @BeforeEach
    void setUp() {
        admin = new SystemAdmin("Admin", "User", null, "admin@email.com", "+123", "pass", "emp1", AdminLevel.ADMIN);
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
        when(userRepository.findById("admin123")).thenReturn(Optional.of(admin));
        when(sparePartMapper.toDomain(any(), any())).thenReturn(sparePart);
        when(sparePartRepository.save(any(SparePart.class))).thenReturn(sparePart);
        when(sparePartMapper.toResponse(any(SparePart.class), anyInt(), any(), any())).thenReturn(sparePartResponse);

        SparePartResponse result = sparePartAdminService.createSparePart(createRequest);

        assertNotNull(result);
        verify(sparePartRepository, times(1)).save(sparePart);
        verify(userRepository, times(1)).save(admin);
    }

    @Test
    @DisplayName("Should log action when creating spare part")
    void shouldLogActionWhenCreatingSparePart() {
        when(userRepository.findById("admin123")).thenReturn(Optional.of(admin));
        when(sparePartMapper.toDomain(any(), any())).thenReturn(sparePart);
        when(sparePartRepository.save(any(SparePart.class))).thenReturn(sparePart);
        when(sparePartMapper.toResponse(any(SparePart.class), anyInt(), any(), any())).thenReturn(sparePartResponse);

        sparePartAdminService.createSparePart(createRequest);

        assertFalse(admin.getAuditLog().isEmpty());
        assertEquals("CREATE_SPARE_PART", admin.getAuditLog().get(0).getAction());
        assertTrue(admin.getAuditLog().get(0).getDetails().contains("Brake Pads"));
    }

    @Test
    @DisplayName("Should throw exception when non-admin tries to create")
    void shouldThrowExceptionWhenNonAdminTriesToCreate() {
        User regularUser = mock(User.class);
        when(userRepository.findById("user123")).thenReturn(Optional.of(regularUser));

        assertThrows(DomainValidationException.class, () -> {
            sparePartAdminService.createSparePart(createRequest);
        });
    }

    @Test
    @DisplayName("Should throw exception when admin not found for create")
    void shouldThrowExceptionWhenAdminNotFoundForCreate() {
        when(userRepository.findById("admin999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            sparePartAdminService.createSparePart(createRequest);
        });
    }

    @Test
    @DisplayName("Should update spare part successfully")
    void shouldUpdateSparePartSuccessfully() {
        when(userRepository.findById("admin123")).thenReturn(Optional.of(admin));
        when(sparePartRepository.findById("part123")).thenReturn(Optional.of(sparePart));
        when(sparePartRepository.save(any(SparePart.class))).thenReturn(sparePart);
        when(sparePartMapper.toResponse(any(SparePart.class), anyInt(), any(), any())).thenReturn(sparePartResponse);

        SparePartResponse result = sparePartAdminService.updateSparePart("part123", updateRequest);

        assertNotNull(result);
        verify(sparePartRepository, times(1)).save(sparePart);
        verify(userRepository, times(1)).save(admin);
    }

    @Test
    @DisplayName("Should log action when updating spare part")
    void shouldLogActionWhenUpdatingSparePart() {
        when(userRepository.findById("admin123")).thenReturn(Optional.of(admin));
        when(sparePartRepository.findById("part123")).thenReturn(Optional.of(sparePart));
        when(sparePartRepository.save(any(SparePart.class))).thenReturn(sparePart);
        when(sparePartMapper.toResponse(any(SparePart.class), anyInt(), any(), any())).thenReturn(sparePartResponse);

        sparePartAdminService.updateSparePart("part123", updateRequest);

        assertFalse(admin.getAuditLog().isEmpty());
        assertEquals("UPDATE_SPARE_PART", admin.getAuditLog().get(0).getAction());
    }

    @Test
    @DisplayName("Should throw exception when spare part not found for update")
    void shouldThrowExceptionWhenSparePartNotFoundForUpdate() {
        when(userRepository.findById("admin123")).thenReturn(Optional.of(admin));
        when(sparePartRepository.findById("part999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            sparePartAdminService.updateSparePart("part999", updateRequest);
        });
    }

    @Test
    @DisplayName("Should throw exception when admin not found for update")
    void shouldThrowExceptionWhenAdminNotFoundForUpdate() {
        when(userRepository.findById("admin999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            sparePartAdminService.updateSparePart("part123", updateRequest);
        });
    }

    @Test
    @DisplayName("Should delete spare part successfully")
    void shouldDeleteSparePartSuccessfully() {
        when(userRepository.findById("admin123")).thenReturn(Optional.of(admin));
        when(sparePartRepository.findById("part123")).thenReturn(Optional.of(sparePart));
        doNothing().when(sparePartRepository).delete("part123");

        sparePartAdminService.deleteSparePart("part123", "Too old");

        verify(sparePartRepository, times(1)).delete("part123");
        verify(userRepository, times(1)).save(admin);
    }

    @Test
    @DisplayName("Should log action when deleting spare part")
    void shouldLogActionWhenDeletingSparePart() {
        when(userRepository.findById("admin123")).thenReturn(Optional.of(admin));
        when(sparePartRepository.findById("part123")).thenReturn(Optional.of(sparePart));
        doNothing().when(sparePartRepository).delete("part123");

        sparePartAdminService.deleteSparePart("part123", "Too old");

        assertFalse(admin.getAuditLog().isEmpty());
        assertEquals("DELETE_SPARE_PART", admin.getAuditLog().get(0).getAction());
        assertTrue(admin.getAuditLog().get(0).getDetails().contains("Too old"));
    }

    @Test
    @DisplayName("Should throw exception when spare part not found for delete")
    void shouldThrowExceptionWhenSparePartNotFoundForDelete() {
        when(userRepository.findById("admin123")).thenReturn(Optional.of(admin));
        when(sparePartRepository.findById("part999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            sparePartAdminService.deleteSparePart("part999", "Reason");
        });
    }

    @Test
    @DisplayName("Should add compatible model successfully")
    void shouldAddCompatibleModelSuccessfully() {
        CarModel model = new CarModel("model1", "320i", CarBrand.BMW, "G20");
        when(userRepository.findById("admin123")).thenReturn(Optional.of(admin));
        when(sparePartRepository.findById("part123")).thenReturn(Optional.of(sparePart));
        when(carRepository.findModelById("model1")).thenReturn(Optional.of(model));
        when(sparePartRepository.save(any(SparePart.class))).thenReturn(sparePart);

        sparePartAdminService.addCompatibleModel("part123", "model1");

        verify(sparePartRepository, times(1)).save(sparePart);
        assertTrue(sparePart.isCompatibleWith(model));
    }

    @Test
    @DisplayName("Should log action when adding compatible model")
    void shouldLogActionWhenAddingCompatibleModel() {
        CarModel model = new CarModel("model1", "320i", CarBrand.BMW, "G20");
        when(userRepository.findById("admin123")).thenReturn(Optional.of(admin));
        when(sparePartRepository.findById("part123")).thenReturn(Optional.of(sparePart));
        when(carRepository.findModelById("model1")).thenReturn(Optional.of(model));
        when(sparePartRepository.save(any(SparePart.class))).thenReturn(sparePart);

        sparePartAdminService.addCompatibleModel("part123", "model1");

        assertFalse(admin.getAuditLog().isEmpty());
        assertEquals("ADD_COMPATIBLE_MODEL", admin.getAuditLog().get(0).getAction());
    }

    @Test
    @DisplayName("Should throw exception when model not found for adding")
    void shouldThrowExceptionWhenModelNotFoundForAdding() {
        when(userRepository.findById("admin123")).thenReturn(Optional.of(admin));
        when(sparePartRepository.findById("part123")).thenReturn(Optional.of(sparePart));
        when(carRepository.findModelById("model999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            sparePartAdminService.addCompatibleModel("part123", "model999");
        });
    }

    @Test
    @DisplayName("Should throw exception when spare part not found for adding")
    void shouldThrowExceptionWhenSparePartNotFoundForAdding() {
        CarModel model = new CarModel("model1", "320i", CarBrand.BMW, "G20");
        when(userRepository.findById("admin123")).thenReturn(Optional.of(admin));
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

        when(userRepository.findById("admin123")).thenReturn(Optional.of(admin));
        when(sparePartRepository.findById("part123")).thenReturn(Optional.of(sparePartWithModel));
        when(sparePartRepository.save(any(SparePart.class))).thenReturn(sparePartWithModel);

        assertTrue(sparePartWithModel.isCompatibleWith(model));

        sparePartAdminService.removeCompatibleModel("part123", "model1");

        verify(sparePartRepository, times(1)).save(sparePartWithModel);
        assertFalse(sparePartWithModel.isCompatibleWith(model));
    }

    @Test
    @DisplayName("Should log action when removing compatible model")
    void shouldLogActionWhenRemovingCompatibleModel() {
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

        when(userRepository.findById("admin123")).thenReturn(Optional.of(admin));
        when(sparePartRepository.findById("part123")).thenReturn(Optional.of(sparePartWithModel));
        when(sparePartRepository.save(any(SparePart.class))).thenReturn(sparePartWithModel);

        sparePartAdminService.removeCompatibleModel("part123", "model1");

        assertFalse(admin.getAuditLog().isEmpty());
        assertEquals("REMOVE_COMPATIBLE_MODEL", admin.getAuditLog().get(0).getAction());
    }

    @Test
    @DisplayName("Should throw exception when spare part not found for removal")
    void shouldThrowExceptionWhenSparePartNotFoundForRemoval() {
        when(userRepository.findById("admin123")).thenReturn(Optional.of(admin));
        when(sparePartRepository.findById("part999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            sparePartAdminService.removeCompatibleModel("part999", "model1");
        });
    }
}