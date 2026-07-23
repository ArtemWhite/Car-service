package dealerShipOrder.infrastructure.adapters.paymentAdapters.paymentReferencesAdapters;

import dealerShipOrder.domain.models.payment.*;
import dealerShipOrder.infrastructure.jpaRepository.paymentJpaRepositories.PaymentJpaRepository;
import dealerShipOrder.infrastructure.mappers.paymentEntitiesMappers.PaymentEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PaymentDateAdapter {

    private final PaymentJpaRepository jpaRepository;
    private final PaymentEntityMapper mapper;

    private Instant toInstant(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.atZone(ZoneId.systemDefault()).toInstant();
    }

    public List<Payment> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end) {
        return jpaRepository.findByCreatedAtBetween(toInstant(start), toInstant(end)).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public List<Payment> findByProcessedAtBetween(LocalDateTime start, LocalDateTime end) {
        return jpaRepository.findByProcessedAtBetween(toInstant(start), toInstant(end)).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public List<Payment> findByDateRangeAndStatus(LocalDateTime start, LocalDateTime end, PaymentStatus status) {
        return jpaRepository.findByDateRangeAndStatus(toInstant(start), toInstant(end), status.name()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}