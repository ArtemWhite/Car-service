package carIntegrationTests.carSpecificIntegrationTests;

import carIntegrationTests.BaseIntegrationTest;
import domain.models.car.engine.EngineFuelType;
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
class CarRepositoryMethodIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String carId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM cars");
        jdbcTemplate.execute("DELETE FROM engines");
        jdbcTemplate.execute("DELETE FROM transmissions");
        jdbcTemplate.execute("DELETE FROM car_models");
        jdbcTemplate.execute("DELETE FROM car_brands");

        UUID fuelTypeId = getUuidByName("engine_fuel_types", "PETROL");
        UUID transmissionTypeId = getUuidByName("transmission_types", "AUTOMATIC");
        UUID carStatusId = getUuidByName("car_statuses", "AVAILABLE");

        UUID brandId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO car_brands (id, name, display_name, country_made, created_at, updated_at, removed) VALUES (?, 'BMW', 'БМВ', 'Germany', NOW(), NOW(), false)",
                brandId
        );

        UUID modelId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO car_models (id, name, brand_id, generation, created_at, updated_at, removed) VALUES (?, 'X5', ?, 'G05', NOW(), NOW(), false)",
                modelId, brandId
        );

        UUID engineId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO engines (id, fuel_type_id, displacement, horse_power, created_at, updated_at, removed) VALUES (?, ?, 2.0, 249.0, NOW(), NOW(), false)",
                engineId, fuelTypeId
        );

        UUID transmissionId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO transmissions (id, type_id, gears, created_at, updated_at, removed) VALUES (?, ?, 8, NOW(), NOW(), false)",
                transmissionId, transmissionTypeId
        );

        // Создаём автомобиль напрямую через SQL
        carId = UUID.randomUUID().toString();
        jdbcTemplate.update(
                "INSERT INTO cars (id, brand_id, model_id, body_id, color_id, drive_type_id, engine_id, transmission_id, price, status_id, created_at, updated_at, removed) " +
                        "VALUES (?::uuid, ?::uuid, ?::uuid, " +
                        "(SELECT id FROM car_bodies WHERE name = 'SEDAN' LIMIT 1), " +
                        "(SELECT id FROM car_colors WHERE name = 'BLACK' LIMIT 1), " +
                        "(SELECT id FROM drive_types WHERE name = 'FRONT' LIMIT 1), " +
                        "?::uuid, ?::uuid, 2500000.0, ?::uuid, NOW(), NOW(), false)",
                UUID.fromString(carId), brandId, modelId, engineId, transmissionId, carStatusId
        );
    }

    private UUID getUuidByName(String tableName, String name) {
        String sql = String.format("SELECT id FROM %s WHERE name = ?", tableName);
        String idStr = jdbcTemplate.queryForObject(sql, String.class, name);
        return idStr != null ? UUID.fromString(idStr) : null;
    }

    @Test
    @Transactional
    @Rollback
    void shouldCheckIfCarExistsById() {
        boolean exists = carRepository.existsById(carId);
        assertThat(exists).isTrue();

        boolean notExists = carRepository.existsById(UUID.randomUUID().toString());
        assertThat(notExists).isFalse();
    }

    @Test
    @Transactional
    @Rollback
    void shouldCountCarsByStatus() {
        long availableCount = carRepository.countByStatus(CarStatus.AVAILABLE);
        assertThat(availableCount).isGreaterThan(0);

        long soldCount = carRepository.countByStatus(CarStatus.SOLD);
        assertThat(soldCount).isEqualTo(0);
    }

    @Test
    @Transactional
    @Rollback
    void shouldCountAvailableCars() {
        long count = carRepository.countAvailableCars();
        assertThat(count).isGreaterThan(0);
    }

    @Test
    @Transactional
    @Rollback
    void shouldCountCarsByBrand() {
        var counts = carRepository.countCarsByBrand();
        assertThat(counts).isNotEmpty();
    }

    @Test
    @Transactional
    @Rollback
    void shouldFindCarsByDriveType() {
        var cars = carRepository.findByDriveType(DriveType.FRONT.name());
        assertThat(cars).isNotEmpty();
    }

    @Test
    @Transactional
    @Rollback
    void shouldFindCarsByColor() {
        var cars = carRepository.findByColor(CarColor.BLACK.name());
        assertThat(cars).isNotEmpty();
    }

    @Test
    @Transactional
    @Rollback
    void shouldFindCarsByBodyType() {
        var cars = carRepository.findByBody(CarBody.SEDAN.name());
        assertThat(cars).isNotEmpty();
    }

    @Test
    @Transactional
    @Rollback
    void shouldFindCarsByBrandAndModel() {
        var cars = carRepository.findByBrandAndModel("BMW", "X5");
        assertThat(cars).isNotEmpty();
    }

    @Test
    @Transactional
    @Rollback
    void shouldFindCarsByEngineFuelType() {
        var cars = carRepository.findByEngineFuelType(EngineFuelType.PETROL.name());
        assertThat(cars).isNotEmpty();
    }

    @Test
    @Transactional
    @Rollback
    void shouldFindCarsByEnginePowerRange() {
        var cars = carRepository.findByEnginePowerRange(200.0, 300.0);
        assertThat(cars).isNotEmpty();
    }

    @Test
    @Transactional
    @Rollback
    void shouldUpdateCarPrice() {
        int updated = jdbcTemplate.update(
                "UPDATE cars SET price = 3000000.0 WHERE id = ?::uuid",
                UUID.fromString(carId)
        );

        assertThat(updated).isEqualTo(1);

        Double price = jdbcTemplate.queryForObject(
                "SELECT price FROM cars WHERE id = ?::uuid", Double.class, UUID.fromString(carId)
        );
        assertThat(price).isEqualTo(3000000.0);
    }

    @Test
    @Transactional
    @Rollback
    void shouldSoftDeleteCar() {
        int deleted = jdbcTemplate.update(
                "UPDATE cars SET removed = true WHERE id = ?::uuid",
                UUID.fromString(carId)
        );

        assertThat(deleted).isEqualTo(1);

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM cars WHERE id = ?::uuid AND removed = false",
                Integer.class, UUID.fromString(carId)
        );
        assertThat(count).isZero();
    }
}