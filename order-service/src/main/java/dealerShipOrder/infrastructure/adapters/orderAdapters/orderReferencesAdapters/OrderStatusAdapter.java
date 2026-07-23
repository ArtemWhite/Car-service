package dealerShipOrder.infrastructure.adapters.orderAdapters.orderReferencesAdapters;

import dealerShipOrder.domain.models.order.*;
import dealerShipOrder.infrastructure.jpaRepository.orderJpaRepository.OrderJpaRepository;
import dealerShipOrder.infrastructure.mappers.orderEntitiesMappers.OrderEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderStatusAdapter {

    private final OrderJpaRepository jpaRepository;
    private final OrderEntityMapper mapper;

    public List<Order> findByStatus(OrderStatus status) {
        return jpaRepository.findByStatus(status.name()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public List<Order> findByStatusIn(List<OrderStatus> statuses) {
        List<String> statusNames = statuses.stream()
                .map(OrderStatus::name)
                .collect(Collectors.toList());
        return jpaRepository.findByStatusIn(statusNames).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public long countByStatus(OrderStatus status) {
        return jpaRepository.countByStatus(status.name());
    }

    public List<Order> findActiveOrders() {
        return jpaRepository.findActiveOrders().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public long countByOrderType(String orderType) {
        return jpaRepository.countByOrderType(orderType);
    }
}