package testdriveRequestIntegrationTests.testDriveRequestMainIntegrationTests;

import carIntegrationTests.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TestDriveLiquibaseIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldCreateTestDrivesTable() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'test_drive_requests'",
                Integer.class);
        assertThat(count).isEqualTo(1);
    }

    @Test
    void shouldCreateTestDriveStatusesTable() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'test_drive_statuses'",
                Integer.class);
        assertThat(count).isEqualTo(1);
    }

    @Test
    void shouldHaveCorrectColumns() {
        Integer columnCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'test_drive_requests'",
                Integer.class);
        assertThat(columnCount).isGreaterThan(5);
    }

    @Test
    void shouldHaveForeignKeyToStatus() {
        Integer fkCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.table_constraints tc " +
                        "JOIN information_schema.constraint_column_usage ccu ON tc.constraint_name = ccu.constraint_name " +
                        "WHERE tc.table_name = 'test_drive_requests' AND tc.constraint_type = 'FOREIGN KEY' " +
                        "AND ccu.table_name = 'test_drive_statuses'",
                Integer.class);
        assertThat(fkCount).isGreaterThan(0);
    }

    @Test
    void shouldInsertReferenceStatuses() {
        String[] statuses = {"PENDING", "CONFIRMED", "COMPLETED", "CANCELLED", "NO_SHOW"};
        for (String status : statuses) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM test_drive_statuses WHERE name = ?",
                    Integer.class, status);
            assertThat(count).isEqualTo(1);
        }
    }

    @Test
    void shouldHaveCorrectColumnTypes() throws Exception {
        Map<String, String> expectedTypes = Map.of(
                "id", "uuid",
                "client_id", "character varying",
                "car_id", "character varying",
                "manager_id", "character varying",
                "requested_time", "timestamp",
                "confirmed_time", "timestamp",
                "status_id", "uuid",
                "notes", "character varying"
        );

        for (Map.Entry<String, String> entry : expectedTypes.entrySet()) {
            String columnType = jdbcTemplate.queryForObject(
                    "SELECT data_type FROM information_schema.columns WHERE table_name = 'test_drive_requests' AND column_name = ?",
                    String.class, entry.getKey());

            if (entry.getValue().equals("timestamp")) {
                assertThat(columnType).as("Column " + entry.getKey()).contains("timestamp");
            } else {
                assertThat(columnType).as("Column " + entry.getKey()).isEqualTo(entry.getValue());
            }
        }
    }
}