package carIntegrationTests.carComponentsIntegrationTests;

import carIntegrationTests.BaseIntegrationTest;
import domain.models.car.Car;
import domain.models.car.CarConfiguration;
import domain.models.car.CarModel;
import domain.models.car.Price;
import domain.models.car.componentModels.Component;
import domain.models.car.componentModels.ComponentType;
import domain.models.car.engine.Engine;
import domain.models.car.engine.EngineDisplacement;
import domain.models.car.engine.EngineFuelType;
import domain.models.car.engine.EnginePower;
import domain.models.car.transmission.Transmission;
import domain.models.car.transmission.TransmissionType;
import domain.models.car.types.*;
import domain.repository.carRepository.CarRepository;
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
class CarConfigurationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private ConfigurationRepository configurationRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UUID testBrandId;
    private UUID testModelId;
    private UUID testEngineId;
    private UUID testTransmissionId;
    private UUID testFuelTypeId;
    private UUID testTransmissionTypeId;
    private UUID testComponentTypeId;
    private UUID testComponentId;
    private CarModel testModel;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM component_compatible_models");
        jdbcTemplate.execute("DELETE FROM components");
        jdbcTemplate.execute("DELETE FROM car_configurations");
        jdbcTemplate.execute("DELETE FROM cars");
        jdbcTemplate.execute("DELETE FROM engines");
        jdbcTemplate.execute("DELETE FROM transmissions");
        jdbcTemplate.execute("DELETE FROM car_models");
        jdbcTemplate.execute("DELETE FROM car_brands");

        testFuelTypeId = getUuidByName("engine_fuel_types", "PETROL");
        testTransmissionTypeId = getUuidByName("transmission_types", "AUTOMATIC");
        testComponentTypeId = getUuidByName("component_types", "WHEELS");

        testBrandId = UUID.randomUUID();
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

        testEngineId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO engines (id, fuel_type_id, displacement, horse_power, created_at, updated_at, removed) " +
                        "VALUES (?, ?, 2.0, 249.0, NOW(), NOW(), false)",
                testEngineId, testFuelTypeId
        );

        testTransmissionId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO transmissions (id, type_id, gears, created_at, updated_at, removed) " +
                        "VALUES (?, ?, 8, NOW(), NOW(), false)",
                testTransmissionId, testTransmissionTypeId
        );

        testComponentId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO components (id, type_id, name, description, extra_charge, created_at, updated_at, removed) " +
                        "VALUES (?, ?, '20-inch Wheels', 'Sport wheels', 50000.0, NOW(), NOW(), false)",
                testComponentId, testComponentTypeId
        );

        jdbcTemplate.update(
                "INSERT INTO component_compatible_models (component_id, model_id) VALUES (?, ?)",
                testComponentId, testModelId
        );

        testModel = new CarModel(
                testModelId.toString(),
                "X5",
                CarBrand.BMW,
                "G05"
        );
    }

    private UUID getUuidByName(String tableName, String name) {
        String sql = String.format("SELECT id FROM %s WHERE name = ?", tableName);
        String idStr = jdbcTemplate.queryForObject(sql, String.class, name);
        return idStr != null ? UUID.fromString(idStr) : null;
    }

    private Car createTestCar() {
        Engine engine = new Engine(
                testEngineId.toString(),
                EngineFuelType.PETROL,
                EngineDisplacement.of(2.0),
                EnginePower.of(249.0)
        );

        Transmission transmission = new Transmission(TransmissionType.AUTOMATIC, 8);
        transmission.setId(testTransmissionId.toString());

        Price price = Price.of(2500000.0, "RUB");

        Car car = new Car(
                null,
                CarBrand.BMW,
                testModel,
                CarBody.SEDAN,
                CarColor.BLACK,
                DriveType.FRONT,
                engine,
                transmission,
                price
        );
        car.markAsAvailable();
        return car;
    }

    private Component createAndSaveTestComponent() {
        Set<CarModel> compatibleModels = new HashSet<>();
        compatibleModels.add(testModel);

        Component component = new Component(
                testComponentId.toString(),
                ComponentType.WHEELS,
                "20-inch Wheels",
                "Sport wheels",
                Price.of(50000.0, "RUB"),
                compatibleModels
        );
        return component;
    }

    @Test
    @Transactional
    @Rollback
    void shouldFindConfigurationsByModelId() {
        Component component = createAndSaveTestComponent();

        Map<ComponentType, Component> baseComponents = new HashMap<>();
        baseComponents.put(ComponentType.WHEELS, component);

        CarConfiguration config1 = new CarConfiguration(
                UUID.randomUUID().toString(),
                "Sport Package",
                testModel,
                baseComponents,
                Price.of(2500000.0, "RUB")
        );

        CarConfiguration config2 = new CarConfiguration(
                UUID.randomUUID().toString(),
                "Luxury Package",
                testModel,
                baseComponents,
                Price.of(3000000.0, "RUB")
        );

        CarConfiguration savedConfig1 = configurationRepository.save(config1);
        CarConfiguration savedConfig2 = configurationRepository.save(config2);

        List<CarConfiguration> configs = configurationRepository.findByModelId(testModelId.toString());

        assertThat(configs).hasSize(2);
        assertThat(configs).extracting("name")
                .containsExactlyInAnyOrder("Sport Package", "Luxury Package");

        assertThat(configs).extracting("id")
                .containsExactlyInAnyOrder(savedConfig1.getId(), savedConfig2.getId());
    }

    @Test
    @Transactional
    @Rollback
    void shouldApplyConfigurationToCar() {
        Car car = createTestCar();
        Car savedCar = carRepository.save(car);

        assertThat(savedCar.getConfiguration()).isNull();

        Component component = createAndSaveTestComponent();

        Map<ComponentType, Component> baseComponents = new HashMap<>();
        baseComponents.put(ComponentType.WHEELS, component);

        CarConfiguration configuration = new CarConfiguration(
                UUID.randomUUID().toString(),
                "Sport Package",
                testModel,
                baseComponents,
                Price.of(2500000.0, "RUB")
        );

        CarConfiguration savedConfig = configurationRepository.save(configuration);
        assertThat(savedConfig.getId()).isNotNull();

        savedCar.applyConfiguration(savedConfig);
        Car updatedCar = carRepository.save(savedCar);

        assertThat(updatedCar.getConfiguration()).isNotNull();
        assertThat(updatedCar.getConfiguration().getId()).isEqualTo(savedConfig.getId());
        assertThat(updatedCar.getConfiguration().getName()).isEqualTo("Sport Package");

        assertThat(updatedCar.getPrice().getAmount().doubleValue()).isEqualTo(2500000.0);
    }

    @Test
    @Transactional
    @Rollback
    void shouldFailApplyConfigurationForWrongModel() {
        UUID wrongModelId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO car_models (id, name, brand_id, generation, created_at, updated_at, removed) " +
                        "VALUES (?, 'X3', ?, 'G01', NOW(), NOW(), false)",
                wrongModelId, testBrandId
        );

        CarModel wrongModel = new CarModel(
                wrongModelId.toString(),
                "X3",
                CarBrand.BMW,
                "G01"
        );

        Car car = createTestCar();
        Car savedCar = carRepository.save(car);

        Component component = createAndSaveTestComponent();
        Map<ComponentType, Component> baseComponents = new HashMap<>();
        baseComponents.put(ComponentType.WHEELS, component);

        CarConfiguration configuration = new CarConfiguration(
                UUID.randomUUID().toString(),
                "Sport Package",
                wrongModel,
                baseComponents,
                Price.of(2500000.0, "RUB")
        );

        CarConfiguration savedConfig = configurationRepository.save(configuration);

        org.junit.jupiter.api.Assertions.assertThrows(
                domain.exception.IncompatibleComponentException.class,
                () -> savedCar.applyConfiguration(savedConfig)
        );
    }
}