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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CarPriceDiscountTest
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
    @DisplayName("Should apply discount to price")
    void shouldApplyDiscount() {
        Price originalPrice = Price.of(1000000, "RUB");

        Price discountedPrice = originalPrice.applyDiscount(10);

        assertEquals(900000, discountedPrice.getAmount().doubleValue(), 0.0001);
        assertTrue(discountedPrice.isDiscounted());
    }

    @Test
    @DisplayName("Should apply multiple discounts sequentially")
    void shouldApplyMultipleDiscounts() {
        Price price = Price.of(1000000, "RUB");

        price = price.applyDiscount(10);
        price = price.applyDiscount(5);

        assertEquals(855000, price.getAmount().doubleValue(), 0.0001);
        assertTrue(price.isDiscounted());
    }

    @Test
    @DisplayName("Should apply zero discount without changing price")
    void shouldApplyZeroDiscount() {
        Price originalPrice = Price.of(1000000, "RUB");

        Price discountedPrice = originalPrice.applyDiscount(0);

        assertEquals(0, originalPrice.getAmount().compareTo(discountedPrice.getAmount()));
        assertTrue(discountedPrice.isDiscounted());
    }

    @Test
    @DisplayName("Should apply discount to car price")
    void shouldApplyDiscountToCarPrice() {
        assertEquals(3500000, car.getPrice().getAmount().doubleValue(), 0.0001);

        car.setPrice(car.getPrice().applyDiscount(15));

        assertEquals(2975000, car.getPrice().getAmount().doubleValue(), 0.0001);
        assertTrue(car.getPrice().isDiscounted());
    }

    @Test
    @DisplayName("Should apply discount after configuration")
    void shouldApplyDiscountAfterConfiguration() {
        car.applyConfiguration(baseConfig);
        assertEquals(3500000, car.getPrice().getAmount().doubleValue(), 0.0001);

        car.setPrice(car.getPrice().applyDiscount(10));

        assertEquals(3150000, car.getPrice().getAmount().doubleValue(), 0.0001);
        assertTrue(car.getPrice().isDiscounted());
    }

    @Test
    @DisplayName("Should calculate configuration price with discount")
    void shouldCalculateConfigurationPriceWithDiscount() {
        Map<ComponentType, Component> selectedComponents = Map.of(
                ComponentType.WHEELS, sportWheels,
                ComponentType.INTERIOR, leatherInterior
        );

        Price configPrice = baseConfig.calculateTotalPrice(selectedComponents);
        Price discountedPrice = configPrice.applyDiscount(10);

        assertEquals(3705000, configPrice.getAmount().doubleValue(), 0.0001);
        assertEquals(3334500, discountedPrice.getAmount().doubleValue(), 0.0001);
    }

}
