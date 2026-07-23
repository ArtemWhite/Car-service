package domainTest.car.configurationTest;

import domainTest.car.helpers.CarHelpers;
import domain.exception.DomainValidationException;
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

public class CarPriceRecountTest
{
    private Car car;
    private CarModel bmwModel;
    private CarModel audiModel;
    private Component baseWheels;
    private Component sportWheels;
    private Component baseInterior;
    private Component leatherInterior;
    private CarConfiguration baseConfig;
    private CarConfiguration sportConfig;

    @BeforeEach
    void setUp() {
        bmwModel = new CarModel("dfgfh","320i", CarBrand.BMW, "G20");
        audiModel = new CarModel("fff", "A4", CarBrand.RENAULT, "B9");

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
                Set.of(bmwModel, audiModel)
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

        sportConfig = new CarConfiguration(
                "config2",
                "Sport",
                bmwModel,
                Map.of(
                        ComponentType.WHEELS, sportWheels,
                        ComponentType.INTERIOR, leatherInterior
                ),
                Price.of(3700000, "RUB")
        );

        car = CarHelpers.createValidCar();
    }


    @Test
    @DisplayName("Should calculate total price with base components")
    void shouldCalculatePriceWithBaseComponents() {
        Map<ComponentType, Component> selectedComponents = Map.of(
                ComponentType.WHEELS, baseWheels,
                ComponentType.INTERIOR, baseInterior
        );

        Price totalPrice = baseConfig.calculateTotalPrice(selectedComponents);

        assertEquals(3500000, totalPrice.getAmount().doubleValue(), 0.001);
    }

    @Test
    @DisplayName("Should calculate total price with upgraded wheels")
    void shouldCalculatePriceWithUpgradedWheels() {
        Map<ComponentType, Component> selectedComponents = Map.of(
                ComponentType.WHEELS, sportWheels,
                ComponentType.INTERIOR, baseInterior
        );

        Price totalPrice = baseConfig.calculateTotalPrice(selectedComponents);

        assertEquals(3595000, totalPrice.getAmount().doubleValue(), 0.001);
    }

    @Test
    @DisplayName("Should calculate total price with upgraded interior")
    void shouldCalculatePriceWithUpgradedInterior() {
        Map<ComponentType, Component> selectedComponents = Map.of(
                ComponentType.WHEELS, baseWheels,
                ComponentType.INTERIOR, leatherInterior
        );

        Price totalPrice = baseConfig.calculateTotalPrice(selectedComponents);

        assertEquals(3610000, totalPrice.getAmount().doubleValue(), 0.001);
    }

    @Test
    @DisplayName("Should calculate total price with all upgrades")
    void shouldCalculatePriceWithAllUpgrades() {
        Map<ComponentType, Component> selectedComponents = Map.of(
                ComponentType.WHEELS, sportWheels,
                ComponentType.INTERIOR, leatherInterior
        );

        Price totalPrice = baseConfig.calculateTotalPrice(selectedComponents);

        assertEquals(3705000, totalPrice.getAmount().doubleValue(), 0.001);
    }

    @Test
    @DisplayName("Should handle unknown component type in price calculation")
    void shouldHandleUnknownComponentType() {
        Component unknownComponent = new Component(
                "unknown",
                ComponentType.ENGINE,
                "Unknown",
                "Unknown component",
                Price.of(50000, "RUB"),
                Set.of(bmwModel)
        );

        Map<ComponentType, Component> selectedComponents = Map.of(
                ComponentType.WHEELS, baseWheels,
                ComponentType.INTERIOR, baseInterior,
                ComponentType.ENGINE, unknownComponent
        );

        assertThrows(DomainValidationException.class,
                () -> baseConfig.calculateTotalPrice(selectedComponents));
    }

    @Test
    @DisplayName("Should update car price after applying configuration")
    void shouldUpdateCarPriceAfterConfiguration() {
        Price initialPrice = car.getPrice();

        car.applyConfiguration(sportConfig);

        assertNotEquals(initialPrice, car.getPrice());
        assertEquals(sportConfig.getBasePrice(), car.getPrice());
    }

    @Test
    @DisplayName("Should calculate price correctly when mixing base and upgraded components")
    void shouldCalculatePriceWithMixedComponents() {
        Map<ComponentType, Component> selectedComponents = Map.of(
                ComponentType.WHEELS, sportWheels,
                ComponentType.INTERIOR, baseInterior
        );

        Price totalPrice = baseConfig.calculateTotalPrice(selectedComponents);

        assertEquals(
                baseConfig.getBasePrice().getAmount().doubleValue() + sportWheels.getExtraCharge().getAmount().doubleValue(),
                totalPrice.getAmount().doubleValue(), 0.001
        );
    }
}
