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
public class OrderManagerAdapter {

    private final OrderJpaRepository jpaRepository;
    private final OrderEntityMapper mapper;

    public List<Order> findByManagerId(String managerId) {
        return jpaRepository.findByManagerId(managerId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public List<Order> findByManagerIdAndStatus(String managerId, OrderStatus status) {
        return jpaRepository.findByManagerIdAndStatus(managerId, status.name()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public long countByManagerId(String managerId) {
        return jpaRepository.countByManagerId(managerId);
    }
}