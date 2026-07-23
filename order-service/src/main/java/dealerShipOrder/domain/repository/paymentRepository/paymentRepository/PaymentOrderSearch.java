package dealerShipOrder.domain.repository.paymentRepository.paymentRepository;

import dealerShipOrder.domain.models.payment.Payment;

import java.util.List;

public interface PaymentOrderSearch {
    List<Payment> findByOrderId(String orderId);
    List<Payment> findAllByOrderId(String orderId);
    boolean existsByOrderId(String orderId);
}
