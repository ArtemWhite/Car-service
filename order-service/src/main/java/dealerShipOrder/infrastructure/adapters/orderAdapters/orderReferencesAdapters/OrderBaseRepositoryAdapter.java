package dealerShipOrder.infrastructure.adapters.orderAdapters.orderReferencesAdapters;

import dealerShipOrder.domain.models.order.Order;
import dealerShipOrder.infrastructure.entities.orderEntities.OrderEntity;
import dealerShipOrder.infrastructure.jpaRepository.orderJpaRepository.*;
import dealerShipOrder.infrastructure.jpaRepository.orderJpaRepository.referenceOrderJpaRepositories.*;
import dealerShipOrder.infrastructure.mappers.orderEntitiesMappers.OrderEntityMapper;
import dealerShipOrder.infrastructure.mappers.orderEntitiesMappers.OrderHistoryEntryEntityMapper;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Transactional
public class OrderBaseRepositoryAdapter {

    private final OrderJpaRepository jpaRepository;
    private final OrderHistoryEntryJpaRepository historyJpaRepository;
    private final OrderEntityMapper mapper;
    private final OrderHistoryEntryEntityMapper historyMapper;

    @Transactional
    public Order save(Order order) {
        System.out.println("Order history size BEFORE save: " + order.getHistory().size());

        OrderEntity entity = mapper.toEntity(order);
        OrderEntity saved = jpaRepository.save(entity);

        OrderEntity withHistory = jpaRepository.findOrderByIdAndRemovedFalse(saved.getId())
                .orElseThrow();
        Hibernate.initialize(withHistory.getHistory());

        Order result = mapper.toDomain(withHistory);
        System.out.println("Result history size: " + result.getHistory().size());

        return result;
    }

    public Optional<Order> findById(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            return jpaRepository.findOrderByIdAndRemovedFalse(uuid)
                    .map(mapper::toDomain);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public List<Order> findAll() {
        return jpaRepository.findAllOrdersByRemovedFalse().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public void delete(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            jpaRepository.softDelete(uuid);
        } catch (IllegalArgumentException e) {
        }
    }

    public boolean existsById(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            return jpaRepository.findById(uuid).isPresent();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}