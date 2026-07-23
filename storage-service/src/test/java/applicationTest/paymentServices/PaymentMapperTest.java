package applicationTest.paymentServices;

import application.dtos.request.paymentRequest.CreatePaymentRequest;
import application.dtos.request.paymentRequest.ProcessPaymentRequest;
import application.dtos.response.paymentResponse.PaymentResponse;
import application.mapper.PaymentMapper;
import domain.models.payment.Payment;
import domain.models.payment.PaymentMethod;
import domain.models.payment.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PaymentMapper Tests")
class PaymentMapperTest {

    private PaymentMapper paymentMapper;
    private Payment payment;

    @BeforeEach
    void setUp() {
        paymentMapper = new PaymentMapper();
        payment = new Payment("pay123", "order123", "client123", 3500000, PaymentMethod.CARD);
    }

    @Test
    @DisplayName("Should convert CreatePaymentRequest to Payment")
    void shouldConvertCreatePaymentRequestToPayment() {
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setOrderId("order123");
        request.setClientId("client123");
        request.setAmount(3500000.0);
        request.setMethod("CARD");

        Payment result = paymentMapper.toDomain(request);

        assertNotNull(result);
        assertEquals("order123", result.getOrderId());
        assertEquals("client123", result.getClientId());
        assertEquals(3500000, result.getAmount());
        assertEquals(PaymentMethod.CARD, result.getMethod());
        assertEquals(PaymentStatus.PENDING, result.getStatus());
        assertNotNull(result.getCreatedAt());
    }

    @Test
    @DisplayName("Should generate ID for new payment")
    void shouldGenerateIdForNewPayment() {
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setOrderId("order123");
        request.setClientId("client123");
        request.setAmount(3500000.0);
        request.setMethod("CARD");

        Payment result = paymentMapper.toDomain(request);

        assertNotNull(result.getId());
        assertFalse(result.getId().isBlank());
        assertFalse(result.getId().isEmpty());
    }

    @Test
    @DisplayName("Should convert Payment to PaymentResponse")
    void shouldConvertPaymentToPaymentResponse() {
        PaymentResponse response = paymentMapper.toResponse(payment);

        assertNotNull(response);
        assertEquals("pay123", response.getId());
        assertEquals("order123", response.getOrderId());
        assertEquals("client123", response.getClientId());
        assertEquals(3500000, response.getAmount());
        assertEquals("CARD", response.getMethod());
        assertEquals("PENDING", response.getStatus());
        assertTrue(response.isPending());
        assertFalse(response.isCompleted());
        assertFalse(response.isFailed());
        assertFalse(response.isRefunded());
    }

    @Test
    @DisplayName("Should format amount with currency")
    void shouldFormatAmountWithCurrency() {
        PaymentResponse response = paymentMapper.toResponse(payment);

        assertNotNull(response.getAmountFormatted());
        String formatted = response.getAmountFormatted().replace("\u00A0", " ");
        assertTrue(formatted.contains("3 500 000"));
        assertTrue(formatted.contains("₽"));
    }

    @Test
    @DisplayName("Should set method display name correctly")
    void shouldSetMethodDisplayName() {
        Payment cashPayment = new Payment("pay1", "order1", "client1", 1000, PaymentMethod.CASH);
        PaymentResponse cashResponse = paymentMapper.toResponse(cashPayment);
        assertEquals("Наличные", cashResponse.getMethodDisplayName());

        Payment cardPayment = new Payment("pay2", "order2", "client2", 2000, PaymentMethod.CARD);
        PaymentResponse cardResponse = paymentMapper.toResponse(cardPayment);
        assertEquals("Банковская карта", cardResponse.getMethodDisplayName());

        Payment onlinePayment = new Payment("pay3", "order3", "client3", 3000, PaymentMethod.ONLINE);
        PaymentResponse onlineResponse = paymentMapper.toResponse(onlinePayment);
        assertEquals("Онлайн-оплата", onlineResponse.getMethodDisplayName());

        Payment installmentPayment = new Payment("pay4", "order4", "client4", 4000, PaymentMethod.INSTALLMENT);
        PaymentResponse installmentResponse = paymentMapper.toResponse(installmentPayment);
        assertEquals("Рассрочка", installmentResponse.getMethodDisplayName());
    }

    @Test
    @DisplayName("Should set status display name correctly")
    void shouldSetStatusDisplayName() {
        Payment pendingPayment = new Payment("pay1", "order1", "client1", 1000, PaymentMethod.CARD);
        PaymentResponse pendingResponse = paymentMapper.toResponse(pendingPayment);
        assertEquals("Ожидает оплаты", pendingResponse.getStatusDisplayName());

        Payment completedPayment = new Payment("pay2", "order2", "client2", 2000, PaymentMethod.CARD);
        completedPayment.process();
        PaymentResponse completedResponse = paymentMapper.toResponse(completedPayment);
        assertEquals("Оплачен", completedResponse.getStatusDisplayName());

        Payment failedPayment = new Payment("pay3", "order3", "client3", 3000, PaymentMethod.CARD);
        failedPayment.fail("Error");
        PaymentResponse failedResponse = paymentMapper.toResponse(failedPayment);
        assertEquals("Ошибка оплаты", failedResponse.getStatusDisplayName());

        Payment refundedPayment = new Payment("pay4", "order4", "client4", 4000, PaymentMethod.CARD);
        refundedPayment.process();
        refundedPayment.refund();
        PaymentResponse refundedResponse = paymentMapper.toResponse(refundedPayment);
        assertEquals("Возврат", refundedResponse.getStatusDisplayName());
    }

    @Test
    @DisplayName("Should set status flags correctly for pending payment")
    void shouldSetStatusFlagsForPending() {
        PaymentResponse response = paymentMapper.toResponse(payment);

        assertTrue(response.isPending());
        assertFalse(response.isProcessing());
        assertFalse(response.isCompleted());
        assertFalse(response.isFailed());
        assertFalse(response.isRefunded());
    }

    @Test
    @DisplayName("Should set status flags correctly for completed payment")
    void shouldSetStatusFlagsForCompleted() {
        payment.process();
        PaymentResponse response = paymentMapper.toResponse(payment);

        assertFalse(response.isPending());
        assertFalse(response.isProcessing());
        assertTrue(response.isCompleted());
        assertFalse(response.isFailed());
        assertFalse(response.isRefunded());
    }

    @Test
    @DisplayName("Should set status flags correctly for failed payment")
    void shouldSetStatusFlagsForFailed() {
        payment.fail("Error");
        PaymentResponse response = paymentMapper.toResponse(payment);

        assertFalse(response.isPending());
        assertFalse(response.isProcessing());
        assertFalse(response.isCompleted());
        assertTrue(response.isFailed());
        assertFalse(response.isRefunded());
    }

    @Test
    @DisplayName("Should set status flags correctly for refunded payment")
    void shouldSetStatusFlagsForRefunded() {
        payment.process();
        payment.refund();
        PaymentResponse response = paymentMapper.toResponse(payment);

        assertFalse(response.isPending());
        assertFalse(response.isProcessing());
        assertFalse(response.isCompleted());
        assertFalse(response.isFailed());
        assertTrue(response.isRefunded());
    }

    @Test
    @DisplayName("Should convert list of Payments to list of PaymentResponses")
    void shouldConvertListOfPaymentsToListOfResponses() {
        Payment payment2 = new Payment("pay456", "order456", "client456", 2000, PaymentMethod.CASH);
        List<Payment> payments = List.of(payment, payment2);

        List<PaymentResponse> responses = paymentMapper.toResponseList(payments);

        assertNotNull(responses);
        assertEquals(2, responses.size());
    }

    @Test
    @DisplayName("Should return empty list for empty input")
    void shouldReturnEmptyListForEmptyInput() {
        List<Payment> payments = List.of();

        List<PaymentResponse> responses = paymentMapper.toResponseList(payments);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    @DisplayName("Should update payment to completed when success is true")
    void shouldUpdatePaymentToCompletedWhenSuccessTrue() {
        ProcessPaymentRequest request = new ProcessPaymentRequest();
        request.setSuccess(true);
        request.setTransactionId("TXN123");

        paymentMapper.updateFromProcessRequest(payment, request);

        assertEquals(PaymentStatus.COMPLETED, payment.getStatus());
        assertNotNull(payment.getTransactionId());
        assertNotNull(payment.getProcessedAt());
        assertNull(payment.getFailureReason());
    }

    @Test
    @DisplayName("Should update payment to failed when success is false")
    void shouldUpdatePaymentToFailedWhenSuccessFalse() {
        ProcessPaymentRequest request = new ProcessPaymentRequest();
        request.setSuccess(false);
        request.setPaymentDetails("Insufficient funds");

        paymentMapper.updateFromProcessRequest(payment, request);

        assertEquals(PaymentStatus.FAILED, payment.getStatus());
        assertNull(payment.getTransactionId());
        assertNotNull(payment.getProcessedAt());
        assertEquals("Insufficient funds", payment.getFailureReason());
    }

    @Test
    @DisplayName("Should use default failure message when paymentDetails is null")
    void shouldUseDefaultFailureMessage() {
        ProcessPaymentRequest request = new ProcessPaymentRequest();
        request.setSuccess(false);
        request.setPaymentDetails(null);

        paymentMapper.updateFromProcessRequest(payment, request);

        assertEquals(PaymentStatus.FAILED, payment.getStatus());
        assertEquals("Payment failed", payment.getFailureReason());
    }

    @Test
    @DisplayName("Should format amount with ruble symbol")
    void shouldFormatAmountWithRubleSymbol() {
        PaymentResponse response = paymentMapper.toResponse(payment);

        assertTrue(response.getAmountFormatted().contains("₽"));
    }

    @Test
    @DisplayName("Should format amount with spaces for thousands")
    void shouldFormatAmountWithSpaces() {
        PaymentResponse response = paymentMapper.toResponse(payment);

        String formatted = response.getAmountFormatted().replace("\u00A0", " ");
        assertTrue(formatted.contains("3 500 000"));
    }
}