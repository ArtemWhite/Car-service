package dealerShipOrder.application.services.paymentService.client;

import dealerShipOrder.application.dtos.request.paymentRequest.CreatePaymentRequest;
import dealerShipOrder.application.dtos.request.paymentRequest.ProcessPaymentRequest;
import dealerShipOrder.application.dtos.response.paymentResponse.PaymentResponse;

import java.util.List;

public interface PaymentClientService {
    PaymentResponse createPayment(CreatePaymentRequest request);
    PaymentResponse processPayment(String paymentId, ProcessPaymentRequest request);
    PaymentResponse getPaymentStatus(String paymentId);
    List<PaymentResponse> getMyPayments();
}