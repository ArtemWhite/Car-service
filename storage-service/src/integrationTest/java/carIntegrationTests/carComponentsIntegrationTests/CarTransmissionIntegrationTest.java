package carIntegrationTests.carComponentsIntegrationTests;

import carIntegrationTests.BaseIntegrationTest;
import domain.models.car.transmission.Transmission;
import domain.models.car.transmission.TransmissionType;
import domain.repository.carRepository.CarRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class CarTransmissionIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Transactional
    @Rollback
    void shouldFindTransmissionByTypeAndGears() {
        UUID typeId = getUuidByName("transmission_types", "AUTOMATIC");
        UUID transmissionId = UUID.randomUUID();

        jdbcTemplate.update(
                "INSERT INTO transmissions (id, type_id, gears, created_at, updated_at, removed) " +
                        "VALUES (?, ?, 6, NOW(), NOW(), false)",
                transmissionId, typeId
        );

        var found = carRepository.findTransmissionByTypeAndGears(TransmissionType.AUTOMATIC, 6);

        assertThat(found).isPresent();
        assertThat(found.get().getTransmissionType()).isEqualTo(TransmissionType.AUTOMATIC);
        assertThat(found.get().getGears()).isEqualTo(6);
    }

    @Test
    @Transactional
    @Rollback
    void shouldCreateNewTransmissionIfNotFound() {
        var found = carRepository.findTransmissionByTypeAndGears(TransmissionType.MANUAL, 5);
        assertThat(found).isEmpty();

        Transmission newTransmission = new Transmission(TransmissionType.MANUAL, 5);
        Transmission saved = carRepository.saveTransmission(newTransmission);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTransmissionType()).isEqualTo(TransmissionType.MANUAL);
        assertThat(saved.getGears()).isEqualTo(5);
    }

    @Test
    @Transactional
    @Rollback
    void shouldGetCorrectFullName() {
        Transmission auto = new Transmission(TransmissionType.AUTOMATIC, 8);
        assertThat(auto.getFullName()).isEqualTo("АКПП 8ст.");

        Transmission manual = new Transmission(TransmissionType.MANUAL, 6);
        assertThat(manual.getFullName()).isEqualTo("МКПП 6ст.");
    }

    private UUID getUuidByName(String tableName, String name) {
        String sql = String.format("SELECT id FROM %s WHERE name = ?", tableName);
        String idStr = jdbcTemplate.queryForObject(sql, String.class, name);
        return idStr != null ? UUID.fromString(idStr) : null;
    }
}