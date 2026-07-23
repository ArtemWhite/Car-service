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

public class CarPriceValidationTest
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
    @DisplayName("Should create price with valid amount")
    void shouldCreatePriceWithValidAmount() {
        Price price = Price.of(1000000, "RUB");

        assertNotNull(price);
        assertEquals(1000000, price.getAmount().doubleValue(), 0.0001);
        assertEquals("RUB", price.getCurrency().getCurrencyCode());
        assertFalse(price.isDiscounted());
    }

    @Test
    @DisplayName("Should create price with zero amount")
    void shouldCreatePriceWithZeroAmount() {
        Price price = Price.of(0, "RUB");

        assertNotNull(price);
        assertEquals(0, price.getAmount().doubleValue(), 0.0001);
    }

    @Test
    @DisplayName("Should throw exception when creating price with negative amount")
    void shouldThrowExceptionWhenCreatingNegativePrice() {
        assertThrows(DomainValidationException.class, () -> {
            Price.of(-1000, "RUB");
        });
    }

    @Test
    @DisplayName("Should create price with different currencies")
    void shouldCreatePriceWithDifferentCurrencies() {
        Price rubPrice = Price.of(1000000, "RUB");
        Price usdPrice = Price.of(15000, "USD");
        Price eurPrice = Price.of(14000, "EUR");

        assertEquals("RUB", rubPrice.getCurrency().getCurrencyCode());
        assertEquals("USD", usdPrice.getCurrency().getCurrencyCode());
        assertEquals("EUR", eurPrice.getCurrency().getCurrencyCode());
    }

    @Test
    @DisplayName("Should throw exception when adding prices with different currencies")
    void shouldThrowExceptionWhenAddingDifferentCurrencies() {
        Price rubPrice = Price.of(1000000, "RUB");
        Price usdPrice = Price.of(15000, "USD");

        assertThrows(DomainValidationException.class, () -> rubPrice.add(usdPrice));
    }

}
