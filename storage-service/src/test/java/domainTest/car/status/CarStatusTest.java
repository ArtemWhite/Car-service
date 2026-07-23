package domainTest.car.status;

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
import domain.models.car.types.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CarStatusTest
{

    private Car car;

    @BeforeEach
    void setUp() {
        car = CarHelpers.createValidCar();
        car.markAsAvailable();
    }

    @Test
    public void shouldChangeStatusFromAvailableToSold() {
        Car car = new Car("312", CarBrand.BMW,
                new CarModel("23", "X5", CarBrand.BMW, null),
                CarBody.UNIVERSAL, CarColor.RED, DriveType.REAR,
                new Engine("des", EngineFuelType.DIESEL, new EngineDisplacement(8.0), new EnginePower(7.4)),
                new Transmission(TransmissionType.AUTOMATIC, 8),
                new Price(BigDecimal.valueOf(45003213), Currency.getInstance("RUB"), false));

        car.markAsAvailable();
        car.markAsSold();

        assertEquals(CarStatus.SOLD, car.getCarStatus());
    }

    @Test
    void shouldSetDefaultStatusToNull() {
        Car car = CarHelpers.createValidCar();

        Assertions.assertEquals(CarStatus.UNAVAILABLE,car.getCarStatus());
        Assertions.assertNull(car.getConfiguration());
    }

    @Test
    void shouldHaveCorrectInitialState() {
        Car car = CarHelpers.createValidCar();

        assertEquals(CarStatus.UNAVAILABLE,car.getCarStatus());
        assertNull(car.getConfiguration());
        assertNotNull(car.getCarId());
        assertNotNull(car.getBrand());
        assertNotNull(car.getModel());
        assertNotNull(car.getEngine());
        assertNotNull(car.getTransmission());
    }


    @Test
    void shouldChangeFromAvailableToSold() {
        assertEquals(CarStatus.AVAILABLE, car.getCarStatus());

        car.markAsSold();

        assertEquals(CarStatus.SOLD, car.getCarStatus());
    }

    @Test
    void shouldChangeFromAvailableToTestDriveAvailable() {
        assertEquals(CarStatus.AVAILABLE, car.getCarStatus());

        car.addToTestDriveFleet();

        assertEquals(CarStatus.TEST_DRIVE_AVAILABLE, car.getCarStatus());
    }

    @Test
    void shouldChangeFromAvailableToReserved() {
        assertEquals(CarStatus.AVAILABLE, car.getCarStatus());

        car.reserve();

        assertEquals(CarStatus.RESERVED, car.getCarStatus());
    }

    @Test
    void shouldChangeFromAvailableToBooked() {
        assertEquals(CarStatus.AVAILABLE, car.getCarStatus());

        car.markAsBooked();

        assertEquals(CarStatus.BOOKED, car.getCarStatus());
    }

    @Test
    void shouldChangeFromTestDriveAvailableToOnTestDrive() {
        car.addToTestDriveFleet();
        assertEquals(CarStatus.TEST_DRIVE_AVAILABLE, car.getCarStatus());

        car.markAsTestDriveStarted();

        assertEquals(CarStatus.ON_TEST_DRIVE, car.getCarStatus());
    }

    @Test
    void shouldChangeFromAvailableToUnavailable() {
        assertEquals(CarStatus.AVAILABLE, car.getCarStatus());

        car.markAsUnavailable();

        assertEquals(CarStatus.UNAVAILABLE, car.getCarStatus());
    }

    @Test
    void shouldChangeFromAvailableToInService() {
        assertEquals(CarStatus.AVAILABLE, car.getCarStatus());

        car.markAsInService();

        assertEquals(CarStatus.IN_SERVICE, car.getCarStatus());
    }
}


