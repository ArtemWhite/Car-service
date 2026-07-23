package domainTest.car.configurationTest;

import domainTest.car.helpers.CarHelpers;
import domain.exception.DomainValidationException;
import domain.exception.IncompatibleComponentException;
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

public class CarConfigurationTest
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
    @DisplayName("Should apply base configuration to car")
    void shouldApplyBaseConfiguration() {
        assertNull(car.getConfiguration());

        car.applyConfiguration(baseConfig);

        assertNotNull(car.getConfiguration());
        assertEquals(baseConfig, car.getConfiguration());
        assertEquals(baseConfig.getBasePrice(), car.getPrice());
    }

    @Test
    @DisplayName("Should apply sport configuration to car")
    void shouldApplySportConfiguration() {
        assertNull(car.getConfiguration());

        car.applyConfiguration(sportConfig);

        assertNotNull(car.getConfiguration());
        assertEquals(sportConfig, car.getConfiguration());
        assertEquals(sportConfig.getBasePrice(), car.getPrice());
    }

    @Test
    @DisplayName("Should throw exception when applying null configuration")
    void shouldThrowExceptionWhenApplyingNullConfiguration() {
        assertThrows(DomainValidationException.class, () -> car.applyConfiguration(null));
    }

    @Test
    @DisplayName("Should throw exception when applying configuration for different model")
    void shouldThrowExceptionWhenApplyingWrongModelConfiguration() {
        CarConfiguration audiConfig = new CarConfiguration(
                "config3",
                "Base",
                audiModel,
                Map.of(ComponentType.WHEELS, sportWheels),
                Price.of(4000000, "RUB")
        );

        assertThrows(IncompatibleComponentException.class, () -> car.applyConfiguration(audiConfig));
    }
}
