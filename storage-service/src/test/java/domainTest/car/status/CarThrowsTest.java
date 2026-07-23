package domainTest.car.status;

import domainTest.car.helpers.CarHelpers;
import domain.exception.DomainValidationException;
import domain.models.car.Car;
import domain.models.car.CarModel;
import domain.models.car.engine.Engine;
import domain.models.car.engine.*;
import domain.models.car.types.CarBody;
import domain.models.car.types.CarBrand;
import domain.models.car.types.CarColor;
import domain.models.car.types.DriveType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.*;

public class CarThrowsTest
{
    @Test
    void shouldThrowExceptionWhenBrandIsNull() {
        assertThrows(DomainValidationException.class, () -> {
            new Car("car1", null, new CarModel("dsvfdv","320i", CarBrand.BMW, "G20"),
                    CarBody.SEDAN, CarColor.BLACK, DriveType.REAR,
                    CarHelpers.createEngine(), CarHelpers.createTransmission(), CarHelpers.createPrice());
        });
    }

    @Test
    @DisplayName("Should create car with valid model")
    void shouldCreateCarWithValidModel() {
        CarModel model = new CarModel("model1", "X5", CarBrand.BMW, "G05");

        Car car = new Car("car1", CarBrand.BMW, model,
                CarBody.SEDAN, CarColor.BLACK, DriveType.REAR,
                CarHelpers.createEngine(), CarHelpers.createTransmission(), CarHelpers.createPrice());

        assertNotNull(car);
        assertNotNull(car.getModel());
        assertEquals("X5", car.getModel().getName());
    }

    @Test
    void shouldThrowExceptionWhenElectricEngineHasDisplacement() {
        assertThrows(DomainValidationException.class, () -> {
            new Car("car1", CarBrand.LADA, new CarModel("fbf","Model 3", CarBrand.LADA, null),
                    CarBody.SEDAN, CarColor.BLACK, DriveType.REAR,
                    new Engine("engine1",
                    EngineFuelType.ELECTRIC,
                    new EngineDisplacement(2.0),
                    new EnginePower(400)),
                    CarHelpers.createTransmission(), CarHelpers.createPrice());
        });
    }
}
