package sparePartIntegrationTests;

import carIntegrationTests.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class SparePartLiquibaseIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldCreateSparePartsTable() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'spare_parts' AND table_schema = 'public'",
                Integer.class
        );
        assertThat(count).isEqualTo(1);
    }

    @Test
    void shouldCreateSpareTypesTable() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'spare_types' AND table_schema = 'public'",
                Integer.class
        );
        assertThat(count).isEqualTo(1);
    }

    @Test
    void shouldCreateCompatibilitiesTable() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'spare_part_compatibilities' AND table_schema = 'public'",
                Integer.class
        );
        assertThat(count).isEqualTo(1);
    }

    @Test
    void shouldHaveCorrectColumnsInSparePartsTable() {
        Integer columnCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'spare_parts'",
                Integer.class
        );
        assertThat(columnCount).isGreaterThan(10);
    }

    @Test
    void shouldHaveForeignKeyToSpareTypes() {
        Integer fkCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.table_constraints tc " +
                        "JOIN information_schema.constraint_column_usage ccu ON tc.constraint_name = ccu.constraint_name " +
                        "WHERE tc.table_name = 'spare_parts' AND tc.constraint_type = 'FOREIGN KEY' AND ccu.table_name = 'spare_types'",
                Integer.class
        );
        assertThat(fkCount).isGreaterThan(0);
    }

    @Test
    void shouldInsertReferenceData() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM spare_types",
                Integer.class
        );
        assertThat(count).isGreaterThan(0);
    }
}