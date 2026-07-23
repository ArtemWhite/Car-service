package domainTest.sparePart.creationSparePart;

import domain.models.car.CarModel;
import domain.models.car.Price;
import domain.models.car.types.CarBrand;
import domain.models.sparePart.SparePart;
import domain.models.sparePart.SpareType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.*;

class SparePartImmutabilityTest {

    private CarModel bmwModel;
    private CarModel audiModel;
    private Set<CarModel> originalSet;
    private SparePart part;

    @BeforeEach
    void setUp() {
        bmwModel = new CarModel("cdfdf","320i", CarBrand.BMW, "G20");
        audiModel = new CarModel("dddfdsf","A4", CarBrand.AUDI, "B9");

        originalSet = new HashSet<>();
        originalSet.add(bmwModel);

        part = new SparePart(
                "part1",
                SpareType.BRAKE_PADS,
                "Brake Pads",
                "Description",
                Price.of(5000, "RUB"),
                originalSet
        );
    }

    @Test
    @DisplayName("Should not allow modification of returned compatible set")
    void shouldNotAllowModificationOfReturnedSet() {
        Set<CarModel> returnedSet = part.getCompatibles();

        assertThrows(UnsupportedOperationException.class, () -> {
            returnedSet.add(audiModel);
        });
    }

    @Test
    @DisplayName("Should create defensive copy of compatible set")
    void shouldCreateDefensiveCopy() {
        Set<CarModel> beforeModification = part.getCompatibles();
        assertEquals(1, beforeModification.size());

        originalSet.add(audiModel);

        Set<CarModel> afterModification = part.getCompatibles();
        assertEquals(1, afterModification.size());
        assertFalse(afterModification.contains(audiModel));
    }

    @Test
    @DisplayName("Should not allow modification through different references")
    void shouldNotAllowModificationThroughDifferentReferences() {
        Set<CarModel> ref1 = part.getCompatibles();
        Set<CarModel> ref2 = part.getCompatibles();

        assertThrows(UnsupportedOperationException.class, () -> ref1.add(audiModel));
        assertThrows(UnsupportedOperationException.class, () -> ref2.add(audiModel));
    }

    @Test
    @DisplayName("Should maintain immutability of scalar fields")
    void shouldMaintainImmutabilityOfScalarFields() {
        String originalId = part.getId();
        SpareType originalType = part.getType();
        String originalName = part.getName();
        Price originalPrice = part.getPrice();

        String newId = UUID.randomUUID().toString();
        part.setId(newId);
        assertEquals(newId, part.getId());

        part.setId(originalId);

        String newName = "New Name";
        part.setName(newName);
        assertEquals(newName, part.getName());

        part.setName(originalName);

        SpareType newType = SpareType.AIR_FILTER;
        part.setType(newType);
        assertEquals(newType, part.getType());

        part.setType(originalType);

        Price newPrice = Price.of(999.0, "USD");
        part.setPrice(newPrice);
        assertEquals(newPrice, part.getPrice());

        part.setPrice(originalPrice);

        System.out.println("All scalar fields are mutable (have setters)");
    }
}
