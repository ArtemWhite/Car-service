package carIntegrationTests;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class LiquibaseMigrationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldApplyAllMigrations() {
        Integer changeSetsCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM databasechangelog",
                Integer.class
        );
        assertThat(changeSetsCount).isGreaterThan(0);
    }

    @Test
    void shouldCreateAllTables() {
        String[] tables = {"users", "cars", "orders", "payments", "spare_parts", "test_drive_requests"};

        for (String table : tables) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = ? AND table_schema = 'public'",
                    Integer.class,
                    table
            );
            assertThat(count).as("Table %s should exist", table).isEqualTo(1);
        }
    }

    @Test
    void shouldHaveCorrectColumnsInUsersTable() {
        Integer columnCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'users' AND table_schema = 'public'",
                Integer.class
        );
        assertThat(columnCount).isGreaterThan(10);
    }
}