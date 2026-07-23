package domainTest.car.price;

import domainTest.car.helpers.CarHelpers;
import domain.models.car.Car;
import domain.models.car.CarConfiguration;
import domain.models.car.CarModel;
import domain.models.car.Price;
import domain.models.car.componentModels.Component;
import domain.models.car.componentModels.ComponentType;
import domain.models.car.types.CarBrand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class CarPriceCombinationTest
{
    private Car car;
    private CarModel bmwModel;
    private Component baseWheels;
    private Component sportWheels;
    private Component baseInterior;
    private Component leatherInterior;
    private CarConfiguration baseConfig;

    @BeforeEach
    void setUp() {
        bmwModel = new CarModel("fff","320i", CarBrand.BMW, "G20");

        baseWheels = new Component(
                "wheels1",
                ComponentType.WHEELS,
                "17'' Standard",
                "Standard wheels",
                Price.of(0, "RUB"),
                Set.of(bmwModel)
        );

        sportWheels = new Component(
                "wheels2",
                ComponentType.WHEELS,
                "19'' M-Sport",
                "Sport wheels",
                Price.of(95000, "RUB"),
                Set.of(bmwModel)
        );

        baseInterior = new Component(
                "int1",
                ComponentType.INTERIOR,
                "Standard Interior",
                "Fabric interior",
                Price.of(0, "RUB"),
                Set.of(bmwModel)
        );

        leatherInterior = new Component(
                "int2",
                ComponentType.INTERIOR,
                "Leather Interior",
                "Premium leather",
                Price.of(110000, "RUB"),
                Set.of(bmwModel)
        );

        baseConfig = new CarConfiguration(
                "config1",
                "Base",
                bmwModel,
                Map.of(
                        ComponentType.WHEELS, baseWheels,
                        ComponentType.INTERIOR, baseInterior
                ),
                Price.of(3500000, "RUB")
        );

        car = CarHelpers.createValidCar();
    }

    @Test
    @DisplayName("Should handle full price lifecycle")
    void shouldHandleFullPriceLifecycle() {
        assertEquals(3500000, car.getPrice().getAmount().doubleValue(), 0.0001);

        car.applyConfiguration(baseConfig);
        assertEquals(3500000, car.getPrice().getAmount().doubleValue(), 0.0001);

        car.setPrice(car.getPrice().applyDiscount(10));
        assertEquals(3150000, car.getPrice().getAmount().doubleValue(), 0.0001);

        car.setPrice(Price.of(4000000, "RUB"));
        assertEquals(4000000, car.getPrice().getAmount().doubleValue(), 0.0001);
        assertFalse(car.getPrice().isDiscounted());
    }

    @Test
    @DisplayName("Should preserve discount flag after manual price change")
    void shouldPreserveDiscountFlagAfterManualChange() {
        car.setPrice(car.getPrice().applyDiscount(10));
        assertTrue(car.getPrice().isDiscounted());

        car.setPrice(Price.of(4000000, "RUB"));

        assertFalse(car.getPrice().isDiscounted());
    }
}
