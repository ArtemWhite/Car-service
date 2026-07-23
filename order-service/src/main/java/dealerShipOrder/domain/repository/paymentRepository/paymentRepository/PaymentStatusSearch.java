package dealerShipOrder.domain.repository.paymentRepository.paymentRepository;

import dealerShipOrder.domain.models.payment.Payment;
import dealerShipOrder.domain.models.payment.PaymentStatus;

import java.util.List;

public interface PaymentStatusSearch {
    List<Payment> findByStatus(PaymentStatus status);
    List<Payment> findByStatusIn(List<PaymentStatus> statuses);
    long countByStatus(PaymentStatus status);
}
