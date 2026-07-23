package carIntegrationTests.carComponentsIntegrationTests;

import carIntegrationTests.BaseIntegrationTest;
import domain.models.car.CarModel;

import domain.models.car.types.CarBrand;
import infrastructure.jpaRepository.carJpaRepositories.configurationCarJpaRepositories.componentJpaRepositories.ComponentJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;


import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class CarComponentExtendedIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ComponentJpaRepository componentRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UUID testBrandId;
    private UUID testModelId;
    private UUID testComponentTypeId;
    private UUID testComponentId;
    private CarModel testModel;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM component_compatible_models");
        jdbcTemplate.execute("DELETE FROM components");
        jdbcTemplate.execute("DELETE FROM car_models");
        jdbcTemplate.execute("DELETE FROM car_brands");

        testComponentTypeId = getUuidByName("component_types", "WHEELS");

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

        testComponentId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO components (id, type_id, name, description, extra_charge, created_at, updated_at, removed) VALUES (?, ?, '20-inch Wheels', 'Sport wheels', 50000.0, NOW(), NOW(), false)",
                testComponentId, testComponentTypeId
        );

        jdbcTemplate.update(
                "INSERT INTO component_compatible_models (component_id, model_id) VALUES (?, ?)",
                testComponentId, testModelId
        );

        testModel = new CarModel(testModelId.toString(), "X5", CarBrand.BMW, "G05");
    }

    private UUID getUuidByName(String tableName, String name) {
        String sql = String.format("SELECT id FROM %s WHERE name = ?", tableName);
        String idStr = jdbcTemplate.queryForObject(sql, String.class, name);
        return idStr != null ? UUID.fromString(idStr) : null;
    }

    @Test
    @Transactional
    @Rollback
    void shouldUpdateComponent() {
        jdbcTemplate.update(
                "UPDATE components SET name = '22-inch Wheels', extra_charge = 75000.0 WHERE id = ?::uuid",
                testComponentId
        );

        String name = jdbcTemplate.queryForObject(
                "SELECT name FROM components WHERE id = ?::uuid", String.class, testComponentId
        );
        Double extraCharge = jdbcTemplate.queryForObject(
                "SELECT extra_charge FROM components WHERE id = ?::uuid", Double.class, testComponentId
        );

        assertThat(name).isEqualTo("22-inch Wheels");
        assertThat(extraCharge).isEqualTo(75000.0);
    }

    @Test
    @Transactional
    @Rollback
    void shouldDeleteComponent() {
        jdbcTemplate.update(
                "UPDATE components SET removed = true WHERE id = ?::uuid",
                testComponentId
        );

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM components WHERE id = ?::uuid AND removed = false",
                Integer.class, testComponentId
        );

        assertThat(count).isZero();
    }

    @Test
    @Transactional
    @Rollback
    void shouldFindComponentsByType() {
        UUID otherTypeId = getUuidByName("component_types", "AUDIO");
        UUID otherComponentId = UUID.randomUUID();

        jdbcTemplate.update(
                "INSERT INTO components (id, type_id, name, description, extra_charge, created_at, updated_at, removed) " +
                        "VALUES (?, ?, 'Premium Sound', 'Harman Kardon', 100000.0, NOW(), NOW(), false)",
                otherComponentId, otherTypeId
        );

        int wheelsCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM components WHERE type_id = ?::uuid AND removed = false",
                Integer.class, testComponentTypeId
        );

        assertThat(wheelsCount).isGreaterThan(0);
    }

    @Test
    @Transactional
    @Rollback
    void shouldFindComponentsCompatibleWithModel() {
        int compatibleCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM component_compatible_models WHERE model_id = ?::uuid",
                Integer.class, testModelId
        );

        assertThat(compatibleCount).isGreaterThan(0);
    }
}