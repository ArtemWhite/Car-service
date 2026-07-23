package carIntegrationTests.carReferencesIntegrationTests;

import carIntegrationTests.BaseIntegrationTest;
import domain.exception.DomainValidationException;
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
import domain.repository.carRepository.CarRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
class CarStatusTransitionIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UUID testBrandId;
    private UUID testModelId;
    private UUID testEngineId;
    private UUID testTransmissionId;
    private UUID testFuelTypeId;
    private UUID testTransmissionTypeId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM cars");
        jdbcTemplate.execute("DELETE FROM engines");
        jdbcTemplate.execute("DELETE FROM transmissions");
        jdbcTemplate.execute("DELETE FROM car_models");
        jdbcTemplate.execute("DELETE FROM car_brands");

        testFuelTypeId = getUuidByName("engine_fuel_types", "PETROL");
        testTransmissionTypeId = getUuidByName("transmission_types", "AUTOMATIC");

        testBrandId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO car_brands (id, name, display_name, country_made, created_at, updated_at, removed) VALUES (?, 'BMW', 'БМВ', 'Germany', NOW(), NOW(), false)",
                testBrandId
        );

        testModelId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO car_models (id, name, brand_id, generation, created_at, updated_at, removed) VALUES (?, 'X5', ?, 'G05', NOW(), NOW(), false)",
                testModelId, testBrandId
        );

        testEngineId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO engines (id, fuel_type_id, displacement, horse_power, created_at, updated_at, removed) VALUES (?, ?, 2.0, 249.0, NOW(), NOW(), false)",
                testEngineId, testFuelTypeId
        );

        testTransmissionId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO transmissions (id, type_id, gears, created_at, updated_at, removed) VALUES (?, ?, 8, NOW(), NOW(), false)",
                testTransmissionId, testTransmissionTypeId
        );
    }

    private UUID getUuidByName(String tableName, String name) {
        String sql = String.format("SELECT id FROM %s WHERE name = ?", tableName);
        String idStr = jdbcTemplate.queryForObject(sql, String.class, name);
        return idStr != null ? UUID.fromString(idStr) : null;
    }

    private Car createBaseCar() {
        CarModel model = new CarModel(testModelId.toString(), "X5", CarBrand.BMW, "G05");
        Engine engine = new Engine(testEngineId.toString(), EngineFuelType.PETROL, EngineDisplacement.of(2.0), EnginePower.of(249.0));
        Transmission transmission = new Transmission(TransmissionType.AUTOMATIC, 8);
        transmission.setId(testTransmissionId.toString());
        Price price = Price.of(2500000.0, "RUB");

        return new Car(null, CarBrand.BMW, model, CarBody.SEDAN, CarColor.BLACK, DriveType.FRONT, engine, transmission, price);
    }

    @Test
    @Transactional
    @Rollback
    void shouldTransitionFromUnavailableToAvailable() {
        Car car = createBaseCar();
        Car saved = carRepository.save(car);
        assertThat(saved.getCarStatus()).isEqualTo(CarStatus.UNAVAILABLE);
        saved.markAsAvailable();
        Car updated = carRepository.save(saved);
        assertThat(updated.getCarStatus()).isEqualTo(CarStatus.AVAILABLE);
    }

    @Test
    @Transactional
    @Rollback
    void shouldNotTransitionFromSoldToAvailable() {
        Car car = createBaseCar();
        Car saved = carRepository.save(car);
        saved.markAsAvailable();
        saved.markAsSold();
        Car soldCar = carRepository.save(saved);
        assertThat(soldCar.getCarStatus()).isEqualTo(CarStatus.SOLD);
        assertThatThrownBy(soldCar::markAsAvailable)
                .isInstanceOf(domain.exception.DomainValidationException.class)
                .hasMessageContaining("Cannot mark sold car as available");
    }

    @Test
    @Transactional
    @Rollback
    void shouldTransitionFromAvailableToReserved() {
        Car car = createBaseCar();
        Car saved = carRepository.save(car);
        saved.markAsAvailable();
        saved.reserve();
        Car updated = carRepository.save(saved);
        assertThat(updated.getCarStatus()).isEqualTo(CarStatus.RESERVED);
    }

    @Test
    @Transactional
    @Rollback
    void shouldNotReserveNonAvailableCar() {
        Car car = createBaseCar();
        Car saved = carRepository.save(car);
        assertThatThrownBy(saved::reserve)
                .isInstanceOf(domain.exception.DomainValidationException.class)
                .hasMessageContaining("Car is not available for reservation");
    }

    @Test
    @Transactional
    @Rollback
    void shouldTransitionFromAvailableToTestDriveFleet() {
        Car car = createBaseCar();
        Car saved = carRepository.save(car);
        saved.markAsAvailable();
        saved.addToTestDriveFleet();
        Car updated = carRepository.save(saved);
        assertThat(updated.getCarStatus()).isEqualTo(CarStatus.TEST_DRIVE_AVAILABLE);
    }

    @Test
    @Transactional
    @Rollback
    void shouldTransitionFromTestDriveAvailableToOnTestDrive() {
        Car car = createBaseCar();
        Car saved = carRepository.save(car);
        saved.markAsAvailable();
        saved.addToTestDriveFleet();
        saved.markAsTestDriveStarted();
        Car updated = carRepository.save(saved);
        assertThat(updated.getCarStatus()).isEqualTo(CarStatus.ON_TEST_DRIVE);
    }

    @Test
    @Transactional
    @Rollback
    void shouldNotStartTestDriveIfNotInFleet() {
        Car car = createBaseCar();
        Car saved = carRepository.save(car);
        saved.markAsAvailable();
        assertThatThrownBy(saved::markAsTestDriveStarted)
                .isInstanceOf(domain.exception.DomainValidationException.class)
                .hasMessageContaining("Car cannot be used for test drives");
    }

    @Test
    @Transactional
    @Rollback
    void shouldTransitionFromAvailableToSold() {
        Car car = createBaseCar();
        Car saved = carRepository.save(car);
        saved.markAsAvailable();
        saved.markAsSold();
        Car updated = carRepository.save(saved);
        assertThat(updated.getCarStatus()).isEqualTo(CarStatus.SOLD);
        assertThat(updated.isSold()).isTrue();
    }

    @Test
    @Transactional
    @Rollback
    void shouldNotTransitionFromBookedToSold() {
        Car car = createBaseCar();
        Car saved = carRepository.save(car);
        saved.markAsAvailable();
        saved.markAsBooked();

        assertThatThrownBy(saved::markAsSold)
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("Car is not available for sale");

        assertThat(saved.getCarStatus()).isEqualTo(CarStatus.BOOKED);
    }

    @Test
    @Transactional
    @Rollback
    void shouldSellReservedCar() {
        Car car = createBaseCar();
        Car saved = carRepository.save(car);
        saved.markAsAvailable();
        saved.reserve();
        saved.markAsSold();
        assertThat(saved.getCarStatus()).isEqualTo(CarStatus.SOLD);
    }

    @Test
    @Transactional
    @Rollback
    void shouldTransitionFromAvailableToInService() {
        Car car = createBaseCar();
        Car saved = carRepository.save(car);
        saved.markAsAvailable();
        saved.markAsInService();
        Car updated = carRepository.save(saved);
        assertThat(updated.getCarStatus()).isEqualTo(CarStatus.IN_SERVICE);
    }

    @Test
    @Transactional
    @Rollback
    void shouldTransitionFromAvailableToBooked() {
        Car car = createBaseCar();
        Car saved = carRepository.save(car);
        saved.markAsAvailable();
        saved.markAsBooked();
        Car updated = carRepository.save(saved);
        assertThat(updated.getCarStatus()).isEqualTo(CarStatus.BOOKED);
    }

    @Test
    @Transactional
    @Rollback
    void shouldNotBookNonAvailableCar() {
        Car car = createBaseCar();
        Car saved = carRepository.save(car);
        assertThatThrownBy(saved::markAsBooked)
                .isInstanceOf(domain.exception.DomainValidationException.class)
                .hasMessageContaining("Car is not available for booking");
    }

    @Test
    @Transactional
    @Rollback
    void shouldBeAvailableForPurchaseWhenAvailable() {
        Car car = createBaseCar();
        Car saved = carRepository.save(car);
        saved.markAsAvailable();
        Car updated = carRepository.save(saved);
        assertThat(updated.isAvailableForPurchase()).isTrue();
    }

    @Test
    @Transactional
    @Rollback
    void shouldNotBeAvailableForPurchaseWhenSold() {
        Car car = createBaseCar();
        Car saved = carRepository.save(car);
        saved.markAsAvailable();
        saved.markAsSold();
        Car updated = carRepository.save(saved);
        assertThat(updated.isAvailableForPurchase()).isFalse();
    }

    @Test
    @Transactional
    @Rollback
    void shouldBeAvailableForTestDriveOnlyInTestDriveFleet() {
        Car car = createBaseCar();
        Car saved = carRepository.save(car);
        saved.markAsAvailable();
        Car available = carRepository.save(saved);
        assertThat(available.isAvailableForTestDrive()).isFalse();
        available.addToTestDriveFleet();
        Car testDriveAvailable = carRepository.save(available);
        assertThat(testDriveAvailable.isAvailableForTestDrive()).isTrue();
    }

    @Test
    @Transactional
    @Rollback
    void shouldTransitionFromTestDriveToAvailableAfterRemoval() {
        Car car = createBaseCar();
        Car saved = carRepository.save(car);
        saved.markAsAvailable();
        saved.addToTestDriveFleet();
        Car inFleet = carRepository.save(saved);
        assertThat(inFleet.getCarStatus()).isEqualTo(CarStatus.TEST_DRIVE_AVAILABLE);

        inFleet.markAsAvailable();
        Car available = carRepository.save(inFleet);
        assertThat(available.getCarStatus()).isEqualTo(CarStatus.AVAILABLE);
    }

    @Test
    @Transactional
    @Rollback
    void shouldNotAddToTestDriveFleetWhenSold() {
        Car car = createBaseCar();
        Car saved = carRepository.save(car);
        saved.markAsAvailable();
        saved.markAsSold();
        Car soldCar = carRepository.save(saved);

        assertThatThrownBy(soldCar::addToTestDriveFleet)
                .isInstanceOf(domain.exception.DomainValidationException.class)
                .hasMessageContaining("Car cannot be used for test drives");
    }

    @Test
    @Transactional
    @Rollback
    void shouldTransitionFromUnavailableToInService() {
        Car car = createBaseCar();
        Car saved = carRepository.save(car);
        assertThat(saved.getCarStatus()).isEqualTo(CarStatus.UNAVAILABLE);

        saved.markAsInService();
        Car updated = carRepository.save(saved);
        assertThat(updated.getCarStatus()).isEqualTo(CarStatus.IN_SERVICE);
    }

    @Test
    @Transactional
    @Rollback
    void shouldTransitionFromInServiceToAvailable() {
        Car car = createBaseCar();
        Car saved = carRepository.save(car);
        saved.markAsInService();
        Car inService = carRepository.save(saved);
        assertThat(inService.getCarStatus()).isEqualTo(CarStatus.IN_SERVICE);

        inService.markAsAvailable();
        Car available = carRepository.save(inService);
        assertThat(available.getCarStatus()).isEqualTo(CarStatus.AVAILABLE);
    }

    @Test
    @Transactional
    @Rollback
    void shouldReturnCorrectCarInfoBasedOnStatus() {
        Car car = createBaseCar();
        Car saved = carRepository.save(car);

        saved.markAsAvailable();
        Car available = carRepository.save(saved);

        assertThat(available.getCarInfo()).doesNotContain("Доступен для продажи");
        assertThat(available.getCarInfo()).contains("БМВ");
        assertThat(available.getCarInfo()).contains("X5");

        available.markAsSold();
        Car sold = carRepository.save(available);

        assertThat(sold.getCarInfo()).doesNotContain("Продан");
    }

    @Test
    @Transactional
    @Rollback
    void shouldNotReserveAlreadyReservedCar() {
        Car car = createBaseCar();
        Car saved = carRepository.save(car);
        saved.markAsAvailable();
        saved.reserve();
        Car reserved = carRepository.save(saved);

        assertThatThrownBy(reserved::reserve)
                .isInstanceOf(domain.exception.DomainValidationException.class);
    }

    @Test
    @Transactional
    @Rollback
    void shouldNotSellAlreadySoldCar() {
        Car car = createBaseCar();
        Car saved = carRepository.save(car);
        saved.markAsAvailable();
        saved.markAsSold();
        Car sold = carRepository.save(saved);

        assertThatThrownBy(sold::markAsSold)
                .isInstanceOf(domain.exception.DomainValidationException.class);
    }
}