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

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class CarFilterIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UUID testBrandId;
    private UUID testModelIdX5;
    private UUID testModelIdX3;
    private UUID testModelIdCamry;
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

        testModelIdX5 = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO car_models (id, name, brand_id, generation, created_at, updated_at, removed) VALUES (?, 'X5', ?, 'G05', NOW(), NOW(), false)",
                testModelIdX5, testBrandId
        );

        testModelIdX3 = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO car_models (id, name, brand_id, generation, created_at, updated_at, removed) VALUES (?, 'X3', ?, 'G01', NOW(), NOW(), false)",
                testModelIdX3, testBrandId
        );

        UUID toyotaBrandId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO car_brands (id, name, display_name, country_made, created_at, updated_at, removed) VALUES (?, 'TOYOTA', 'Тойота', 'Japan', NOW(), NOW(), false)",
                toyotaBrandId
        );

        testModelIdCamry = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO car_models (id, name, brand_id, generation, created_at, updated_at, removed) VALUES (?, 'Camry', ?, 'XV70', NOW(), NOW(), false)",
                testModelIdCamry, toyotaBrandId
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

    private Car createCar(CarBrand brand, CarModel model, CarBody body, CarColor color, DriveType driveType, double price) {
        Engine engine = new Engine(testEngineId.toString(), EngineFuelType.PETROL, EngineDisplacement.of(2.0), EnginePower.of(249.0));
        Transmission transmission = new Transmission(TransmissionType.AUTOMATIC, 8);
        transmission.setId(testTransmissionId.toString());
        Price carPrice = Price.of(price, "RUB");

        Car car = new Car(null, brand, model, body, color, driveType, engine, transmission, carPrice);
        car.markAsAvailable();
        return car;
    }

    @Test
    @Transactional
    @Rollback
    void shouldFilterByBrand() {
        CarModel bmwModel = new CarModel(testModelIdX5.toString(), "X5", CarBrand.BMW, "G05");
        Car bmw = createCar(CarBrand.BMW, bmwModel, CarBody.SEDAN, CarColor.BLACK, DriveType.FRONT, 2500000.0);
        carRepository.save(bmw);

        CarModel toyotaModel = new CarModel(testModelIdCamry.toString(), "Camry", CarBrand.TOYOTA, "XV70");
        Car toyota = createCar(CarBrand.TOYOTA, toyotaModel, CarBody.SEDAN, CarColor.BLACK, DriveType.FRONT, 2000000.0);
        carRepository.save(toyota);

        List<Car> bmwCars = carRepository.findByBrand(CarBrand.BMW);
        List<Car> toyotaCars = carRepository.findByBrand(CarBrand.TOYOTA);

        assertThat(bmwCars).hasSize(1);
        assertThat(bmwCars.get(0).getBrand()).isEqualTo(CarBrand.BMW);
        assertThat(toyotaCars).hasSize(1);
        assertThat(toyotaCars.get(0).getBrand()).isEqualTo(CarBrand.TOYOTA);
    }

    @Test
    @Transactional
    @Rollback
    void shouldFilterByModel() {
        CarModel x5Model = new CarModel(testModelIdX5.toString(), "X5", CarBrand.BMW, "G05");
        Car x5 = createCar(CarBrand.BMW, x5Model, CarBody.SEDAN, CarColor.BLACK, DriveType.FRONT, 2500000.0);
        carRepository.save(x5);

        CarModel x3Model = new CarModel(testModelIdX3.toString(), "X3", CarBrand.BMW, "G01");
        Car x3 = createCar(CarBrand.BMW, x3Model, CarBody.COUPE, CarColor.WHITE, DriveType.FULL, 3000000.0);
        carRepository.save(x3);

        List<Car> x5Cars = carRepository.findByModel(x5Model);
        List<Car> x3Cars = carRepository.findByModel(x3Model);

        assertThat(x5Cars).hasSize(1);
        assertThat(x5Cars.get(0).getModel().getName()).isEqualTo("X5");
        assertThat(x3Cars).hasSize(1);
        assertThat(x3Cars.get(0).getModel().getName()).isEqualTo("X3");
    }

    @Test
    @Transactional
    @Rollback
    void shouldFilterByPriceRange() {
        CarModel bmwModel = new CarModel(testModelIdX5.toString(), "X5", CarBrand.BMW, "G05");
        Car cheap = createCar(CarBrand.BMW, bmwModel, CarBody.SEDAN, CarColor.BLACK, DriveType.FRONT, 1500000.0);
        Car medium = createCar(CarBrand.BMW, bmwModel, CarBody.SEDAN, CarColor.BLACK, DriveType.FRONT, 2500000.0);
        Car expensive = createCar(CarBrand.BMW, bmwModel, CarBody.SEDAN, CarColor.BLACK, DriveType.FRONT, 3500000.0);
        carRepository.save(cheap);
        carRepository.save(medium);
        carRepository.save(expensive);

        Price minPrice = Price.of(2000000.0, "RUB");
        Price maxPrice = Price.of(3000000.0, "RUB");

        List<Car> carsInRange = carRepository.findByPriceRange(minPrice, maxPrice);

        assertThat(carsInRange).hasSize(1);
        assertThat(carsInRange.get(0).getPrice().getAmount().doubleValue()).isEqualTo(2500000.0);
    }

    @Test
    @Transactional
    @Rollback
    void shouldFilterByMultipleCriteria() {
        CarModel x5Model = new CarModel(testModelIdX5.toString(), "X5", CarBrand.BMW, "G05");
        Car bmwBlack = createCar(CarBrand.BMW, x5Model, CarBody.SEDAN, CarColor.BLACK, DriveType.FRONT, 2500000.0);
        Car bmwWhite = createCar(CarBrand.BMW, x5Model, CarBody.SEDAN, CarColor.WHITE, DriveType.FRONT, 2500000.0);
        carRepository.save(bmwBlack);
        carRepository.save(bmwWhite);

        List<Car> blackBmw = carRepository.findCarsByFilters(
                CarBrand.BMW, x5Model, null, CarColor.BLACK, null, null, null
        );

        assertThat(blackBmw).hasSize(1);
        assertThat(blackBmw.get(0).getColor()).isEqualTo(CarColor.BLACK);
    }

    @Test
    @Transactional
    @Rollback
    void shouldReturnEmptyListWhenNoCarsMatchFilter() {
        CarModel x5Model = new CarModel(testModelIdX5.toString(), "X5", CarBrand.BMW, "G05");
        Car bmw = createCar(CarBrand.BMW, x5Model, CarBody.SEDAN, CarColor.BLACK, DriveType.FRONT, 2500000.0);
        carRepository.save(bmw);

        List<Car> audiCars = carRepository.findByBrand(CarBrand.AUDI);

        assertThat(audiCars).isEmpty();
    }
}