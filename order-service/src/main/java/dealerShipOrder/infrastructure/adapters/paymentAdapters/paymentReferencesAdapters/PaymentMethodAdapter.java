package dealerShipOrder.infrastructure.adapters.paymentAdapters.paymentReferencesAdapters;

import dealerShipOrder.domain.models.payment.*;
import dealerShipOrder.infrastructure.jpaRepository.paymentJpaRepositories.PaymentJpaRepository;
import dealerShipOrder.infrastructure.mappers.paymentEntitiesMappers.PaymentEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PaymentMethodAdapter {

    private final PaymentJpaRepository jpaRepository;
    private final PaymentEntityMapper mapper;

    public List<Payment> findByMethod(PaymentMethod method) {
        return jpaRepository.findByMethod(method.name()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public List<Payment> findByMethodAndStatus(PaymentMethod method, PaymentStatus status) {
        return jpaRepository.findByMethodAndStatus(method.name(), status.name()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}