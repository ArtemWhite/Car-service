package domainTest.car.helpers;

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
import domain.models.car.types.CarBrand;
import domain.models.car.types.CarColor;
import domain.models.car.types.DriveType;

import java.math.BigDecimal;
import java.util.Currency;

public class CarHelpers
{
    public static Car createValidCar() {
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

        CarModel bmw320iModel = new CarModel("dfgfht", "320i", CarBrand.BMW, "G20");

        Car car = new Car(
                "car123",
                CarBrand.BMW,
                bmw320iModel,
                CarBody.SEDAN,
                CarColor.BLACK,
                DriveType.REAR,
                engine,
                transmission,
                new Price(BigDecimal.valueOf(3500000), Currency.getInstance("RUB"), false)
        );
        return car;
    }



    public static Engine createEngine() {
        return new Engine(
                "engine123",
                EngineFuelType.PETROL,
                new EngineDisplacement(2.0),
                new EnginePower(184)
        );
    }

    public static Transmission createTransmission() {
        return new Transmission(TransmissionType.AUTOMATIC, 8);
    }

    public static Price createPrice() {
        return new Price(BigDecimal.valueOf(3500000), Currency.getInstance("RUB"), false);
    }

    public static Price createPrice(double amount) {
        return new Price(BigDecimal.valueOf(amount), Currency.getInstance("RUB"), false);
    }
}
