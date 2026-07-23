package carIntegrationTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import domain.models.car.types.CarStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class CarControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String adminId;

    @BeforeEach
    void setUp() {
        createReferenceDataIfNotExists();

        UUID statusId = jdbcTemplate.queryForObject(
                "SELECT id FROM user_statuses WHERE name = 'ACTIVE'", UUID.class);
        UUID userTypeId = jdbcTemplate.queryForObject(
                "SELECT id FROM user_types WHERE name = 'SYSTEM_ADMIN'", UUID.class);
        UUID adminLevelId = jdbcTemplate.queryForObject(
                "SELECT id FROM admin_levels WHERE name = 'SUPER_ADMIN'", UUID.class);

        jdbcTemplate.update("DELETE FROM audit_log_entries");
        jdbcTemplate.update("DELETE FROM admin_permissions");
        jdbcTemplate.update("DELETE FROM system_admins");
        jdbcTemplate.update("DELETE FROM users WHERE email = 'admin@test.com'");

        adminId = UUID.randomUUID().toString();

        jdbcTemplate.update(
                "INSERT INTO users (id, first_name, last_name, middle_name, email, phone, password_hash, status_id, user_type_id, created_at, updated_at, removed) " +
                        "VALUES (?::uuid, ?, ?, ?, ?, ?, ?, ?::uuid, ?::uuid, NOW(), NOW(), false)",
                UUID.fromString(adminId), "Admin", "User", null, "admin@test.com",
                "+71234567890", "hashed", statusId, userTypeId
        );

        jdbcTemplate.update(
                "INSERT INTO system_admins (user_id, admin_level_id, last_login_at) VALUES (?::uuid, ?::uuid, NOW())",
                UUID.fromString(adminId), adminLevelId
        );
    }

    private void createReferenceDataIfNotExists() {
        Integer activeCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_statuses WHERE name = 'ACTIVE'", Integer.class);
        if (activeCount == 0) {
            UUID id = UUID.randomUUID();
            jdbcTemplate.update(
                    "INSERT INTO user_statuses (id, name, display_name, can_authenticate, created_at, updated_at, removed) " +
                            "VALUES (?::uuid, 'ACTIVE', 'Активен', true, NOW(), NOW(), false)", id);
            id = UUID.randomUUID();
            jdbcTemplate.update(
                    "INSERT INTO user_statuses (id, name, display_name, can_authenticate, created_at, updated_at, removed) " +
                            "VALUES (?::uuid, 'INACTIVE', 'Неактивен', false, NOW(), NOW(), false)", id);
            id = UUID.randomUUID();
            jdbcTemplate.update(
                    "INSERT INTO user_statuses (id, name, display_name, can_authenticate, created_at, updated_at, removed) " +
                            "VALUES (?::uuid, 'BLOCKED', 'Заблокирован', false, NOW(), NOW(), false)", id);
        }

        Integer systemAdminCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_types WHERE name = 'SYSTEM_ADMIN'", Integer.class);
        if (systemAdminCount == 0) {
            UUID id = UUID.randomUUID();
            jdbcTemplate.update(
                    "INSERT INTO user_types (id, name, display_name, created_at, updated_at, removed) " +
                            "VALUES (?::uuid, 'SYSTEM_ADMIN', 'Системный администратор', NOW(), NOW(), false)", id);
            id = UUID.randomUUID();
            jdbcTemplate.update(
                    "INSERT INTO user_types (id, name, display_name, created_at, updated_at, removed) " +
                            "VALUES (?::uuid, 'CLIENT', 'Клиент', NOW(), NOW(), false)", id);
            id = UUID.randomUUID();
            jdbcTemplate.update(
                    "INSERT INTO user_types (id, name, display_name, created_at, updated_at, removed) " +
                            "VALUES (?::uuid, 'MANAGER', 'Менеджер', NOW(), NOW(), false)", id);
            id = UUID.randomUUID();
            jdbcTemplate.update(
                    "INSERT INTO user_types (id, name, display_name, created_at, updated_at, removed) " +
                            "VALUES (?::uuid, 'WAREHOUSE_ADMIN', 'Складской администратор', NOW(), NOW(), false)", id);
        }

        Integer superAdminCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM admin_levels WHERE name = 'SUPER_ADMIN'", Integer.class);
        if (superAdminCount == 0) {
            UUID id = UUID.randomUUID();
            jdbcTemplate.update(
                    "INSERT INTO admin_levels (id, name, display_name, level, created_at, updated_at, removed) " +
                            "VALUES (?::uuid, 'SUPER_ADMIN', 'Супер администратор', 100, NOW(), NOW(), false)", id);
            id = UUID.randomUUID();
            jdbcTemplate.update(
                    "INSERT INTO admin_levels (id, name, display_name, level, created_at, updated_at, removed) " +
                            "VALUES (?::uuid, 'ADMIN', 'Администратор', 50, NOW(), NOW(), false)", id);
        }
    }

    private String createCarRequest(String brand, String model, double price) {
        String priceStr = String.format(Locale.US, "%.2f", price);
        return String.format("""
        {
            "brand": "%s",
            "model": "%s",
            "bodyType": "SEDAN",
            "color": "BLACK",
            "driveType": "FRONT",
            "engineFuelType": "PETROL",
            "enginePower": 249.0,
            "engineDisplacement": 2.0,
            "transmissionGears": 8,
            "transmissionType": "AUTOMATIC",
            "price": %s
        }
        """, brand, model, priceStr);
    }

    private String createCarRequestViaMapper(String brand, String model, double price) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("brand", brand);
        request.put("model", model);
        request.put("bodyType", "SEDAN");
        request.put("color", "BLACK");
        request.put("driveType", "FRONT");
        request.put("engineFuelType", "PETROL");
        request.put("enginePower", 249.0);
        request.put("engineDisplacement", 2.0);
        request.put("transmissionGears", 8);
        request.put("transmissionType", "AUTOMATIC");
        request.put("price", price);
        return objectMapper.writeValueAsString(request);
    }

    private void createCarWithStatus(String brand, String model, double price, CarStatus status) throws Exception {
        String request = createCarRequest(brand, model, price);
        String response = mockMvc.perform(post("/api/admin/cars")
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String carId = objectMapper.readTree(response).get("id").asText();

        if (status != CarStatus.AVAILABLE) {
            String updateRequest = String.format("{\"status\": \"%s\"}", status.name());
            mockMvc.perform(put("/api/admin/cars/{id}", carId)
                            .header("X-User-Id", adminId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateRequest))
                    .andExpect(status().isOk());
        }
    }

    private String createCarWithSpecs(String brand, String model, String bodyType,
                                    String color, double price) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("brand", brand);
        request.put("model", model);
        request.put("bodyType", bodyType);
        request.put("color", color);
        request.put("driveType", "FRONT");
        request.put("engineFuelType", "PETROL");
        request.put("enginePower", 249.0);
        request.put("engineDisplacement", 2.0);
        request.put("transmissionGears", 8);
        request.put("transmissionType", "AUTOMATIC");
        request.put("price", price);

        String response = mockMvc.perform(post("/api/admin/cars")
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("id").asText();
    }

    @Test
    @Transactional
    @Rollback
    void shouldCreateCar() throws Exception {
        String request = createCarRequest("BMW", "X5", 2500000.0);

        mockMvc.perform(post("/api/admin/cars")
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.brand").value("BMW"))
                .andExpect(jsonPath("$.model").value("X5"));
    }

    @Test
    @Transactional
    @Rollback
    void shouldGetCarById() throws Exception {
        String createRequest = createCarRequest("BMW", "X5", 2500000.0);

        String response = mockMvc.perform(post("/api/admin/cars")
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequest))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String carId = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(get("/api/cars/{id}", carId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(carId))
                .andExpect(jsonPath("$.brand").value("BMW"))
                .andExpect(jsonPath("$.model").value("X5"));
    }

    @Test
    @Transactional
    @Rollback
    void shouldReturn404WhenCarNotFound() throws Exception {
        mockMvc.perform(get("/api/cars/{id}", UUID.randomUUID().toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    @Rollback
    void shouldCreateCarWithMinimumRequiredFields() throws Exception {
        String request = createCarRequest("TOYOTA", "Camry", 2000000.0);

        mockMvc.perform(post("/api/admin/cars")
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.brand").value("TOYOTA"))
                .andExpect(jsonPath("$.model").value("Camry"));
    }

    @Test
    @Transactional
    @Rollback
    void shouldFailCreateCarWithInvalidData() throws Exception {
        String request = """
        {
            "brand": "",
            "model": "",
            "bodyType": "SEDAN",
            "color": "BLACK",
            "driveType": "FRONT",
            "engineFuelType": "PETROL",
            "enginePower": -100.0,
            "engineDisplacement": 0.0,
            "transmissionGears": 0,
            "transmissionType": "AUTOMATIC",
            "price": -1000.0
        }
        """;

        mockMvc.perform(post("/api/admin/cars")
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    @Rollback
    void shouldFailCreateCarWithoutAuth() throws Exception {
        String request = createCarRequest("BMW", "X5", 2500000.0);

        mockMvc.perform(post("/api/admin/cars")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    @Rollback
    void shouldUpdateCar() throws Exception {
        String createRequest = createCarRequest("BMW", "X5", 2500000.0);

        String response = mockMvc.perform(post("/api/admin/cars")
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequest))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String carId = objectMapper.readTree(response).get("id").asText();

        String updateRequest = """
        {
            "price": 3000000.0,
            "status": "AVAILABLE"
        }
        """;

        mockMvc.perform(put("/api/admin/cars/{id}", carId)
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(3000000.0))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    @Transactional
    @Rollback
    void shouldDeleteCar() throws Exception {
        String createRequest = createCarRequest("BMW", "X5", 2500000.0);

        String response = mockMvc.perform(post("/api/admin/cars")
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequest))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String carId = objectMapper.readTree(response).get("id").asText();

        String updateRequest = "{\"status\": \"UNAVAILABLE\"}";
        mockMvc.perform(put("/api/admin/cars/{id}", carId)
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/admin/cars/{id}", carId)
                        .header("X-User-Id", adminId)
                        .param("reason", "Test deletion"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/cars/{id}", carId))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    @Rollback
    void shouldFailDeleteCarWhenNotAuthorized() throws Exception {
        String carId = UUID.randomUUID().toString();

        mockMvc.perform(delete("/api/admin/cars/{id}", carId)
                        .param("reason", "Test deletion"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    @Rollback
    void shouldGetAvailableCars() throws Exception {
        createCarWithStatus("BMW", "X5", 2500000.0, CarStatus.AVAILABLE);
        createCarWithStatus("TOYOTA", "Camry", 2000000.0, CarStatus.AVAILABLE);
        createCarWithStatus("BMW", "X3", 3000000.0, CarStatus.SOLD);

        mockMvc.perform(get("/api/cars/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cars.length()").value(2))
                .andExpect(jsonPath("$.cars[0].status").value("AVAILABLE"))
                .andExpect(jsonPath("$.cars[1].status").value("AVAILABLE"))
                .andExpect(jsonPath("$.totalCount").value(2))
                .andExpect(jsonPath("$.availableCount").value(2));
    }

    @Test
    @Transactional
    @Rollback
    void shouldGetCarsWithFilters() throws Exception {
        String carId1 = createCarWithSpecs("BMW", "X5", "SEDAN", "BLACK", 2500000.0);
        String carId2 = createCarWithSpecs("BMW", "X3", "COUPE", "WHITE", 3000000.0);
        String carId3 = createCarWithSpecs("TOYOTA", "Camry", "SEDAN", "BLACK", 2000000.0);

        updateCarStatusById(carId1, "AVAILABLE");
        updateCarStatusById(carId2, "SOLD");
        updateCarStatusById(carId3, "AVAILABLE");

        mockMvc.perform(get("/api/cars")
                        .param("brand", "BMW")
                        .param("minPrice", "2000000")
                        .param("maxPrice", "2800000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cars.length()").value(1))
                .andExpect(jsonPath("$.cars[0].model").value("X5"));
    }

    private void updateCarStatusById(String carId, String status) throws Exception {
        String updateRequest = String.format("{\"status\": \"%s\"}", status);
        mockMvc.perform(put("/api/admin/cars/{id}", carId)
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().isOk());
    }

    @Test
    @Transactional
    @Rollback
    void shouldFailUpdateCarWithInvalidPrice() throws Exception {
        String createRequest = createCarRequest("BMW", "X5", 2500000.0);

        String response = mockMvc.perform(post("/api/admin/cars")
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequest))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String carId = objectMapper.readTree(response).get("id").asText();

        String updateRequest = "{\"price\": -1000.0}";

        mockMvc.perform(put("/api/admin/cars/{id}", carId)
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().isBadRequest());
    }
}