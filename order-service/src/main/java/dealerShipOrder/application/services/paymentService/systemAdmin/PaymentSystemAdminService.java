package dealerShipOrder.application.services.paymentService.systemAdmin;

import dealerShipOrder.application.dtos.response.paymentResponse.PaymentResponse;

import java.util.List;

public interface PaymentSystemAdminService {
    PaymentResponse refundPayment(String paymentId, String reason);
    List<PaymentResponse> getPaymentsByStatus(String status);
    List<PaymentResponse> getPaymentsByDateRange(String from, String to);
}