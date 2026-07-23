package dealerShipOrder.application.services.paymentService;

import dealerShipOrder.application.dtos.response.paymentResponse.PaymentResponse;

import java.util.List;

public interface PaymentService
{
    PaymentResponse getPaymentById(String id);
    List<PaymentResponse> getAllPayments();
    List<PaymentResponse> getPaymentsByOrderId(String orderId);
}
