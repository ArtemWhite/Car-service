package testdriveRequestIntegrationTests.testDriveRequestMainIntegrationTests;

import dealerShipOrder.BaseIntegrationTest;
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

        testCarModelId = UUID.randomUUID().toString();
        testCarId = UUID.randomUUID().toString();
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

        String[] userStatuses = {"ACTIVE", "INACTIVE", "BLOCKED"};
        for (String status : userStatuses) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM user_statuses WHERE name = ?", Integer.class, status);
            if (count == 0) {
                jdbcTemplate.update(
                        "INSERT INTO user_statuses (id, name, display_name, can_authenticate, created_at, updated_at, removed) " +
                                "VALUES (?::uuid, ?, ?, true, NOW(), NOW(), false)",
                        UUID.randomUUID(), status, status);
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
        return UUID.randomUUID().toString();
    }

    protected String createTestCarForTestDrive() throws Exception {
        return UUID.randomUUID().toString();
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