package domainTest.sparePart.creationSparePart;

import domain.models.car.CarModel;
import domain.models.car.Price;
import domain.models.car.types.CarBrand;
import domain.models.sparePart.SparePart;
import domain.models.sparePart.SpareType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

class SparePartCreationTest {

    private CarModel createBmwModel() {
        return new CarModel("fdfdfd","320i", CarBrand.BMW, "G20");
    }

    private CarModel createAudiModel() {
        return new CarModel("ggffdf","A4", CarBrand.RENAULT, "B9");
    }

    @Test
    @DisplayName("Should successfully create spare part with all fields")
    void shouldCreateSparePartWithAllFields() {
        String id = "part123";
        SpareType type = SpareType.BRAKE_PADS;
        String name = "Brake Pads Premium";
        String description = "High-quality brake pads";
        Price price = Price.of(5000, "RUB");
        Set<CarModel> compatibles = Set.of(createBmwModel());

        SparePart part = new SparePart(id, type, name, description, price, compatibles);

        assertNotNull(part);
        assertEquals(id, part.getId());
        assertEquals(type, part.getType());
        assertEquals(name, part.getName());
        assertEquals(description, part.getDescription());
        assertEquals(price, part.getPrice());
        assertEquals(1, part.getCompatibles().size());
        assertTrue(part.isCompatibleWith(createBmwModel()));
    }

    @Test
    @DisplayName("Should create spare part with empty compatible models list")
    void shouldCreateSparePartWithEmptyCompatibles() {
        Set<CarModel> emptyCompatibles = new HashSet<>();

        SparePart part = new SparePart(
                "part123",
                SpareType.OIL_FILTER,
                "Oil Filter",
                "Standard oil filter",
                Price.of(500, "RUB"),
                emptyCompatibles
        );

        assertNotNull(part);
        assertEquals(0, part.getCompatibles().size());
        assertFalse(part.isCompatibleWith(createBmwModel()));
    }

    @Test
    @DisplayName("Should create spare part with multiple compatible models")
    void shouldCreateSparePartWithMultipleCompatibles() {
        Set<CarModel> compatibles = Set.of(createBmwModel(), createAudiModel());

        SparePart part = new SparePart(
                "part123",
                SpareType.WHEEL,
                "Alloy Wheel 18''",
                "Sports alloy wheel",
                Price.of(15000, "RUB"),
                compatibles
        );

        assertEquals(2, part.getCompatibles().size());
        assertTrue(part.isCompatibleWith(createBmwModel()));
        assertTrue(part.isCompatibleWith(createAudiModel()));
    }

    @Test
    @DisplayName("Should create spare part with minimal fields")
    void shouldCreateSparePartWithMinimalFields() {
        SparePart part = new SparePart(
                "part123",
                SpareType.BATTERY,
                "Battery",
                "",
                Price.of(8000, "RUB"),
                new HashSet<>()
        );

        assertNotNull(part);
        assertEquals("", part.getDescription());
        assertEquals(0, part.getCompatibles().size());
    }

    @Test
    @DisplayName("Should create spare part with different spare types")
    void shouldCreateSparePartWithDifferentTypes() {
        for (SpareType type : SpareType.values()) {
            SparePart part = new SparePart(
                    "part" + type.ordinal(),
                    type,
                    "Test " + type.name(),
                    "Description",
                    Price.of(1000, "RUB"),
                    new HashSet<>()
            );

            assertEquals(type, part.getType());
            assertEquals(type.getDisplayName(), part.getType().getDisplayName());
        }
    }
}