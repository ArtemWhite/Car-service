package carIntegrationTests;

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

class CarRepositoryIntegrationTest extends BaseIntegrationTest {

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
    private UUID testDriveTypeId;
    private UUID testCarStatusId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM cars");
        jdbcTemplate.execute("DELETE FROM engines");
        jdbcTemplate.execute("DELETE FROM transmissions");
        jdbcTemplate.execute("DELETE FROM car_models");
        jdbcTemplate.execute("DELETE FROM car_brands");

        testFuelTypeId = getUuidByName("engine_fuel_types", "PETROL");
        testTransmissionTypeId = getUuidByName("transmission_types", "AUTOMATIC");
        testDriveTypeId = getUuidByName("drive_types", "FRONT");
        testCarStatusId = getUuidByName("car_statuses", "UNAVAILABLE");

        testBrandId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO car_brands (id, name, display_name, country_made, created_at, updated_at, removed) VALUES (?, ?, ?, ?, NOW(), NOW(), false)",
                testBrandId, "BMW", "БМВ", "Germany"
        );

        testModelId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO car_models (id, name, brand_id, generation, created_at, updated_at, removed) VALUES (?, ?, ?, ?, NOW(), NOW(), false)",
                testModelId, "X5", testBrandId, "G05"
        );

        testEngineId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO engines (id, fuel_type_id, displacement, horse_power, created_at, updated_at, removed) VALUES (?, ?, ?, ?, NOW(), NOW(), false)",
                testEngineId, testFuelTypeId, 2.0, 249.0
        );

        testTransmissionId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO transmissions (id, type_id, gears, created_at, updated_at, removed) VALUES (?, ?, ?, NOW(), NOW(), false)",
                testTransmissionId, testTransmissionTypeId, 8
        );
    }

    private UUID getUuidByName(String tableName, String name) {
        String sql = String.format("SELECT id FROM %s WHERE name = ?", tableName);
        String idStr = jdbcTemplate.queryForObject(sql, String.class, name);
        return idStr != null ? UUID.fromString(idStr) : null;
    }

    private Car createTestCar() {
        CarModel model = new CarModel(
                testModelId.toString(),
                "X5",
                CarBrand.BMW,
                "G05"
        );

        Engine engine = new Engine(
                testEngineId.toString(),
                EngineFuelType.PETROL,
                EngineDisplacement.of(2.0),
                EnginePower.of(249.0)
        );
        // engine.setId(testEngineId.toString());

        Transmission transmission = new Transmission(TransmissionType.AUTOMATIC, 8);
        transmission.setId(testTransmissionId.toString());

        Price price = Price.of(2500000.0, "RUB");

        return new Car(
                null,
                CarBrand.BMW,
                model,
                CarBody.SEDAN,
                CarColor.BLACK,
                DriveType.FRONT,
                engine,
                transmission,
                price
        );
    }

    @Test
    @Transactional
    @Rollback
    void shouldSaveAndFindCar() {
        Car car = createTestCar();
        Car saved = carRepository.save(car);
        Car found = carRepository.findById(saved.getCarId()).orElse(null);

        assertThat(found).isNotNull();
        assertThat(found.getBrand()).isEqualTo(CarBrand.BMW);
        assertThat(found.getModel().getName()).isEqualTo("X5");
    }

    @Test
    @Transactional
    @Rollback
    void shouldDeleteCar() {
        Car car = createTestCar();
        Car saved = carRepository.save(car);
        carRepository.delete(saved.getCarId());
        assertThat(carRepository.findById(saved.getCarId())).isEmpty();
    }

    @Test
    @Transactional
    @Rollback
    void shouldUpdateCarPrice() {
        Car car = createTestCar();
        Car saved = carRepository.save(car);
        Price newPrice = Price.of(3000000.0, "RUB");
        saved.setPrice(newPrice);
        Car result = carRepository.save(saved);
        assertThat(result.getPrice().getAmount().doubleValue()).isEqualTo(3000000.0);
    }

    @Test
    @Transactional
    @Rollback
    void shouldMarkCarAsAvailable() {
        Car car = createTestCar();
        Car saved = carRepository.save(car);

        saved.markAsAvailable();
        Car result = carRepository.save(saved);

        assertThat(result.isAvailableForPurchase()).isTrue();
        assertThat(result.getCarStatus()).isEqualTo(CarStatus.AVAILABLE);
    }

    @Test
    @Transactional
    @Rollback
    void shouldMarkCarAsSold() {
        Car car = createTestCar();
        Car saved = carRepository.save(car);

        saved.markAsAvailable();
        saved.markAsSold();

        Car result = carRepository.save(saved);

        assertThat(result.isSold()).isTrue();
        assertThat(result.getCarStatus()).isEqualTo(CarStatus.SOLD);
    }

    @Test
    @Transactional
    @Rollback
    void shouldReserveCar() {
        Car car = createTestCar();
        Car saved = carRepository.save(car);

        saved.markAsAvailable();
        saved.reserve();

        Car result = carRepository.save(saved);

        assertThat(result.isReserved()).isTrue();
        assertThat(result.getCarStatus()).isEqualTo(CarStatus.RESERVED);
    }

    @Test
    @Transactional
    @Rollback
    void shouldReturnEmptyWhenCarNotFound() {
        var found = carRepository.findById(UUID.randomUUID().toString());
        assertThat(found).isEmpty();
    }

    @Test
    @Transactional
    @Rollback
    void shouldFindAllCars() {
        Car car1 = createTestCar();
        Car car2 = createTestCar();
        carRepository.save(car1);
        carRepository.save(car2);
        var allCars = carRepository.findAll();
        assertThat(allCars).isNotNull();
        assertThat(allCars.size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    @Transactional
    @Rollback
    void shouldSaveCarWithElectricEngine() {
        UUID electricFuelTypeId = getUuidByName("engine_fuel_types", "ELECTRIC");
        UUID autoTransmissionTypeId = getUuidByName("transmission_types", "AUTOMATIC");

        UUID electricEngineId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO engines (id, fuel_type_id, displacement, horse_power, created_at, updated_at, removed) VALUES (?, ?, ?, ?, NOW(), NOW(), false)",
                electricEngineId, electricFuelTypeId, 0.0, 500.0
        );

        UUID electricTransmissionId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO transmissions (id, type_id, gears, created_at, updated_at, removed) VALUES (?, ?, ?, NOW(), NOW(), false)",
                electricTransmissionId, autoTransmissionTypeId, 1
        );

        CarModel model = new CarModel(
                testModelId.toString(),
                "iX",
                CarBrand.BMW,
                "i20"
        );

        Engine engine = new Engine(
                electricEngineId.toString(),
                EngineFuelType.ELECTRIC,
                EngineDisplacement.of(0.0),
                EnginePower.of(500.0)
        );

        Transmission transmission = new Transmission(
                TransmissionType.AUTOMATIC,
                1
        );
        transmission.setId(electricTransmissionId.toString());

        Price price = Price.of(8000000.0, "RUB");

        Car car = new Car(
                null,
                CarBrand.BMW,
                model,
                CarBody.COUPE,
                CarColor.WHITE,
                DriveType.FULL,
                engine,
                transmission,
                price
        );

        Car saved = carRepository.save(car);

        assertThat(saved.getCarId()).isNotNull();
        assertThat(saved.getEngine().getEngineFuelType()).isEqualTo(EngineFuelType.ELECTRIC);
    }

    @Test
    @Transactional
    @Rollback
    void shouldGetCarInfo() {
        Car car = createTestCar();
        Car saved = carRepository.save(car);
        String carInfo = saved.getCarInfo();
        assertThat(carInfo).contains("БМВ");
        assertThat(carInfo).contains("X5");
        assertThat(carInfo).contains("Бензин");
        assertThat(carInfo).contains("АКПП 8ст.");
        assertThat(carInfo).contains("Чёрный");
    }
}