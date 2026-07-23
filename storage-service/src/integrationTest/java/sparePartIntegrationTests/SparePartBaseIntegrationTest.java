package sparePartIntegrationTests;

import carIntegrationTests.BaseIntegrationTest;
import domain.models.car.CarModel;
import domain.repository.carRepository.CarRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.EntityManager;

import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
public abstract class SparePartBaseIntegrationTest extends BaseIntegrationTest {

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
    protected String warehouseAdminId;
    protected String carModelId;
    protected String carId;

    @BeforeEach
    void baseSetUp() throws Exception {
        createReferenceData();
        createTestUsers();

        entityManager.flush();
        entityManager.clear();

        carModelId = createTestCarModel();

        Integer modelExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM car_models WHERE id = ?::uuid AND removed = false",
                Integer.class, UUID.fromString(carModelId)
        );
        if (modelExists == 0) {
            throw new RuntimeException("Failed to create car model! Model not found after creation.");
        }
        System.out.println("Car model created successfully: " + carModelId);

        carId = createTestCar();
    }

    private void createReferenceData() {
        Integer positionCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM warehouse_positions WHERE name = 'WAREHOUSE_WORKER'",
                Integer.class);
        if (positionCount == 0) {
            jdbcTemplate.update(
                    "INSERT INTO warehouse_positions (id, name, display_name, created_at, updated_at, removed) " +
                            "VALUES (?::uuid, 'WAREHOUSE_WORKER', 'Складской работник', NOW(), NOW(), false)",
                    UUID.randomUUID());
            System.out.println("Created warehouse_position: WAREHOUSE_WORKER");
        }

        Integer managerPosCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM manager_positions WHERE name = 'SALES_MANAGER'",
                Integer.class);
        if (managerPosCount == 0) {
            jdbcTemplate.update(
                    "INSERT INTO manager_positions (id, name, display_name, max_concurrent_orders, max_concurrent_test_drives, created_at, updated_at, removed) " +
                            "VALUES (?::uuid, 'SALES_MANAGER', 'Менеджер по продажам', 10, 5, NOW(), NOW(), false)",
                    UUID.randomUUID());
            System.out.println("Created manager_position: SALES_MANAGER");
        }

        Integer adminLevelCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM admin_levels WHERE name = 'SUPER_ADMIN'",
                Integer.class);
        if (adminLevelCount == 0) {
            jdbcTemplate.update(
                    "INSERT INTO admin_levels (id, name, display_name, level, created_at, updated_at, removed) " +
                            "VALUES (?::uuid, 'SUPER_ADMIN', 'Супер администратор', 100, NOW(), NOW(), false)",
                    UUID.randomUUID());
            System.out.println("Created admin_level: SUPER_ADMIN");
        }

        Integer auditTableCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'audit_log_entries'",
                Integer.class);
        if (auditTableCount == 0) {
            jdbcTemplate.execute(
                    "CREATE TABLE IF NOT EXISTS audit_log_entries (" +
                            "id UUID PRIMARY KEY, " +
                            "admin_id UUID NOT NULL, " +
                            "action VARCHAR(100) NOT NULL, " +
                            "details VARCHAR(500), " +
                            "log_timestamp TIMESTAMP NOT NULL, " +
                            "created_at TIMESTAMP, " +
                            "updated_at TIMESTAMP, " +
                            "removed BOOLEAN DEFAULT false" +
                            ")"
            );
            System.out.println("Created audit_log_entries table");
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

        warehouseAdminId = UUID.randomUUID().toString();
        createUser(warehouseAdminId, "WAREHOUSE_ADMIN");
        createWarehouseAdmin(warehouseAdminId);
    }

    protected void createUser(String id, String type) {
        jdbcTemplate.update(
                "INSERT INTO users (id, first_name, last_name, email, phone, password_hash, status_id, user_type_id, created_at, updated_at, removed) " +
                        "VALUES (?::uuid, 'Test', 'User', ?, '1234567890', 'hash', " +
                        "(SELECT id FROM user_statuses WHERE name = 'ACTIVE'), " +
                        "(SELECT id FROM user_types WHERE name = ?), NOW(), NOW(), false)",
                UUID.fromString(id), id + "@test.com", type
        );
    }

    protected void createClient(String id) {
        jdbcTemplate.update(
                "INSERT INTO clients (user_id, preferred_contact_method, newsletter_subscribed) VALUES (?::uuid, 'EMAIL', false)",
                UUID.fromString(id)
        );
    }

    protected void createManager(String id) {
        jdbcTemplate.update(
                "INSERT INTO managers (user_id, position_id, max_concurrent_orders, max_concurrent_test_drives, available) " +
                        "VALUES (?::uuid, (SELECT id FROM manager_positions LIMIT 1), 10, 5, true)",
                UUID.fromString(id)
        );
    }

    protected void createSystemAdmin(String id) {
        jdbcTemplate.update(
                "INSERT INTO system_admins (user_id, admin_level_id, last_login_at) " +
                        "VALUES (?::uuid, (SELECT id FROM admin_levels WHERE name = 'SUPER_ADMIN'), NOW())",
                UUID.fromString(id)
        );
    }

    protected void createWarehouseAdmin(String id) {
        jdbcTemplate.update(
                "INSERT INTO warehouse_admins (user_id, warehouse_position_id, on_duty) " +
                        "VALUES (?::uuid, (SELECT id FROM warehouse_positions LIMIT 1), false)",
                UUID.fromString(id)
        );
    }

    protected String createTestCarModel() throws Exception {
        String brandName = "BMW";

        UUID brandId = jdbcTemplate.queryForObject(
                "SELECT id FROM car_brands WHERE name = ?", UUID.class, brandName);

        if (brandId == null) {
            brandId = UUID.randomUUID();
            jdbcTemplate.update(
                    "INSERT INTO car_brands (id, name, display_name, country_made, created_at, updated_at, removed) " +
                            "VALUES (?::uuid, ?, ?, 'Test', NOW(), NOW(), false)",
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

        Optional<CarModel> found = carRepository.findModelById(modelId.toString());
        if (found.isEmpty()) {
            System.err.println("WARNING: Model not found after creation! ID: " + modelId);
            List<Map<String, Object>> allModels = jdbcTemplate.queryForList(
                    "SELECT id, name, removed FROM car_models");
            System.out.println("All models in DB: " + allModels);
        } else {
            System.out.println("Model created and found successfully: " + modelId);
        }

        return modelId.toString();
    }

    protected String createTestCar() throws Exception {
        String uniqueBrandName = "TEST_CAR_BRAND_" + UUID.randomUUID().toString().substring(0, 8);
        UUID brandId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO car_brands (id, name, display_name, country_made, created_at, updated_at, removed) " +
                        "VALUES (?::uuid, ?, ?, 'Test', NOW(), NOW(), false)",
                brandId, uniqueBrandName, uniqueBrandName);

        String uniqueModelName = "TEST_CAR_MODEL_" + UUID.randomUUID().toString().substring(0, 8);
        UUID modelId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO car_models (id, name, brand_id, generation, created_at, updated_at, removed) " +
                        "VALUES (?::uuid, ?, ?::uuid, 'TEST', NOW(), NOW(), false)",
                modelId, uniqueModelName, brandId);

        UUID fuelTypeId = jdbcTemplate.queryForObject(
                "SELECT id FROM engine_fuel_types WHERE name = 'PETROL'", UUID.class);
        UUID engineId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO engines (id, fuel_type_id, displacement, horse_power, created_at, updated_at, removed) " +
                        "VALUES (?::uuid, ?::uuid, 2.0, 200.0, NOW(), NOW(), false)",
                engineId, fuelTypeId);

        UUID transmissionTypeId = jdbcTemplate.queryForObject(
                "SELECT id FROM transmission_types WHERE name = 'MANUAL'", UUID.class);
        UUID transmissionId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO transmissions (id, type_id, gears, created_at, updated_at, removed) " +
                        "VALUES (?::uuid, ?::uuid, 6, NOW(), NOW(), false)",
                transmissionId, transmissionTypeId);

        UUID bodyId = jdbcTemplate.queryForObject(
                "SELECT id FROM car_bodies WHERE name = 'SEDAN'", UUID.class);
        UUID colorId = jdbcTemplate.queryForObject(
                "SELECT id FROM car_colors WHERE name = 'BLACK'", UUID.class);
        UUID driveTypeId = jdbcTemplate.queryForObject(
                "SELECT id FROM drive_types WHERE name = 'FRONT'", UUID.class);
        UUID statusId = jdbcTemplate.queryForObject(
                "SELECT id FROM car_statuses WHERE name = 'AVAILABLE'", UUID.class);

        String carUuid = UUID.randomUUID().toString();
        jdbcTemplate.update(
                "INSERT INTO cars (id, brand_id, model_id, body_id, color_id, drive_type_id, engine_id, transmission_id, price, status_id, created_at, updated_at, removed) " +
                        "VALUES (?::uuid, ?::uuid, ?::uuid, ?::uuid, ?::uuid, ?::uuid, ?::uuid, ?::uuid, 2000000.0, ?::uuid, NOW(), NOW(), false)",
                UUID.fromString(carUuid), brandId, modelId, bodyId, colorId, driveTypeId, engineId, transmissionId, statusId);

        return carUuid;
    }

    protected String createSparePartDirect(String name, String type, double price, int quantity, String compatibleModelId) {
        String sparePartId = UUID.randomUUID().toString();

        UUID spareTypeId = jdbcTemplate.queryForObject(
                "SELECT id FROM spare_types WHERE name = ?", UUID.class, type);

        String partNumber = "PN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        jdbcTemplate.update(
                "INSERT INTO spare_parts (id, type_id, name, description, manufacturer, part_number, price, currency, stock_quantity, section_id, location, created_at, updated_at, removed) " +
                        "VALUES (?::uuid, ?::uuid, ?, ?, ?, ?, ?, 'RUB', ?, 'SEC-01', 'A-01', NOW(), NOW(), false)",
                UUID.fromString(sparePartId), spareTypeId, name, "Test description for " + name,
                "Test Manufacturer", partNumber, price, quantity);

        if (compatibleModelId != null) {
            UUID sparePartUuid = UUID.fromString(sparePartId);
            UUID modelUuid = UUID.fromString(compatibleModelId);
            jdbcTemplate.update(
                    "INSERT INTO spare_part_compatibilities (id, spare_part_id, car_model_id, created_at, updated_at, removed) " +
                            "VALUES (?::uuid, ?::uuid, ?::uuid, NOW(), NOW(), false)",
                    UUID.randomUUID(), sparePartUuid, modelUuid);
        }

        return sparePartId;
    }

    protected String createSparePartDirect(String name, String type, double price, int quantity) {
        return createSparePartDirect(name, type, price, quantity, null);
    }

    protected String createSparePart(String name, String type, double price, int quantity, Set<String> compatibleModelIds) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("spareType", type);
        request.put("name", name);
        request.put("description", "Test description for " + name);
        request.put("manufacturer", "Test Manufacturer");
        request.put("partNumber", "PN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        request.put("price", price);
        request.put("quantity", quantity);
        request.put("sectionId", "SEC-01");
        request.put("location", "A-01");
        if (compatibleModelIds != null && !compatibleModelIds.isEmpty()) {
            request.put("compatibleModelIds", compatibleModelIds);
        }

        String response = mockMvc.perform(post("/api/admin/spare-parts")
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("id").asText();
    }

    protected String createSparePart(String name, String type, double price, int quantity) throws Exception {
        return createSparePart(name, type, price, quantity, null);
    }
}