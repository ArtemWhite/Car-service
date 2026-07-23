package dealerShipOrder.domain.repository.paymentRepository.paymentRepository;

import dealerShipOrder.domain.models.payment.Payment;
import dealerShipOrder.domain.models.payment.PaymentMethod;
import dealerShipOrder.domain.models.payment.PaymentStatus;

import java.util.List;

public interface PaymentMethodSearch {
    List<Payment> findByMethod(PaymentMethod method);
    List<Payment> findByMethodAndStatus(PaymentMethod method, PaymentStatus status);
}
