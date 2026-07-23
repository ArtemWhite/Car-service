package carIntegrationTests.carReferencesIntegrationTests;

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

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class CarInfoFormattingIntegrationTest extends BaseIntegrationTest {

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

    private Car createTestCar(CarColor color) {
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

        Transmission transmission = new Transmission(TransmissionType.AUTOMATIC, 8);
        transmission.setId(testTransmissionId.toString());

        Price price = Price.of(2500000.0, "RUB");

        return new Car(
                null,
                CarBrand.BMW,
                model,
                CarBody.SEDAN,
                color,
                DriveType.FRONT,
                engine,
                transmission,
                price
        );
    }

    @Test
    @Transactional
    @Rollback
    void shouldFormatCarInfoWithAllDetails() {
        Car car = createTestCar(CarColor.BLACK);
        Car saved = carRepository.save(car);

        String carInfo = saved.getCarInfo();

        assertThat(carInfo).contains("БМВ");
        assertThat(carInfo).contains("X5");
        assertThat(carInfo).contains("Бензин");
        assertThat(carInfo).contains("2,0 л");
        assertThat(carInfo).contains("249,0 л.с.");
        assertThat(carInfo).contains("АКПП 8ст.");
        assertThat(carInfo).contains("Чёрный");
    }

    @Test
    @Transactional
    @Rollback
    void shouldFormatCarInfoForElectricCar() {
        UUID electricFuelTypeId = getUuidByName("engine_fuel_types", "ELECTRIC");
        UUID autoTransmissionTypeId = getUuidByName("transmission_types", "AUTOMATIC");

        UUID electricEngineId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO engines (id, fuel_type_id, displacement, horse_power, created_at, updated_at, removed) " +
                        "VALUES (?, ?, ?, ?, NOW(), NOW(), false)",
                electricEngineId, electricFuelTypeId, 0.0, 500.0
        );

        UUID electricTransmissionId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO transmissions (id, type_id, gears, created_at, updated_at, removed) " +
                        "VALUES (?, ?, ?, NOW(), NOW(), false)",
                electricTransmissionId, autoTransmissionTypeId, 1
        );

        UUID electricModelId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO car_models (id, name, brand_id, generation, created_at, updated_at, removed) " +
                        "VALUES (?, 'iX', ?, 'i20', NOW(), NOW(), false)",
                electricModelId, testBrandId
        );

        CarModel model = new CarModel(
                electricModelId.toString(),
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

        Transmission transmission = new Transmission(TransmissionType.AUTOMATIC, 1);
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
        String carInfo = saved.getCarInfo();

        assertThat(carInfo).contains("БМВ");
        assertThat(carInfo).contains("iX");
        assertThat(carInfo).contains("Электро");
        assertThat(carInfo).contains("500");
        assertThat(carInfo).contains("л.с.");

        assertThat(carInfo).doesNotContainPattern("\\d[.,]\\d\\sл[^.]");
        assertThat(carInfo).doesNotContain("л,");
        assertThat(carInfo).doesNotContain("л ");
    }

    @Test
    @Transactional
    @Rollback
    void shouldFormatCarInfoForDieselCar() {
        UUID dieselFuelTypeId = getUuidByName("engine_fuel_types", "DIESEL");

        UUID dieselEngineId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO engines (id, fuel_type_id, displacement, horse_power, created_at, updated_at, removed) VALUES (?, ?, ?, ?, NOW(), NOW(), false)",
                dieselEngineId, dieselFuelTypeId, 3.0, 300.0
        );

        Engine engine = new Engine(
                dieselEngineId.toString(),
                EngineFuelType.DIESEL,
                EngineDisplacement.of(3.0),
                EnginePower.of(300.0)
        );

        CarModel model = new CarModel(
                testModelId.toString(),
                "X5",
                CarBrand.BMW,
                "G05"
        );

        Transmission transmission = new Transmission(TransmissionType.AUTOMATIC, 8);
        transmission.setId(testTransmissionId.toString());

        Price price = Price.of(3500000.0, "RUB");

        Car car = new Car(
                null,
                CarBrand.BMW,
                model,
                CarBody.COUPE,
                CarColor.BLACK,
                DriveType.FULL,
                engine,
                transmission,
                price
        );

        Car saved = carRepository.save(car);
        String carInfo = saved.getCarInfo();

        assertThat(carInfo).contains("БМВ");
        assertThat(carInfo).contains("X5");
        assertThat(carInfo).contains("Дизель");
        assertThat(carInfo).contains("3,0 л");
        assertThat(carInfo).contains("300,0 л.с.");
    }

    @Test
    @Transactional
    @Rollback
    void shouldFormatCarInfoWithRedColor() {
        Car car = createTestCar(CarColor.RED);
        Car saved = carRepository.save(car);

        String carInfo = saved.getCarInfo();

        assertThat(carInfo).contains("Красный");
        assertThat(carInfo).doesNotContain("Чёрный");
    }

    @Test
    @Transactional
    @Rollback
    void shouldFormatCarInfoWithWhiteColor() {
        Car car = createTestCar(CarColor.WHITE);
        Car saved = carRepository.save(car);

        String carInfo = saved.getCarInfo();

        assertThat(carInfo).contains("Белый");
    }
}