package dealerShipOrder.infrastructure.adapters.orderAdapters.orderReferencesAdapters;

import dealerShipOrder.domain.models.order.Order;
import dealerShipOrder.domain.models.order.OrderStatus;
import dealerShipOrder.infrastructure.jpaRepository.orderJpaRepository.OrderJpaRepository;
import dealerShipOrder.infrastructure.mappers.orderEntitiesMappers.OrderEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderClientAdapter {

    private final OrderJpaRepository jpaRepository;
    private final OrderEntityMapper mapper;

    public List<Order> findByClientId(String clientId) {
        return jpaRepository.findByClientId(clientId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public List<Order> findByClientIdAndStatus(String clientId, OrderStatus status) {
        return jpaRepository.findByClientIdAndStatus(clientId, status.name()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public long countByClientId(String clientId) {
        return jpaRepository.countByClientId(clientId);
    }
}