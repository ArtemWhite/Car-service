package dealerShipOrder.domain.repository.paymentRepository.paymentRepository;

import dealerShipOrder.domain.models.payment.Payment;
import dealerShipOrder.domain.repository.BaseRepository;

public interface PaymentRepository extends
        BaseRepository<Payment>,
        PaymentOrderSearch,
        PaymentClientSearch,
        PaymentStatusSearch,
        PaymentMethodSearch,
        PaymentDateSearch {
}
