package domainTest.sparePart.creationSparePart;

import domain.exception.DomainValidationException;
import domain.models.car.CarModel;
import domain.models.car.Price;
import domain.models.car.types.CarBrand;
import domain.models.sparePart.SparePart;
import domain.models.sparePart.SpareType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.Assert.*;

class SparePartCompatibilityTest {

    private CarModel bmwModel;
    private CarModel audiModel;
    private CarModel mercedesModel;
    private SparePart bmwPart;
    private SparePart universalPart;
    private SparePart emptyPart;

    @BeforeEach
    void setUp() {
        bmwModel = new CarModel("aaa","320i", CarBrand.BMW, "G20");
        audiModel = new CarModel("ffff","A4", CarBrand.AUDI, "B9");
        mercedesModel = new CarModel("frrrg","C200", CarBrand.MERCEDES, "W206");

        bmwPart = new SparePart(
                "part1",
                SpareType.BRAKE_PADS,
                "BMW Brake Pads",
                "Only for BMW",
                Price.of(5000, "RUB"),
                Set.of(bmwModel)
        );

        universalPart = new SparePart(
                "part2",
                SpareType.OIL_FILTER,
                "Universal Oil Filter",
                "Works with multiple models",
                Price.of(800, "RUB"),
                Set.of(bmwModel, audiModel, mercedesModel)
        );

        emptyPart = new SparePart(
                "part3",
                SpareType.BATTERY,
                "Generic Battery",
                "No compatibility info",
                Price.of(6000, "RUB"),
                Set.of()
        );
    }

    @Test
    @DisplayName("Should return true when spare part is compatible with model")
    void shouldReturnTrueWhenCompatible() {
        assertTrue(bmwPart.isCompatibleWith(bmwModel));
        assertTrue(universalPart.isCompatibleWith(bmwModel));
        assertTrue(universalPart.isCompatibleWith(audiModel));
        assertTrue(universalPart.isCompatibleWith(mercedesModel));
    }

    @Test
    @DisplayName("Should return false when spare part is not compatible with model")
    void shouldReturnFalseWhenNotCompatible() {
        assertFalse(bmwPart.isCompatibleWith(audiModel));
        assertFalse(bmwPart.isCompatibleWith(mercedesModel));
        assertFalse(emptyPart.isCompatibleWith(bmwModel));
        assertFalse(emptyPart.isCompatibleWith(audiModel));
    }

    @Test
    @DisplayName("Should handle null model in compatibility check")
    void shouldHandleNullModel() {
        assertThrows(DomainValidationException.class, () -> bmwPart.isCompatibleWith(null));
    }

    @Test
    @DisplayName("Should return false when compatible set is empty")
    void shouldReturnFalseWhenEmptySet() {
        assertFalse(emptyPart.isCompatibleWith(bmwModel));
        assertFalse(emptyPart.isCompatibleWith(audiModel));
    }

    @Test
    @DisplayName("Should maintain compatibility after multiple checks")
    void shouldMaintainCompatibilityAfterMultipleChecks() {
        assertTrue(bmwPart.isCompatibleWith(bmwModel));
        assertTrue(bmwPart.isCompatibleWith(bmwModel));
        assertFalse(bmwPart.isCompatibleWith(audiModel));
        assertFalse(bmwPart.isCompatibleWith(audiModel));
    }

    @Test
    @DisplayName("Should correctly identify compatible models in set")
    void shouldCorrectlyIdentifyCompatibleModels() {
        Set<CarModel> compatibles = universalPart.getCompatibles();

        assertTrue(compatibles.contains(bmwModel));
        assertTrue(compatibles.contains(audiModel));
        assertTrue(compatibles.contains(mercedesModel));
        assertEquals(3, compatibles.size());
    }
}
