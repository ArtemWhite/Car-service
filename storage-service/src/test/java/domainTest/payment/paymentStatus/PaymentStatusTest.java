package domainTest.payment.paymentStatus;

import domain.models.payment.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

class PaymentStatusTest {

    @Test
    @DisplayName("Should have all payment statuses")
    void shouldHaveAllStatuses() {
        PaymentStatus[] statuses = PaymentStatus.values();

        assertEquals(5, statuses.length);
        assertEquals(PaymentStatus.PENDING, statuses[0]);
        assertEquals(PaymentStatus.PROCESSING, statuses[1]);
        assertEquals(PaymentStatus.COMPLETED, statuses[2]);
        assertEquals(PaymentStatus.FAILED, statuses[3]);
        assertEquals(PaymentStatus.REFUNDED, statuses[4]);
    }

    @Test
    @DisplayName("Should maintain correct order")
    void shouldMaintainCorrectOrder() {
        assertEquals(0, PaymentStatus.PENDING.ordinal());
        assertEquals(1, PaymentStatus.PROCESSING.ordinal());
        assertEquals(2, PaymentStatus.COMPLETED.ordinal());
        assertEquals(3, PaymentStatus.FAILED.ordinal());
        assertEquals(4, PaymentStatus.REFUNDED.ordinal());
    }

    @Test
    @DisplayName("Should convert from string correctly")
    void shouldConvertFromString() {
        assertEquals(PaymentStatus.PENDING, PaymentStatus.valueOf("PENDING"));
        assertEquals(PaymentStatus.PROCESSING, PaymentStatus.valueOf("PROCESSING"));
        assertEquals(PaymentStatus.COMPLETED, PaymentStatus.valueOf("COMPLETED"));
        assertEquals(PaymentStatus.FAILED, PaymentStatus.valueOf("FAILED"));
        assertEquals(PaymentStatus.REFUNDED, PaymentStatus.valueOf("REFUNDED"));
    }

    @Test
    @DisplayName("Should represent payment lifecycle")
    void shouldRepresentPaymentLifecycle() {
        assertEquals(PaymentStatus.PENDING, PaymentStatus.PENDING);

        assertEquals(PaymentStatus.PROCESSING, PaymentStatus.PROCESSING);

        assertNotEquals(PaymentStatus.COMPLETED, PaymentStatus.FAILED);
        assertNotEquals(PaymentStatus.COMPLETED, PaymentStatus.REFUNDED);
        assertNotEquals(PaymentStatus.FAILED, PaymentStatus.REFUNDED);
    }
}