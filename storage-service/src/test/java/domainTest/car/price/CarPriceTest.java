package domainTest.car.price;

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

public class CarPriceTest
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
    @DisplayName("Should update car price successfully")
    void shouldUpdateCarPrice() {
        Price initialPrice = car.getPrice();
        Price newPrice = Price.of(4000000, "RUB");

        car.setPrice(newPrice);

        assertNotEquals(initialPrice, car.getPrice());
        assertEquals(newPrice, car.getPrice());
        assertEquals(4000000, car.getPrice().getAmount().doubleValue(), 0.000001);
    }

    @Test
    @DisplayName("Should throw exception when setting null price")
    void shouldThrowExceptionWhenSettingNullPrice() {
        assertThrows(DomainValidationException.class, () -> car.setPrice(null));
    }

    @Test
    @DisplayName("Should allow updating price multiple times")
    void shouldAllowMultiplePriceUpdates() {
        car.setPrice(Price.of(3600000, "RUB"));
        assertEquals(3600000, car.getPrice().getAmount().doubleValue(), 0.000001);

        car.setPrice(Price.of(3700000, "RUB"));
        assertEquals(3700000, car.getPrice().getAmount().doubleValue(), 0.000001);

        car.setPrice(Price.of(3800000, "RUB"));
        assertEquals(3800000, car.getPrice().getAmount().doubleValue(), 0.000001);
    }

    @Test
    @DisplayName("Should update price after applying configuration")
    void shouldUpdatePriceAfterConfiguration() {
        Price initialPrice = car.getPrice();

        car.applyConfiguration(baseConfig);

        assertNotEquals(initialPrice, car.getPrice());
        assertEquals(baseConfig.getBasePrice(), car.getPrice());
    }

    @Test
    @DisplayName("Should update price after applying configuration with upgraded components")
    void shouldUpdatePriceAfterUpgradedConfiguration() {
        CarConfiguration upgradedConfig = new CarConfiguration(
                "config2",
                "Upgraded",
                bmwModel,
                Map.of(
                        ComponentType.WHEELS, sportWheels,
                        ComponentType.INTERIOR, leatherInterior
                ),
                Price.of(3700000, "RUB")
        );

        car.applyConfiguration(upgradedConfig);

        assertEquals(3700000, car.getPrice().getAmount().doubleValue(), 0.0001);
    }

    @Test
    @DisplayName("Should preserve manually set price after configuration")
    void shouldPreserveManualPriceAfterConfiguration() {
        car.setPrice(Price.of(5000000, "RUB"));

        car.applyConfiguration(baseConfig);

        assertEquals(baseConfig.getBasePrice(), car.getPrice());
        assertNotEquals(5000000, car.getPrice().getAmount().doubleValue(), 0.0001);
    }

    @Test
    @DisplayName("Should update price correctly when configuration changes")
    void shouldUpdatePriceWhenConfigurationChanges() {
        CarConfiguration sportConfig = new CarConfiguration(
                "sport",
                "Sport",
                bmwModel,
                Map.of(
                        ComponentType.WHEELS, sportWheels,
                        ComponentType.INTERIOR, baseInterior
                ),
                Price.of(3595000, "RUB")
        );

        car.applyConfiguration(baseConfig);
        assertEquals(3500000, car.getPrice().getAmount().doubleValue(),0.0001);

        car.applyConfiguration(sportConfig);

        assertEquals(3595000, car.getPrice().getAmount().doubleValue(), 0.0001);
    }
}
