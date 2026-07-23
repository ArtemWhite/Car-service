package carIntegrationTests.carSpecificIntegrationTests;

import carIntegrationTests.BaseIntegrationTest;
import domain.models.car.CarConfiguration;
import domain.models.car.CarModel;
import domain.models.car.Price;
import domain.models.car.componentModels.Component;
import domain.models.car.componentModels.ComponentType;
import domain.models.car.types.CarBrand;
import domain.repository.carRepository.ConfigurationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class CarConfigurationExtendedIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ConfigurationRepository configurationRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UUID testBrandId;
    private UUID testModelId;
    private UUID testComponentTypeId;
    private UUID testComponentId;
    private CarModel testModel;
    private Component testComponent;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM component_compatible_models");
        jdbcTemplate.execute("DELETE FROM components");
        jdbcTemplate.execute("DELETE FROM car_configurations");
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

        Set<CarModel> compatibleModels = new HashSet<>();
        compatibleModels.add(testModel);

        testComponent = new Component(
                testComponentId.toString(),
                ComponentType.WHEELS,
                "20-inch Wheels",
                "Sport wheels",
                Price.of(50000.0, "RUB"),
                compatibleModels
        );
    }

    private UUID getUuidByName(String tableName, String name) {
        String sql = String.format("SELECT id FROM %s WHERE name = ?", tableName);
        String idStr = jdbcTemplate.queryForObject(sql, String.class, name);
        return idStr != null ? UUID.fromString(idStr) : null;
    }

    private CarConfiguration createTestConfiguration(String name, double price) {
        Map<ComponentType, Component> baseComponents = new HashMap<>();
        baseComponents.put(ComponentType.WHEELS, testComponent);

        return new CarConfiguration(
                UUID.randomUUID().toString(),
                name,
                testModel,
                baseComponents,
                Price.of(price, "RUB")
        );
    }

    @Test
    @Transactional
    @Rollback
    void shouldUpdateConfiguration() {
        CarConfiguration config = createTestConfiguration("Sport Package", 2500000.0);
        CarConfiguration saved = configurationRepository.save(config);

        CarConfiguration updatedConfig = new CarConfiguration(
                saved.getId(),
                "Sport Package PLUS",
                testModel,
                saved.getBaseComponents(),
                Price.of(2700000.0, "RUB")
        );

        CarConfiguration updated = configurationRepository.save(updatedConfig);

        assertThat(updated.getName()).isEqualTo("Sport Package PLUS");
        assertThat(updated.getBasePrice().getAmount().doubleValue()).isEqualTo(2700000.0);
    }

    @Test
    @Transactional
    @Rollback
    void shouldDeleteConfiguration() {
        CarConfiguration config = createTestConfiguration("Sport Package", 2500000.0);
        CarConfiguration saved = configurationRepository.save(config);

        String configId = saved.getId();
        assertThat(configurationRepository.findById(configId)).isPresent();

        jdbcTemplate.update(
                "UPDATE car_configurations SET removed = true WHERE id = ?::uuid",
                UUID.fromString(configId)
        );

        assertThat(configurationRepository.findById(configId)).isEmpty();
    }

    @Test
    @Transactional
    @Rollback
    void shouldFindConfigurationsByPriceRange() {
        CarConfiguration cheap = createTestConfiguration("Base Package", 2000000.0);
        CarConfiguration medium = createTestConfiguration("Sport Package", 2500000.0);
        CarConfiguration expensive = createTestConfiguration("Luxury Package", 3000000.0);

        configurationRepository.save(cheap);
        configurationRepository.save(medium);
        configurationRepository.save(expensive);

        List<CarConfiguration> results = configurationRepository.findByPriceRange(2400000.0, 2600000.0);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Sport Package");
    }

    @Test
    @Transactional
    @Rollback
    void shouldFindBaseConfigurationForModel() {
        CarConfiguration config1 = createTestConfiguration("Base Package", 2000000.0);
        CarConfiguration config2 = createTestConfiguration("Sport Package", 2500000.0);

        configurationRepository.save(config1);
        configurationRepository.save(config2);

        Optional<CarConfiguration> base = configurationRepository.findBaseByModelId(testModelId.toString());

        assertThat(base).isPresent();
        assertThat(base.get().getName()).isEqualTo("Base Package");
    }
}