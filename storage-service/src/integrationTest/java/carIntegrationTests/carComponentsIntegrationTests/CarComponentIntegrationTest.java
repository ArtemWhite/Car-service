package carIntegrationTests.carComponentsIntegrationTests;

import carIntegrationTests.BaseIntegrationTest;
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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class CarComponentIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ConfigurationRepository configurationRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UUID testComponentTypeId;
    private UUID testModelId;
    private CarModel testModel;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM component_compatible_models");
        jdbcTemplate.execute("DELETE FROM components");
        jdbcTemplate.execute("DELETE FROM car_models");
        jdbcTemplate.execute("DELETE FROM car_brands");

        testComponentTypeId = getUuidByName("component_types", "WHEELS");

        UUID testBrandId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO car_brands (id, name, display_name, country_made, created_at, updated_at, removed) " +
                        "VALUES (?, 'BMW', 'БМВ', 'Germany', NOW(), NOW(), false)",
                testBrandId
        );

        testModelId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO car_models (id, name, brand_id, generation, created_at, updated_at, removed) " +
                        "VALUES (?, 'X5', ?, 'G05', NOW(), NOW(), false)",
                testModelId, testBrandId
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
    void shouldCreateComponentWithCompatibleModels() {
        UUID componentId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO components (id, type_id, name, description, extra_charge, created_at, updated_at, removed) " +
                        "VALUES (?, ?, '20-inch Wheels', 'Sport wheels', 50000.0, NOW(), NOW(), false)",
                componentId, testComponentTypeId
        );

        jdbcTemplate.update(
                "INSERT INTO component_compatible_models (component_id, model_id) VALUES (?, ?)",
                componentId, testModelId
        );

        Set<CarModel> compatibleModels = new HashSet<>();
        compatibleModels.add(testModel);

        Component component = new Component(
                componentId.toString(),
                ComponentType.WHEELS,
                "20-inch Wheels",
                "Sport wheels",
                Price.of(50000.0, "RUB"),
                compatibleModels
        );

        assertThat(component.isCompatibleWith(testModel)).isTrue();
    }

    @Test
    @Transactional
    @Rollback
    void shouldNotBeCompatibleWithNonMatchingModel() {
        UUID otherModelId = UUID.randomUUID();
        UUID otherBrandId = UUID.randomUUID();

        jdbcTemplate.update(
                "INSERT INTO car_brands (id, name, display_name, country_made, created_at, updated_at, removed) " +
                        "VALUES (?, 'TOYOTA', 'Тойота', 'Japan', NOW(), NOW(), false)",
                otherBrandId
        );

        jdbcTemplate.update(
                "INSERT INTO car_models (id, name, brand_id, generation, created_at, updated_at, removed) " +
                        "VALUES (?, 'Camry', ?, 'XV70', NOW(), NOW(), false)",
                otherModelId, otherBrandId
        );

        CarModel otherModel = new CarModel(otherModelId.toString(), "Camry", CarBrand.TOYOTA, "XV70");

        UUID componentId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO components (id, type_id, name, description, extra_charge, created_at, updated_at, removed) " +
                        "VALUES (?, ?, '20-inch Wheels', 'Sport wheels', 50000.0, NOW(), NOW(), false)",
                componentId, testComponentTypeId
        );

        jdbcTemplate.update(
                "INSERT INTO component_compatible_models (component_id, model_id) VALUES (?, ?)",
                componentId, testModelId
        );

        Set<CarModel> compatibleModels = new HashSet<>();
        compatibleModels.add(testModel);

        Component component = new Component(
                componentId.toString(),
                ComponentType.WHEELS,
                "20-inch Wheels",
                "Sport wheels",
                Price.of(50000.0, "RUB"),
                compatibleModels
        );

        assertThat(component.isCompatibleWith(otherModel)).isFalse();
    }

    @Test
    @Transactional
    @Rollback
    void shouldCalculatePriceWithComponent() {
        Price basePrice = Price.of(2500000.0, "RUB");
        Price componentPrice = Price.of(50000.0, "RUB");
        Price total = basePrice.add(componentPrice);

        assertThat(total.getAmount().doubleValue()).isEqualTo(2550000.0);
    }
}