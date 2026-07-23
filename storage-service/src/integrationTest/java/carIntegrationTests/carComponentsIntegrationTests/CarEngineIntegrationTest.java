package carIntegrationTests.carComponentsIntegrationTests;

import carIntegrationTests.BaseIntegrationTest;
import domain.models.car.engine.Engine;
import domain.models.car.engine.EngineDisplacement;
import domain.models.car.engine.EngineFuelType;
import domain.models.car.engine.EnginePower;
import domain.repository.carRepository.CarRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class CarEngineIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Transactional
    @Rollback
    void shouldFindEngineByFuelTypePowerAndDisplacement() {
        UUID fuelTypeId = getUuidByName("engine_fuel_types", "PETROL");
        UUID engineId = UUID.randomUUID();

        jdbcTemplate.update(
                "INSERT INTO engines (id, fuel_type_id, displacement, horse_power, created_at, updated_at, removed) " +
                        "VALUES (?, ?, 2.0, 200.0, NOW(), NOW(), false)",
                engineId, fuelTypeId
        );

        var found = carRepository.findEngineByFuelTypePowerAndDisplacement(
                EngineFuelType.PETROL, 200.0, 2.0
        );

        assertThat(found).isPresent();
        assertThat(found.get().getEngineFuelType()).isEqualTo(EngineFuelType.PETROL);
        assertThat(found.get().getEnginePower().getHorsePower()).isEqualTo(200.0);
        assertThat(found.get().getEngineDisplacement().getLiters()).isEqualTo(2.0);
    }

    @Test
    @Transactional
    @Rollback
    void shouldCreateNewEngineIfNotFound() {
        var found = carRepository.findEngineByFuelTypePowerAndDisplacement(
                EngineFuelType.DIESEL, 300.0, 3.0
        );

        assertThat(found).isEmpty();

        Engine newEngine = new Engine(
                UUID.randomUUID().toString(),
                EngineFuelType.DIESEL,
                EngineDisplacement.of(3.0),
                EnginePower.of(300.0)
        );

        Engine saved = carRepository.saveEngine(newEngine);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEngineFuelType()).isEqualTo(EngineFuelType.DIESEL);
    }

    @Test
    @Transactional
    @Rollback
    void shouldNotCreateElectricEngineWithPositiveDisplacement() {
        org.junit.jupiter.api.Assertions.assertThrows(
                domain.exception.DomainValidationException.class,
                () -> new Engine(
                        UUID.randomUUID().toString(),
                        EngineFuelType.ELECTRIC,
                        EngineDisplacement.of(2.0),
                        EnginePower.of(500.0)
                )
        );
    }

    private UUID getUuidByName(String tableName, String name) {
        String sql = String.format("SELECT id FROM %s WHERE name = ?", tableName);
        String idStr = jdbcTemplate.queryForObject(sql, String.class, name);
        return idStr != null ? UUID.fromString(idStr) : null;
    }
}