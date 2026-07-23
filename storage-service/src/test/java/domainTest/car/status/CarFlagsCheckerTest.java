package domainTest.car.status;

import domainTest.car.helpers.CarHelpers;
import domain.models.car.Car;
import domain.models.car.types.CarStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.*;

public class CarFlagsCheckerTest
{
    private Car car;

    @BeforeEach
    void setUp() {
        car = CarHelpers.createValidCar();
        car.markAsAvailable();
    }

    @Test
    @DisplayName("Should correctly identify car available for purchase")
    void shouldIdentifyAvailableForPurchase() {
        car.markAsAvailable();

        assertTrue(car.isAvailableForPurchase());
        assertFalse(car.isAvailableForTestDrive());
        assertFalse(car.isSold());
    }

    @Test
    @DisplayName("Should correctly identify car available for test drive")
    void shouldIdentifyAvailableForTestDrive() {
        car.addToTestDriveFleet();

        assertTrue(car.isAvailableForTestDrive());
        assertFalse(car.isAvailableForPurchase());
        assertFalse(car.isSold());
    }

    @Test
    @DisplayName("Should correctly identify sold car")
    void shouldIdentifySoldCar() {
        car.markAsSold();

        assertTrue(car.isSold());
        assertFalse(car.isAvailableForPurchase());
        assertFalse(car.isAvailableForTestDrive());
    }

    @Test
    @DisplayName("Should correctly identify car on test drive")
    void shouldIdentifyOnTestDrive() {
        car.addToTestDriveFleet();
        car.markAsTestDriveStarted();

        assertTrue(car.isOnTestDrive());
        assertFalse(car.isAvailableForPurchase());
        assertFalse(car.isAvailableForTestDrive());
    }

    @Test
    @DisplayName("Should correctly identify reserved car")
    void shouldIdentifyReservedCar() {
        car.reserve();

        assertTrue(car.isReserved());
        assertFalse(car.isAvailableForPurchase());
    }

    @Test
    @DisplayName("Should correctly identify unavailable car")
    void shouldIdentifyUnavailableCar() {
        car.markAsUnavailable();

        assertTrue(car.isUnavailable());
        assertFalse(car.isAvailableForPurchase());
    }

    @Test
    @DisplayName("Should correctly identify car in service")
    void shouldIdentifyInService() {
        car.markAsInService();

        assertTrue(car.isInService());
        assertFalse(car.isAvailableForPurchase());
    }

    @Test
    @DisplayName("Should handle full lifecycle: AVAILABLE -> TEST_DRIVE_AVAILABLE -> ON_TEST_DRIVE -> AVAILABLE")
    void shouldHandleFullLifecycleTestDrive() {
        assertEquals(CarStatus.AVAILABLE, car.getCarStatus());

        car.addToTestDriveFleet();
        assertEquals(CarStatus.TEST_DRIVE_AVAILABLE, car.getCarStatus());

        car.markAsTestDriveStarted();
        assertEquals(CarStatus.ON_TEST_DRIVE, car.getCarStatus());

        car.markAsAvailable();
        assertEquals(CarStatus.AVAILABLE, car.getCarStatus());
    }
}
