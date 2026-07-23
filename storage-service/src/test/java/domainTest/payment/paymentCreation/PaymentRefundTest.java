package domainTest.payment.paymentCreation;

import domain.exception.DomainValidationException;
import domain.models.payment.Payment;
import domain.models.payment.PaymentMethod;
import domain.models.payment.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.*;

class PaymentRefundTest {

    private Payment payment;

    @BeforeEach
    void setUp() {
        payment = new Payment("pay123", "order456", "client789", 3500000, PaymentMethod.CARD);
    }

    @Test
    @DisplayName("Should successfully refund completed payment")
    void shouldSuccessfullyRefundCompletedPayment() {
        payment.process();
        assertEquals(PaymentStatus.COMPLETED, payment.getStatus());

        payment.refund();

        assertEquals(PaymentStatus.REFUNDED, payment.getStatus());
        assertNotNull(payment.getProcessedAt());
    }

    @Test
    @DisplayName("Should not refund pending payment")
    void shouldNotRefundPendingPayment() {
        assertEquals(PaymentStatus.PENDING, payment.getStatus());

        assertThrows(DomainValidationException.class, () -> payment.refund());
    }

    @Test
    @DisplayName("Should not refund failed payment")
    void shouldNotRefundFailedPayment() {
        payment.fail("Error");

        assertThrows(DomainValidationException.class, () -> payment.refund());
    }

    @Test
    @DisplayName("Should not refund already refunded payment")
    void shouldNotRefundAlreadyRefundedPayment() {
        payment.process();
        payment.refund();

        assertThrows(DomainValidationException.class, () -> payment.refund());
    }

    @Test
    @DisplayName("Should update processedAt on refund")
    void shouldUpdateProcessedAtOnRefund() {
        payment.process();
        LocalDateTime processedAfterProcess = payment.getProcessedAt();

        try { Thread.sleep(10); } catch (InterruptedException e) {}
        payment.refund();

        assertNotNull(payment.getProcessedAt());
        assertTrue(payment.getProcessedAt().isAfter(processedAfterProcess));
    }
}
