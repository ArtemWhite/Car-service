package testdriveRequestIntegrationTests.testDriveRequestMainIntegrationTests;

import dealerShipOrder.BaseIntegrationTest;
import domain.models.car.types.CarBrand;
import domain.models.car.types.CarStatus;
import domain.repository.carRepository.CarRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.*;

@AutoConfigureMockMvc
@Transactional
public abstract class TestDriveBaseIntegrationTest extends BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected EntityManager entityManager;

    @Autowired
    protected CarRepository carRepository;

    protected String adminId;
    protected String managerId;
    protected String clientId;
    protected String testCarId;
    protected String testCarModelId;

    @BeforeEach
    void baseSetUp() throws Exception {
        createReferenceData();
        createTestUsers();

        entityManager.flush();
        entityManager.clear();

        testCarModelId = createTestCarModel();

        Integer modelExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM car_models WHERE id = ?::uuid AND removed = false",
                Integer.class, UUID.fromString(testCarModelId)
        );
        if (modelExists == 0) {
            throw new RuntimeException("Failed to create car model! Model not found after creation.");
        }
        System.out.println("Car model created successfully: " + testCarModelId);

        testCarId = createTestCarForTestDrive();
    }

    private void createReferenceData() {
        String[] testDriveStatuses = {"PENDING", "CONFIRMED", "COMPLETED", "CANCELLED", "NO_SHOW"};
        for (String status : testDriveStatuses) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM test_drive_statuses WHERE name = ?", Integer.class, status);
            if (count == 0) {
                jdbcTemplate.update(
                        "INSERT INTO test_drive_statuses (id, name, display_name, created_at, updated_at, removed) " +
                                "VALUES (?::uuid, ?, ?, NOW(), NOW(), false)",
                        UUID.randomUUID(), status, status);
                System.out.println("Created test_drive_status: " + status);
            }
        }

        for (CarStatus carStatus : CarStatus.values()) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM car_statuses WHERE name = ?", Integer.class, carStatus.name());
            if (count == 0) {
                jdbcTemplate.update(
                        "INSERT INTO car_statuses (id, name, display_name, created_at, updated_at, removed) " +
                                "VALUES (?::uuid, ?, ?, NOW(), NOW(), false)",
                        UUID.randomUUID(), carStatus.name(), carStatus.getDisplayName());
                System.out.println("Created car_status: " + carStatus.name());
            }
        }

        for (CarBrand brand : CarBrand.values()) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM car_brands WHERE name = ?", Integer.class, brand.name());
            if (count == 0) {
                jdbcTemplate.update(
                        "INSERT INTO car_brands (id, name, display_name, country_made, created_at, updated_at, removed) " +
                                "VALUES (?::uuid, ?, ?, ?, NOW(), NOW(), false)",
                        UUID.randomUUID(), brand.name(), brand.getDisplayName(), brand.getCountryMade());
                System.out.println("Created car_brand: " + brand.name());
            }
        }

        String[] userTypes = {"CLIENT", "MANAGER", "SYSTEM_ADMIN", "WAREHOUSE_ADMIN"};
        for (String type : userTypes) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM user_types WHERE name = ?", Integer.class, type);
            if (count == 0) {
                jdbcTemplate.update(
                        "INSERT INTO user_types (id, name, display_name, created_at, updated_at, removed) " +
                                "VALUES (?::uuid, ?, ?, NOW(), NOW(), false)",
                        UUID.randomUUID(), type, type);
            }
        }

        Integer managerPosCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM manager_positions WHERE name = 'SALES_MANAGER'", Integer.class);
        if (managerPosCount == 0) {
            jdbcTemplate.update(
                    "INSERT INTO manager_positions (id, name, display_name, max_concurrent_orders, max_concurrent_test_drives, created_at, updated_at, removed) " +
                            "VALUES (?::uuid, 'SALES_MANAGER', 'Менеджер по продажам', 10, 5, NOW(), NOW(), false)",
                    UUID.randomUUID());
        }

        Integer adminLevelCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM admin_levels WHERE name = 'SUPER_ADMIN'", Integer.class);
        if (adminLevelCount == 0) {
            jdbcTemplate.update(
                    "INSERT INTO admin_levels (id, name, display_name, level, created_at, updated_at, removed) " +
                            "VALUES (?::uuid, 'SUPER_ADMIN', 'Супер администратор', 100, NOW(), NOW(), false)",
                    UUID.randomUUID());
        }
    }

    protected void createTestUsers() {
        adminId = UUID.randomUUID().toString();
        createUser(adminId, "SYSTEM_ADMIN");
        createSystemAdmin(adminId);

        managerId = UUID.randomUUID().toString();
        createUser(managerId, "MANAGER");
        createManager(managerId);

        clientId = UUID.randomUUID().toString();
        createUser(clientId, "CLIENT");
        createClient(clientId);
    }

    protected void createUser(String id, String type) {
        jdbcTemplate.update(
                "INSERT INTO users (id, first_name, last_name, email, phone, password_hash, status_id, user_type_id, created_at, updated_at, removed) " +
                        "VALUES (?::uuid, 'Test', 'User', ?, '1234567890', 'hash', " +
                        "(SELECT id FROM user_statuses WHERE name = 'ACTIVE'), " +
                        "(SELECT id FROM user_types WHERE name = ?), NOW(), NOW(), false)",
                UUID.fromString(id), id + "@test.com", type);
    }

    protected void createClient(String id) {
        jdbcTemplate.update(
                "INSERT INTO clients (user_id, preferred_contact_method, newsletter_subscribed) " +
                        "VALUES (?::uuid, 'EMAIL', false)", UUID.fromString(id));
    }

    protected void createManager(String id) {
        jdbcTemplate.update(
                "INSERT INTO managers (user_id, position_id, max_concurrent_orders, max_concurrent_test_drives, available) " +
                        "VALUES (?::uuid, (SELECT id FROM manager_positions LIMIT 1), 10, 5, true)",
                UUID.fromString(id));
    }

    protected void createSystemAdmin(String id) {
        jdbcTemplate.update(
                "INSERT INTO system_admins (user_id, admin_level_id, last_login_at) " +
                        "VALUES (?::uuid, (SELECT id FROM admin_levels WHERE name = 'SUPER_ADMIN'), NOW())",
                UUID.fromString(id));
    }

    protected String createTestCarModel() throws Exception {
        String brandName = "BMW";

        UUID brandId = jdbcTemplate.queryForObject(
                "SELECT id FROM car_brands WHERE name = ?", UUID.class, brandName);

        if (brandId == null) {
            brandId = UUID.randomUUID();
            jdbcTemplate.update(
                    "INSERT INTO car_brands (id, name, display_name, country_made, created_at, updated_at, removed) " +
                            "VALUES (?::uuid, ?, ?, 'Germany', NOW(), NOW(), false)",
                    brandId, brandName, brandName);
        }

        String uniqueModelName = "TEST_MODEL_" + UUID.randomUUID().toString().substring(0, 8);
        UUID modelId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO car_models (id, name, brand_id, generation, created_at, updated_at, removed) " +
                        "VALUES (?::uuid, ?, ?::uuid, 'G05', NOW(), NOW(), false)",
                modelId, uniqueModelName, brandId);

        entityManager.flush();
        entityManager.clear();

        return modelId.toString();
    }

    protected String createTestCarForTestDrive() throws Exception {
        String brandName = "BMW";

        UUID brandId = jdbcTemplate.queryForObject(
                "SELECT id FROM car_brands WHERE name = ?", UUID.class, brandName);

        if (brandId == null) {
            brandId = UUID.randomUUID();
            jdbcTemplate.update(
                    "INSERT INTO car_brands (id, name, display_name, country_made, created_at, updated_at, removed) " +
                            "VALUES (?::uuid, ?, ?, 'Germany', NOW(), NOW(), false)",
                    brandId, brandName, brandName);
        }

        String uniqueModelName = "TEST_MODEL_" + UUID.randomUUID().toString().substring(0, 8);
        UUID modelId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO car_models (id, name, brand_id, generation, created_at, updated_at, removed) " +
                        "VALUES (?::uuid, ?, ?::uuid, 'G05', NOW(), NOW(), false)",
                modelId, uniqueModelName, brandId);

        UUID fuelTypeId = jdbcTemplate.queryForObject(
                "SELECT id FROM engine_fuel_types WHERE name = 'PETROL'", UUID.class);
        UUID engineId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO engines (id, fuel_type_id, displacement, horse_power, created_at, updated_at, removed) " +
                        "VALUES (?::uuid, ?::uuid, 3.0, 300.0, NOW(), NOW(), false)",
                engineId, fuelTypeId);

        UUID transmissionTypeId = jdbcTemplate.queryForObject(
                "SELECT id FROM transmission_types WHERE name = 'AUTOMATIC'", UUID.class);
        UUID transmissionId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO transmissions (id, type_id, gears, created_at, updated_at, removed) " +
                        "VALUES (?::uuid, ?::uuid, 8, NOW(), NOW(), false)",
                transmissionId, transmissionTypeId);

        UUID bodyId = jdbcTemplate.queryForObject(
                "SELECT id FROM car_bodies WHERE name = 'SEDAN'", UUID.class);
        UUID colorId = jdbcTemplate.queryForObject(
                "SELECT id FROM car_colors WHERE name = 'BLACK'", UUID.class);
        UUID driveTypeId = jdbcTemplate.queryForObject(
                "SELECT id FROM drive_types WHERE name = 'FRONT'", UUID.class);

        UUID statusId = jdbcTemplate.queryForObject(
                "SELECT id FROM car_statuses WHERE name = 'TEST_DRIVE_AVAILABLE'", UUID.class);

        if (statusId == null) {
            statusId = jdbcTemplate.queryForObject(
                    "SELECT id FROM car_statuses WHERE name = 'AVAILABLE'", UUID.class);
        }

        String carUuid = UUID.randomUUID().toString();
        jdbcTemplate.update(
                "INSERT INTO cars (id, brand_id, model_id, body_id, color_id, drive_type_id, engine_id, transmission_id, price, status_id, created_at, updated_at, removed) " +
                        "VALUES (?::uuid, ?::uuid, ?::uuid, ?::uuid, ?::uuid, ?::uuid, ?::uuid, ?::uuid, 3500000.0, ?::uuid, NOW(), NOW(), false)",
                UUID.fromString(carUuid), brandId, modelId, bodyId, colorId, driveTypeId, engineId, transmissionId, statusId);

        entityManager.flush();
        entityManager.clear();

        var carOpt = carRepository.findById(carUuid);
        if (carOpt.isPresent()) {
            System.out.println("Car created and found by repository! Available for test drive: " +
                    carOpt.get().isAvailableForTestDrive());
        } else {
            System.err.println("ERROR: Car not found by repository after creation!");
        }

        return carUuid;
    }

    protected String createAdditionalTestCar() throws Exception {
        return createTestCarForTestDrive();
    }

    protected LocalDateTime getFutureTime() {
        return LocalDateTime.now().plusHours(2);
    }

    protected LocalDateTime getPastTime() {
        return LocalDateTime.now().minusHours(2);
    }

    protected String formatDateTime(LocalDateTime dateTime) {
        return dateTime.withNano(0).toString();
    }

    protected String getFutureTimeFormatted() {
        return formatDateTime(getFutureTime());
    }

    protected String getPastTimeFormatted() {
        return formatDateTime(getPastTime());
    }
}