package dealerShipOrder.domainTest.payment.paymentCreation;

import dealerShipOrder.domain.models.payment.Payment;
import dealerShipOrder.domain.models.payment.PaymentMethod;
import dealerShipOrder.domain.models.payment.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.*;

class PaymentProcessTest {

    private Payment payment;

    @BeforeEach
    void setUp() {
        payment = new Payment("pay123", "order456", "client789", 3500000, PaymentMethod.CARD);
    }

    @Test
    @DisplayName("Should successfully process payment")
    void shouldSuccessfullyProcessPayment() {
        LocalDateTime before = LocalDateTime.now();
        assertEquals(PaymentStatus.PENDING, payment.getStatus());
        assertNull(payment.getProcessedAt());
        assertNull(payment.getTransactionId());

        payment.process();

        assertEquals(PaymentStatus.COMPLETED, payment.getStatus());
        assertNotNull(payment.getProcessedAt());
        assertNotNull(payment.getTransactionId());
        assertTrue(payment.getProcessedAt().isAfter(before) || payment.getProcessedAt().isEqual(before));
    }

    @Test
    @DisplayName("Should generate transaction ID on successful process")
    void shouldGenerateTransactionId() {
        payment.process();

        assertNotNull(payment.getTransactionId());
        assertTrue(payment.getTransactionId().startsWith("TXN-"));
        assertEquals(40, payment.getTransactionId().length());
    }

    @Test
    @DisplayName("Should set processedAt timestamp on successful process")
    void shouldSetProcessedAt() {
        LocalDateTime before = LocalDateTime.now();

        payment.process();
        LocalDateTime after = LocalDateTime.now();

        assertNotNull(payment.getProcessedAt());
        assertTrue(payment.getProcessedAt().isAfter(before) || payment.getProcessedAt().isEqual(before));
        assertTrue(payment.getProcessedAt().isBefore(after) || payment.getProcessedAt().isEqual(after));
    }

    @Test
    @DisplayName("Should change status to COMPLETED")
    void shouldChangeStatusToCompleted() {
        payment.process();

        assertEquals(PaymentStatus.COMPLETED, payment.getStatus());
    }
}