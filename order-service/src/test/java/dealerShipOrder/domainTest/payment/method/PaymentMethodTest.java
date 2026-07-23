package dealerShipOrder.domainTest.payment.method;

import dealerShipOrder.domain.models.payment.PaymentMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;

class PaymentMethodTest {

    @Test
    @DisplayName("Should have all payment methods")
    void shouldHaveAllMethods() {
        PaymentMethod[] methods = PaymentMethod.values();

        assertEquals(4, methods.length);
        assertEquals(PaymentMethod.CASH, methods[0]);
        assertEquals(PaymentMethod.CARD, methods[1]);
        assertEquals(PaymentMethod.ONLINE, methods[2]);
        assertEquals(PaymentMethod.INSTALLMENT, methods[3]);
    }

    @Test
    @DisplayName("Should maintain correct order")
    void shouldMaintainCorrectOrder() {
        assertEquals(0, PaymentMethod.CASH.ordinal());
        assertEquals(1, PaymentMethod.CARD.ordinal());
        assertEquals(2, PaymentMethod.ONLINE.ordinal());
        assertEquals(3, PaymentMethod.INSTALLMENT.ordinal());
    }

    @Test
    @DisplayName("Should convert from string correctly")
    void shouldConvertFromString() {
        assertEquals(PaymentMethod.CASH, PaymentMethod.valueOf("CASH"));
        assertEquals(PaymentMethod.CARD, PaymentMethod.valueOf("CARD"));
        assertEquals(PaymentMethod.ONLINE, PaymentMethod.valueOf("ONLINE"));
        assertEquals(PaymentMethod.INSTALLMENT, PaymentMethod.valueOf("INSTALLMENT"));
    }
}
