package dealerShipOrder.domain.repository.paymentRepository.paymentRepository;

import dealerShipOrder.domain.models.payment.Payment;
import dealerShipOrder.domain.models.payment.PaymentStatus;

import java.util.List;

public interface PaymentClientSearch {
    List<Payment> findByClientId(String clientId);
    List<Payment> findByClientIdAndStatus(String clientId, PaymentStatus status);
}
