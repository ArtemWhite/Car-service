package domainTest.car.status;

import domainTest.car.helpers.CarHelpers;
import domain.exception.DomainValidationException;
import domain.models.car.Car;
import domain.models.car.types.CarStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class CarNotAvailableStatusesTest
{
    private Car car;

    @BeforeEach
    void setUp() {
        car = CarHelpers.createValidCar();
        car.markAsAvailable();
    }

    @Test
    @DisplayName("Should not allow marking sold car as available")
    void shouldNotAllowMarkingSoldCarAsAvailable() {
        car.markAsSold();
        assertEquals(CarStatus.SOLD, car.getCarStatus());

        assertThrows(DomainValidationException.class, () -> car.markAsAvailable());
    }

    @Test
    @DisplayName("Should not allow selling unavailable car")
    void shouldNotAllowSellingUnavailableCar() {
        car.markAsUnavailable();
        assertEquals(CarStatus.UNAVAILABLE, car.getCarStatus());

        assertThrows(DomainValidationException.class, () -> car.markAsSold());
    }

    @Test
    void shouldNotAddSoldCarToTestDriveFleet() {
        car.markAsSold();
        assertEquals(CarStatus.SOLD, car.getCarStatus());

        assertThrows(DomainValidationException.class, () -> car.addToTestDriveFleet());
    }

    @Test
    void shouldNotReserveSoldCar() {
        car.markAsSold();
        assertEquals(CarStatus.SOLD, car.getCarStatus());

        assertThrows(DomainValidationException.class, () -> car.reserve());
    }

    @Test
    void shouldNotBookSoldCar() {
        car.markAsSold();
        assertEquals(CarStatus.SOLD, car.getCarStatus());

        assertThrows(DomainValidationException.class, () -> car.markAsBooked());
    }

    @Test
    void shouldNotStartTestDriveOnNonTestDriveCar() {
        assertEquals(CarStatus.AVAILABLE, car.getCarStatus());

        assertThrows(DomainValidationException.class, () -> car.markAsTestDriveStarted());
    }

    @Test
    void shouldNotAllowMultipleChangesFromSameState() {

        car.addToTestDriveFleet();
        assertEquals(CarStatus.TEST_DRIVE_AVAILABLE, car.getCarStatus());

        assertThrows(DomainValidationException.class, () -> car.addToTestDriveFleet());
    }
}
