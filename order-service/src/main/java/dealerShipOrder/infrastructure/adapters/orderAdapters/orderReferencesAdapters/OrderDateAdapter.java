package dealerShipOrder.infrastructure.adapters.orderAdapters.orderReferencesAdapters;

import dealerShipOrder.domain.models.order.Order;
import dealerShipOrder.infrastructure.jpaRepository.orderJpaRepository.OrderJpaRepository;
import dealerShipOrder.infrastructure.mappers.orderEntitiesMappers.OrderEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderDateAdapter {

    private final OrderJpaRepository jpaRepository;
    private final OrderEntityMapper mapper;

    private Instant toInstant(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.atZone(ZoneId.systemDefault()).toInstant();
    }

    public List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end) {
        return jpaRepository.findByCreatedAtBetween(toInstant(start), toInstant(end)).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public List<Order> findByCompletedAtBetween(LocalDateTime start, LocalDateTime end) {
        return jpaRepository.findByCompletedAtBetween(toInstant(start), toInstant(end)).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}