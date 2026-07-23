package domainTest.sparePart.creationSparePart;

import domain.models.car.CarModel;
import domain.models.car.Price;
import domain.models.car.types.CarBrand;
import domain.models.sparePart.SparePart;
import domain.models.sparePart.SpareType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.Assert.*;

class SparePartValidationTest {

    private CarModel createBmwModel() {
        return new CarModel(null,"320i", CarBrand.BMW, "G20");
    }

    @Test
    @DisplayName("Should throw exception when type is null")
    void shouldThrowExceptionWhenTypeIsNull() {
        assertThrows(NullPointerException.class, () -> {
            new SparePart(
                    "part1",
                    null,
                    "Brake Pads",
                    "Description",
                    Price.of(5000, "RUB"),
                    Set.of(createBmwModel())
            );
        });
    }

    @Test
    @DisplayName("Should throw exception when name is null")
    void shouldThrowExceptionWhenNameIsNull() {
        assertThrows(NullPointerException.class, () -> {
            new SparePart(
                    "part1",
                    SpareType.BRAKE_PADS,
                    null,
                    "Description",
                    Price.of(5000, "RUB"),
                    Set.of(createBmwModel())
            );
        });
    }

    @Test
    @DisplayName("Should throw exception when price is null")
    void shouldThrowExceptionWhenPriceIsNull() {
        assertThrows(NullPointerException.class, () -> {
            new SparePart(
                    "part1",
                    SpareType.BRAKE_PADS,
                    "Brake Pads",
                    "Description",
                    null,
                    Set.of(createBmwModel())
            );
        });
    }

    @Test
    @DisplayName("Should accept null description")
    void shouldAcceptNullDescription() {
        SparePart part = new SparePart(
                "part1",
                SpareType.BRAKE_PADS,
                "Brake Pads",
                null,
                Price.of(5000, "RUB"),
                Set.of(createBmwModel())
        );

        assertNull(part.getDescription());
    }

    @Test
    @DisplayName("Should accept id as null")
    void shouldAcceptNullId() {
        SparePart part = new SparePart(
                null,
                SpareType.BRAKE_PADS,
                "Brake Pads",
                "Description",
                Price.of(5000, "RUB"),
                Set.of(createBmwModel())
        );

        assertNull(part.getId());
    }
}
