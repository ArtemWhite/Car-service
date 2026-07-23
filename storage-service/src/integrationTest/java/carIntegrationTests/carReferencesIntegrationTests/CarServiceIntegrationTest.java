package carIntegrationTests.carReferencesIntegrationTests;

import application.dtos.request.carRequest.CarFilterRequest;
import application.dtos.response.carResponse.CarResponse;
import application.services.carService.CarService;
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
class CarServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CarService carService;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UUID testBrandId;
    private UUID testModelId;
    private UUID testEngineId;
    private UUID testTransmissionId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM cars");
        jdbcTemplate.execute("DELETE FROM engines");
        jdbcTemplate.execute("DELETE FROM transmissions");
        jdbcTemplate.execute("DELETE FROM car_models");
        jdbcTemplate.execute("DELETE FROM car_brands");

        UUID fuelTypeId = getUuidByName("engine_fuel_types", "PETROL");
        UUID transmissionTypeId = getUuidByName("transmission_types", "AUTOMATIC");

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
                testEngineId, fuelTypeId
        );

        testTransmissionId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO transmissions (id, type_id, gears, created_at, updated_at, removed) VALUES (?, ?, 8, NOW(), NOW(), false)",
                testTransmissionId, transmissionTypeId
        );
    }

    private UUID getUuidByName(String tableName, String name) {
        String sql = String.format("SELECT id FROM %s WHERE name = ?", tableName);
        String idStr = jdbcTemplate.queryForObject(sql, String.class, name);
        return idStr != null ? UUID.fromString(idStr) : null;
    }

    private Car createAndSaveTestCar() {
        CarModel model = new CarModel(testModelId.toString(), "X5", CarBrand.BMW, "G05");
        Engine engine = new Engine(testEngineId.toString(), EngineFuelType.PETROL, EngineDisplacement.of(2.0), EnginePower.of(249.0));
        Transmission transmission = new Transmission(TransmissionType.AUTOMATIC, 8);
        transmission.setId(testTransmissionId.toString());
        Price price = Price.of(2500000.0, "RUB");

        Car car = new Car(null, CarBrand.BMW, model, CarBody.SEDAN, CarColor.BLACK, DriveType.FRONT, engine, transmission, price);
        car.markAsAvailable();
        return carRepository.save(car);
    }

    @Test
    @Transactional
    @Rollback
    void shouldGetCarById() {
        Car saved = createAndSaveTestCar();
        CarResponse response = carService.getCarById(saved.getCarId());

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(saved.getCarId());
        assertThat(response.getBrand()).isEqualTo("BMW");
        assertThat(response.getModel()).isEqualTo("X5");
    }

    @Test
    @Transactional
    @Rollback
    void shouldGetAvailableCars() {
        createAndSaveTestCar();
        createAndSaveTestCar();

        List<CarResponse> availableCars = carService.getAvailableCars();

        assertThat(availableCars).hasSize(2);
        assertThat(availableCars.get(0).getStatus()).isEqualTo("AVAILABLE");
    }

    @Test
    @Transactional
    @Rollback
    void shouldGetCarsWithFilters() {
        createAndSaveTestCar();

        CarFilterRequest filter = new CarFilterRequest();
        filter.setBrand("BMW");
        filter.setMinPrice(2000000.0);
        filter.setMaxPrice(3000000.0);

        List<CarResponse> filtered = carService.getCarsWithFilters(filter);

        assertThat(filtered).hasSize(1);
        assertThat(filtered.get(0).getBrand()).isEqualTo("BMW");
    }
}