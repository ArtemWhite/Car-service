package dealerShipOrder.domain.repository.paymentRepository.paymentRepository;

import dealerShipOrder.domain.models.payment.Payment;
import dealerShipOrder.domain.models.payment.PaymentStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentDateSearch {
    List<Payment> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    List<Payment> findByProcessedAtBetween(LocalDateTime start, LocalDateTime end);
    List<Payment> findByDateRangeAndStatus(LocalDateTime start, LocalDateTime end, PaymentStatus status);
}
