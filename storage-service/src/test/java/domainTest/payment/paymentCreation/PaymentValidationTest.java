package domainTest.payment.paymentCreation;

import domain.models.payment.Payment;
import domain.models.payment.PaymentMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

class PaymentValidationTest {

    @Test
    @DisplayName("Should create payment with zero amount")
    void shouldCreatePaymentWithZeroAmount() {
        Payment payment = new Payment("pay123", "order456", "client789", 0, PaymentMethod.CARD);

        assertEquals(0, payment.getAmount(), 0.001);
    }

    @Test
    @DisplayName("Should create payment with null ID")
    void shouldCreatePaymentWithNullId() {
        Payment payment = new Payment(null, "order456", "client789", 1000, PaymentMethod.CARD);

        assertNull(payment.getId());
    }

    @Test
    @DisplayName("Should create payment with null orderId")
    void shouldCreatePaymentWithNullOrderId() {
        Payment payment = new Payment("pay123", null, "client789", 1000, PaymentMethod.CARD);

        assertNull(payment.getOrderId());
    }

    @Test
    @DisplayName("Should create payment with null clientId")
    void shouldCreatePaymentWithNullClientId() {
        Payment payment = new Payment("pay123", "order456", null, 1000, PaymentMethod.CARD);

        assertNull(payment.getClientId());
    }

    @Test
    @DisplayName("Should create payment with null method")
    void shouldCreatePaymentWithNullMethod() {
        Payment payment = new Payment("pay123", "order456", "client789", 1000, null);

        assertNull(payment.getMethod());
    }
}
