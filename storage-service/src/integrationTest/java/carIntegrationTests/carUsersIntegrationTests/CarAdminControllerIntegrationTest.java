package carIntegrationTests.carUsersIntegrationTests;

import carIntegrationTests.BaseIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import domain.repository.userRepository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class CarAdminControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserRepository userRepository;

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

        adminId = UUID.randomUUID().toString();

        jdbcTemplate.update(
                "INSERT INTO users (id, first_name, last_name, email, phone, password_hash, status_id, user_type_id, created_at, updated_at, removed) " +
                        "VALUES (?::uuid, 'Admin', 'User', 'admin@test.com', '+71234567890', 'hashed', ?::uuid, ?::uuid, NOW(), NOW(), false)",
                UUID.fromString(adminId), statusId, userTypeId
        );

        jdbcTemplate.update(
                "INSERT INTO system_admins (user_id, admin_level_id, last_login_at) VALUES (?::uuid, ?::uuid, NOW())",
                UUID.fromString(adminId), adminLevelId
        );
    }

    private void createReferenceDataIfNotExists() {
        if (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_statuses WHERE name = 'ACTIVE'", Integer.class) == 0) {
            jdbcTemplate.update("INSERT INTO user_statuses (id, name, display_name, can_authenticate, created_at, updated_at, removed) VALUES (?::uuid, 'ACTIVE', 'Активен', true, NOW(), NOW(), false)", UUID.randomUUID());
            jdbcTemplate.update("INSERT INTO user_statuses (id, name, display_name, can_authenticate, created_at, updated_at, removed) VALUES (?::uuid, 'INACTIVE', 'Неактивен', false, NOW(), NOW(), false)", UUID.randomUUID());
            jdbcTemplate.update("INSERT INTO user_statuses (id, name, display_name, can_authenticate, created_at, updated_at, removed) VALUES (?::uuid, 'BLOCKED', 'Заблокирован', false, NOW(), NOW(), false)", UUID.randomUUID());
        }

        if (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_types WHERE name = 'SYSTEM_ADMIN'", Integer.class) == 0) {
            jdbcTemplate.update("INSERT INTO user_types (id, name, display_name, created_at, updated_at, removed) VALUES (?::uuid, 'SYSTEM_ADMIN', 'Системный администратор', NOW(), NOW(), false)", UUID.randomUUID());
            jdbcTemplate.update("INSERT INTO user_types (id, name, display_name, created_at, updated_at, removed) VALUES (?::uuid, 'CLIENT', 'Клиент', NOW(), NOW(), false)", UUID.randomUUID());
            jdbcTemplate.update("INSERT INTO user_types (id, name, display_name, created_at, updated_at, removed) VALUES (?::uuid, 'MANAGER', 'Менеджер', NOW(), NOW(), false)", UUID.randomUUID());
        }

        if (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM admin_levels WHERE name = 'SUPER_ADMIN'", Integer.class) == 0) {
            jdbcTemplate.update("INSERT INTO admin_levels (id, name, display_name, level, created_at, updated_at, removed) VALUES (?::uuid, 'SUPER_ADMIN', 'Супер администратор', 100, NOW(), NOW(), false)", UUID.randomUUID());
            jdbcTemplate.update("INSERT INTO admin_levels (id, name, display_name, level, created_at, updated_at, removed) VALUES (?::uuid, 'ADMIN', 'Администратор', 50, NOW(), NOW(), false)", UUID.randomUUID());
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
                .andExpect(jsonPath("$.brand").value("BMW"));
    }

    @Test
    @Transactional
    @Rollback
    void shouldUpdateCarStatus() throws Exception {
        String createRequest = createCarRequest("BMW", "X5", 2500000.0);
        String response = mockMvc.perform(post("/api/admin/cars")
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequest))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String carId = objectMapper.readTree(response).get("id").asText();
        String updateRequest = "{\"status\": \"SOLD\"}";

        mockMvc.perform(put("/api/admin/cars/{id}", carId)
                        .header("X-User-Id", adminId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SOLD"));
    }

    @Test
    @Transactional
    @Rollback
    void shouldFailCreateCarWithoutAdminAuth() throws Exception {
        String request = createCarRequest("BMW", "X5", 2500000.0);

        mockMvc.perform(post("/api/admin/cars")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
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
    }
}