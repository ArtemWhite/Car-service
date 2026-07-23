package domainTest.payment.paymentCreation;

import domain.models.payment.Payment;
import domain.models.payment.PaymentMethod;
import domain.models.payment.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class PaymentCreationTest {

    @Test
    @DisplayName("Should successfully create payment with all fields")
    void shouldCreatePaymentWithAllFields() {
        String id = "pay123";
        String orderId = "order456";
        String clientId = "client789";
        double amount = 3500000;
        PaymentMethod method = PaymentMethod.CARD;

        Payment payment = new Payment(id, orderId, clientId, amount, method);

        assertNotNull(payment);
        assertEquals(id, payment.getId());
        assertEquals(orderId, payment.getOrderId());
        assertEquals(clientId, payment.getClientId());
        assertEquals(amount, payment.getAmount());
        assertEquals(method, payment.getMethod());
        assertEquals(PaymentStatus.PENDING, payment.getStatus());
        assertNotNull(payment.getCreatedAt());
        assertNull(payment.getProcessedAt());
        assertNull(payment.getTransactionId());
        assertNull(payment.getFailureReason());
    }

    @Test
    @DisplayName("Should have PENDING as default status")
    void shouldHavePendingAsDefaultStatus() {
        Payment payment = new Payment("pay123", "order456", "client789", 3500000, PaymentMethod.CARD);

        assertEquals(PaymentStatus.PENDING, payment.getStatus());
    }

    @Test
    @DisplayName("Should set createdAt to current time")
    void shouldSetCreatedAtToCurrentTime() {
        LocalDateTime before = LocalDateTime.now();

        Payment payment = new Payment("pay123", "order456", "client789", 3500000, PaymentMethod.CASH);
        LocalDateTime after = LocalDateTime.now();

        assertNotNull(payment.getCreatedAt());
        assertTrue(payment.getCreatedAt().isAfter(before) || payment.getCreatedAt().isEqual(before));
        assertTrue(payment.getCreatedAt().isBefore(after) || payment.getCreatedAt().isEqual(after));
    }

    @Test
    @DisplayName("Should create payment with different amounts")
    void shouldCreatePaymentWithDifferentAmounts() {
        Payment payment1 = new Payment("pay1", "order1", "client1", 1000, PaymentMethod.CARD);
        Payment payment2 = new Payment("pay2", "order2", "client2", 999999.99, PaymentMethod.CASH);

        assertEquals(1000, payment1.getAmount(), 0.001);
        assertEquals(999999.99, payment2.getAmount(), 0.001);
    }

    @Test
    @DisplayName("Should create payment with different methods")
    void shouldCreatePaymentWithDifferentMethods() {
        Payment cardPayment = new Payment("pay1", "order1", "client1", 1000, PaymentMethod.CARD);
        Payment cashPayment = new Payment("pay2", "order2", "client2", 2000, PaymentMethod.CASH);
        Payment onlinePayment = new Payment("pay3", "order3", "client3", 3000, PaymentMethod.ONLINE);
        Payment installmentPayment = new Payment("pay4", "order4", "client4", 4000, PaymentMethod.INSTALLMENT);

        assertEquals(PaymentMethod.CARD, cardPayment.getMethod());
        assertEquals(PaymentMethod.CASH, cashPayment.getMethod());
        assertEquals(PaymentMethod.ONLINE, onlinePayment.getMethod());
        assertEquals(PaymentMethod.INSTALLMENT, installmentPayment.getMethod());
    }
}