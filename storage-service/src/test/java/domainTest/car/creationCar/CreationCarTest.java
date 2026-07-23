package domainTest.car.creationCar;

import domainTest.car.helpers.CarHelpers;
import domain.models.car.Car;
import domain.models.car.CarModel;
import domain.models.car.Price;
import domain.models.car.engine.Engine;
import domain.models.car.engine.EngineDisplacement;
import domain.models.car.engine.EngineFuelType;
import domain.models.car.engine.EnginePower;
import domain.models.car.transmission.Transmission;
import domain.models.car.transmission.TransmissionType;
import domain.models.car.types.CarBody;
import domain.models.car.types.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class CreationCarTest
{
    @Test
    void shouldCreateCarWithValidParameters() {
        Engine engine = new Engine(
                "engine123",
                EngineFuelType.PETROL,
                new EngineDisplacement(2.0),
                new EnginePower(184)
        );

        Transmission transmission = new Transmission(
                TransmissionType.AUTOMATIC,
                8
        );

        Car car = new Car(
                "car123",
                CarBrand.BMW,
                new CarModel("dfgghtr", "320i", CarBrand.BMW, null),
                CarBody.SEDAN,
                CarColor.BLACK,
                DriveType.REAR,
                engine,
                transmission,
                new Price(BigDecimal.valueOf(3500000), Currency.getInstance("RUB"), false)
        );

        Assertions.assertNotNull(car);
        Assertions.assertEquals("car123", car.getCarId());
        Assertions.assertEquals(CarBrand.BMW, car.getBrand());
        Assertions.assertEquals("320i", car.getModel().getName());
        Assertions.assertEquals(CarBody.SEDAN, car.getBody());
        Assertions.assertEquals(CarColor.BLACK, car.getColor());
        Assertions.assertEquals(DriveType.REAR, car.getDriveType());
        Assertions.assertEquals(EngineFuelType.PETROL, car.getEngine().getEngineFuelType());
        Assertions.assertEquals(2.0, car.getEngine().getEngineDisplacement().getLiters());
        Assertions.assertEquals(184, car.getEngine().getEnginePower().getHorsePower());
        Assertions.assertEquals(TransmissionType.AUTOMATIC, car.getTransmission().getTransmissionType());
        Assertions.assertEquals(8, car.getTransmission().getGears());
        Assertions.assertEquals(3500000, car.getPrice().getAmount().doubleValue());
    }

    @Test
    void shouldCreateCarWithDifferentEngineTypes() {
        Engine petrolEngine = new Engine(
                "engine1",
                EngineFuelType.PETROL,
                new EngineDisplacement(2.0),
                new EnginePower(184)
        );

        assertDoesNotThrow(() -> {
            new Car("car1", CarBrand.BMW, new CarModel("fddfn","320i", CarBrand.BMW, "G20"),
                    CarBody.SEDAN, CarColor.BLACK, DriveType.REAR,
                    petrolEngine, CarHelpers.createTransmission(), CarHelpers.createPrice());
        });

        Engine dieselEngine = new Engine(
                "engine2",
                EngineFuelType.DIESEL,
                new EngineDisplacement(3.0),
                new EnginePower(249)
        );

        assertDoesNotThrow(() -> {
            new Car("car2", CarBrand.BMW, new CarModel("fgghyrt","330d", CarBrand.BMW, "G20"),
                    CarBody.SEDAN, CarColor.BLACK, DriveType.REAR,
                    dieselEngine, CarHelpers.createTransmission(), CarHelpers.createPrice());
        });

        Engine electricEngine = new Engine(
                "engine3",
                EngineFuelType.ELECTRIC,
                new EngineDisplacement(0.0),
                new EnginePower(400)
        );

        assertDoesNotThrow(() -> {
            new Car("car3", CarBrand.LADA, new CarModel("dfbrh","Model 3", CarBrand.LADA, null),
                    CarBody.SEDAN, CarColor.BLACK, DriveType.REAR,
                    electricEngine, CarHelpers.createTransmission(), CarHelpers.createPrice());
        });
    }

    @Test
    void shouldCreateCarWithDifferentBodyTypes() {
        CarBody[] bodyTypes = CarBody.values();

        for (CarBody bodyType : bodyTypes) {
            assertDoesNotThrow(() -> {
                new Car("car" + bodyType, CarBrand.BMW, new CarModel("dfbfgb","320i", CarBrand.BMW, "G20"),
                        bodyType, CarColor.BLACK, DriveType.REAR,
                        CarHelpers.createEngine(), CarHelpers.createTransmission(), CarHelpers.createPrice());
            });
        }
    }

    @Test
    void shouldCreateCarWithDifferentDriveTypes() {
        DriveType[] driveTypes = DriveType.values();

        for (DriveType driveType : driveTypes) {
            assertDoesNotThrow(() -> {
                new Car("car" + driveType, CarBrand.BMW, new CarModel("cb","320i", CarBrand.BMW, "G20"),
                        CarBody.SEDAN, CarColor.BLACK, driveType,
                        CarHelpers.createEngine(), CarHelpers.createTransmission(), CarHelpers.createPrice());
            });
        }
    }
}
