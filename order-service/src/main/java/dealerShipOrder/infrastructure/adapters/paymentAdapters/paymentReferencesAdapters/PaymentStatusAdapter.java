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
public class PaymentStatusAdapter {

    private final PaymentJpaRepository jpaRepository;
    private final PaymentEntityMapper mapper;

    public List<Payment> findByStatus(PaymentStatus status) {
        return jpaRepository.findByStatus(status.name()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public List<Payment> findByStatusIn(List<PaymentStatus> statuses) {
        List<String> statusNames = statuses.stream()
                .map(PaymentStatus::name)
                .collect(Collectors.toList());
        return jpaRepository.findByStatusIn(statusNames).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public long countByStatus(PaymentStatus status) {
        return jpaRepository.countByStatus(status.name());
    }
}