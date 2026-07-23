package dealerShipOrder.infrastructure.adapters.paymentAdapters.paymentReferencesAdapters;

import dealerShipOrder.domain.models.payment.Payment;
import dealerShipOrder.infrastructure.jpaRepository.paymentJpaRepositories.PaymentJpaRepository;
import dealerShipOrder.infrastructure.mappers.paymentEntitiesMappers.PaymentEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PaymentOrderAdapter {

    private final PaymentJpaRepository jpaRepository;
    private final PaymentEntityMapper mapper;

    public List<Payment> findByOrderId(String orderId) {
        return jpaRepository.findByOrderId(orderId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public List<Payment> findAllByOrderId(String orderId) {
        return jpaRepository.findPaymentsByOrderIdDesc(orderId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public boolean existsByOrderId(String orderId) {
        return jpaRepository.existsByOrderId(orderId);
    }
}