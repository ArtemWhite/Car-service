package carIntegrationTests.carSpecificIntegrationTests;

import carIntegrationTests.BaseIntegrationTest;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Transactional
class CarNegativeScenariosIntegrationTest extends BaseIntegrationTest {

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
    void shouldFailCreateCarWithZeroPrice() {
        CarModel model = new CarModel(testModelId.toString(), "X5", CarBrand.BMW, "G05");
        Engine engine = new Engine(testEngineId.toString(), EngineFuelType.PETROL, EngineDisplacement.of(2.0), EnginePower.of(249.0));
        Transmission transmission = new Transmission(TransmissionType.AUTOMATIC, 8);
        transmission.setId(testTransmissionId.toString());

        assertThatThrownBy(() -> Price.of(-0.1, "RUB"))
                .isInstanceOf(domain.exception.DomainValidationException.class);
    }

    @Test
    @Transactional
    @Rollback
    void shouldFailCreateCarWithNegativePrice() {
        assertThatThrownBy(() -> Price.of(-1000.0, "RUB"))
                .isInstanceOf(domain.exception.DomainValidationException.class);
    }

    @Test
    @Transactional
    @Rollback
    void shouldFailCreateEngineWithNegativePower() {
        assertThatThrownBy(() -> EnginePower.of(-100.0))
                .isInstanceOf(domain.exception.DomainValidationException.class);
    }

    @Test
    @Transactional
    @Rollback
    void shouldFailCreateEngineWithZeroPower() {
        assertThatThrownBy(() -> EnginePower.of(0.0))
                .isInstanceOf(domain.exception.DomainValidationException.class);
    }

    @Test
    @Transactional
    @Rollback
    void shouldFailCreateEngineWithNegativeDisplacement() {
        assertThatThrownBy(() -> EngineDisplacement.of(-1.0))
                .isInstanceOf(domain.exception.DomainValidationException.class);
    }

    @Test
    @Transactional
    @Rollback
    void shouldFailMarkAlreadySoldCarAsAvailable() {
        Car car = createBaseCar();
        Car saved = carRepository.save(car);
        saved.markAsAvailable();
        saved.markAsSold();
        Car sold = carRepository.save(saved);

        assertThatThrownBy(sold::markAsAvailable)
                .isInstanceOf(domain.exception.DomainValidationException.class);
    }

    @Test
    @Transactional
    @Rollback
    void shouldMarkReservedCarAsSold() {
        Car car = createBaseCar();
        Car saved = carRepository.save(car);
        saved.markAsAvailable();
        saved.reserve();
        Car reserved = carRepository.save(saved);

        reserved.markAsSold();
        assertThat(reserved.getCarStatus()).isEqualTo(CarStatus.SOLD);
    }

    @Test
    @Transactional
    @Rollback
    void shouldFailReserveAlreadyReservedCar() {
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
    void shouldReturnEmptyWhenCarNotFound() {
        var found = carRepository.findById(UUID.randomUUID().toString());
        assertThatThrownBy(() -> found.orElseThrow())
                .isInstanceOf(java.util.NoSuchElementException.class);
    }

    @Test
    @Transactional
    @Rollback
    void shouldFailCreateCarWithNullModel() {
        CarModel model = null;
        Engine engine = new Engine(testEngineId.toString(), EngineFuelType.PETROL,
                EngineDisplacement.of(2.0), EnginePower.of(249.0));
        Transmission transmission = new Transmission(TransmissionType.AUTOMATIC, 8);
        transmission.setId(testTransmissionId.toString());
        Price price = Price.of(2500000.0, "RUB");

        assertThatThrownBy(() -> new Car(null, CarBrand.BMW, model, CarBody.SEDAN,
                CarColor.BLACK, DriveType.FRONT, engine, transmission, price))
                .isInstanceOf(domain.exception.DomainValidationException.class)
                .hasMessageContaining("Car model cannot be null");
    }

    @Test
    @Transactional
    @Rollback
    void shouldFailCreateCarWithModelNotBelongingToBrand() {
        CarModel wrongModel = new CarModel(UUID.randomUUID().toString(), "Camry", CarBrand.TOYOTA, "XV70");
        Engine engine = new Engine(testEngineId.toString(), EngineFuelType.PETROL, EngineDisplacement.of(2.0), EnginePower.of(249.0));
        Transmission transmission = new Transmission(TransmissionType.AUTOMATIC, 8);
        transmission.setId(testTransmissionId.toString());
        Price price = Price.of(2500000.0, "RUB");

        assertThatThrownBy(() -> new Car(null, CarBrand.BMW, wrongModel, CarBody.SEDAN, CarColor.BLACK, DriveType.FRONT, engine, transmission, price))
                .isInstanceOf(domain.exception.DomainValidationException.class)
                .hasMessageContaining("Model does not belong to selected brand");
    }

    @Test
    @Transactional
    @Rollback
    void shouldFailCreateCarWithModelButNoBrand() {
        CarModel model = new CarModel(testModelId.toString(), "X5", CarBrand.BMW, "G05");
        Engine engine = new Engine(testEngineId.toString(), EngineFuelType.PETROL, EngineDisplacement.of(2.0), EnginePower.of(249.0));
        Transmission transmission = new Transmission(TransmissionType.AUTOMATIC, 8);
        transmission.setId(testTransmissionId.toString());
        Price price = Price.of(2500000.0, "RUB");

        assertThatThrownBy(() -> new Car(null, null, model, CarBody.SEDAN, CarColor.BLACK, DriveType.FRONT, engine, transmission, price))
                .isInstanceOf(domain.exception.DomainValidationException.class)
                .hasMessageContaining("Car brand cannot be null");
    }
}