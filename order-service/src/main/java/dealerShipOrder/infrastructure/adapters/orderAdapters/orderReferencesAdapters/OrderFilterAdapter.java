package dealerShipOrder.infrastructure.adapters.orderAdapters.orderReferencesAdapters;

import dealerShipOrder.domain.models.order.Order;
import dealerShipOrder.domain.models.order.OrderStatus;
import dealerShipOrder.domain.models.order.OrderType;
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
public class OrderFilterAdapter {

    private final OrderJpaRepository jpaRepository;
    private final OrderEntityMapper mapper;

    private Instant toInstant(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.atZone(ZoneId.systemDefault()).toInstant();
    }

    public List<Order> findOrdersWithFilters(
            OrderStatus status,
            OrderType type,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            String clientId,
            String managerId) {

        String statusName = status != null ? status.name() : null;
        String typeName = type != null ? type.name() : null;

        Instant instantFrom = dateFrom != null ? toInstant(dateFrom) : Instant.ofEpochSecond(0);
        Instant instantTo = dateTo != null ? toInstant(dateTo) : Instant.now().plusSeconds(315360000); // +10 лет

        return jpaRepository.findOrdersWithFilters(statusName, typeName, instantFrom, instantTo, clientId, managerId)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}