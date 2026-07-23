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

class PaymentErrorTest {

    private Payment payment;

    @BeforeEach
    void setUp() {
        payment = new Payment("pay123", "order456", "client789", 3500000, PaymentMethod.CARD);
    }

    @Test
    @DisplayName("Should fail payment with reason")
    void shouldFailPaymentWithReason() {
        String failureReason = "Insufficient funds";

        payment.fail(failureReason);

        assertEquals(PaymentStatus.FAILED, payment.getStatus());
        assertEquals(failureReason, payment.getFailureReason());
        assertNotNull(payment.getProcessedAt());
    }

    @Test
    @DisplayName("Should not process already processed payment")
    void shouldNotProcessAlreadyProcessedPayment() {
        payment.process();

        assertThrows(DomainValidationException.class, () -> payment.process());
    }

    @Test
    @DisplayName("Should not process cancelled payment")
    void shouldNotProcessCancelledPayment() {
        payment.fail("Cancelled by user");

        assertThrows(DomainValidationException.class, () -> payment.process());
    }

    @Test
    @DisplayName("Should not process refunded payment")
    void shouldNotProcessRefundedPayment() {
        payment.process();
        payment.refund();

        assertThrows(DomainValidationException.class, () -> payment.process());
    }

    @Test
    @DisplayName("Should set failure reason on fail")
    void shouldSetFailureReason() {
        String reason = "Payment gateway error";

        payment.fail(reason);

        assertEquals(reason, payment.getFailureReason());
    }

    @Test
    @DisplayName("Should set processedAt on fail")
    void shouldSetProcessedAtOnFail() {
        LocalDateTime before = LocalDateTime.now();

        payment.fail("Error");

        assertNotNull(payment.getProcessedAt());
        assertTrue(payment.getProcessedAt().isAfter(before) || payment.getProcessedAt().isEqual(before));
    }
}