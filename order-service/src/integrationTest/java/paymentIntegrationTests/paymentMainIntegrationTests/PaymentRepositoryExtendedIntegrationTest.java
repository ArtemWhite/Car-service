// ============================================
// 6. PaymentRepositoryExtendedIntegrationTest.java
// ============================================
package paymentIntegrationTests.paymentMainIntegrationTests;

import dealerShipOrder.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class PaymentRepositoryExtendedIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private EntityManager entityManager;

    private String paymentId;
    private String orderId;
    private String clientId;

    @BeforeEach
    void setUp() {
        createTestPayment();
    }

    @Test
    void shouldUpdatePaymentStatusDirectly() {
        UUID paymentUuid = UUID.fromString(paymentId);
        jdbcTemplate.update(
                "UPDATE payments SET status_id = (SELECT id FROM payment_statuses WHERE name = 'COMPLETED') WHERE id = ?::uuid",
                paymentUuid
        );

        entityManager.flush();
        entityManager.clear();

        String status = jdbcTemplate.queryForObject(
                "SELECT s.name FROM payments p JOIN payment_statuses s ON p.status_id = s.id WHERE p.id = ?::uuid",
                String.class, paymentUuid
        );
        assertThat(status).isEqualTo("COMPLETED");
    }

    @Test
    void shouldUpdatePaymentTransactionIdDirectly() {
        UUID paymentUuid = UUID.fromString(paymentId);
        String newTransactionId = "MANUAL-TXN-123";

        jdbcTemplate.update(
                "UPDATE payments SET transaction_id = ? WHERE id = ?::uuid",
                newTransactionId, paymentUuid
        );

        entityManager.flush();
        entityManager.clear();

        String transactionId = jdbcTemplate.queryForObject(
                "SELECT transaction_id FROM payments WHERE id = ?::uuid",
                String.class, paymentUuid
        );
        assertThat(transactionId).isEqualTo(newTransactionId);
    }

    @Test
    void shouldSoftDeletePayment() {
        UUID paymentUuid = UUID.fromString(paymentId);

        jdbcTemplate.update(
                "UPDATE payments SET removed = true WHERE id = ?::uuid",
                paymentUuid
        );

        entityManager.flush();
        entityManager.clear();

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM payments WHERE id = ?::uuid AND removed = false",
                Integer.class, paymentUuid
        );
        assertThat(count).isEqualTo(0);
    }

    @Test
    void shouldNotReturnSoftDeletedPayments() {
        UUID paymentUuid = UUID.fromString(paymentId);
        jdbcTemplate.update(
                "UPDATE payments SET removed = true WHERE id = ?::uuid",
                paymentUuid
        );

        entityManager.flush();
        entityManager.clear();

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM payments WHERE removed = false",
                Integer.class
        );

        assertThat(count).isEqualTo(0);
    }

    private void createTestPayment() {
        clientId = UUID.randomUUID().toString();
        orderId = UUID.randomUUID().toString();
        paymentId = UUID.randomUUID().toString();

        jdbcTemplate.update(
                "INSERT INTO orders (id, client_id, type_id, status_id, created_at, updated_at, removed) " +
                        "VALUES (?::uuid, ?, (SELECT id FROM order_types LIMIT 1), (SELECT id FROM order_statuses WHERE name = 'CREATED'), NOW(), NOW(), false)",
                UUID.fromString(orderId), clientId
        );

        jdbcTemplate.update(
                "INSERT INTO payments (id, order_id, client_id, amount, method_id, status_id, created_at, updated_at, removed) " +
                        "VALUES (?::uuid, ?, ?, 2500000.0, (SELECT id FROM payment_methods LIMIT 1), (SELECT id FROM payment_statuses WHERE name = 'PENDING'), NOW(), NOW(), false)",
                UUID.fromString(paymentId), orderId, clientId
        );
    }
}