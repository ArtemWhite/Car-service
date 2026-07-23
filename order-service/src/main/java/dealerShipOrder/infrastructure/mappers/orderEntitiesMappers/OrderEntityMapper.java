package dealerShipOrder.infrastructure.mappers.orderEntitiesMappers;

import dealerShipOrder.domain.models.order.*;
import dealerShipOrder.infrastructure.entities.orderEntities.OrderEntity;
import dealerShipOrder.infrastructure.entities.orderEntities.OrderHistoryEntryEntity;
import dealerShipOrder.infrastructure.entities.orderEntities.referenceOrderEntities.OrderStatusEntity;
import dealerShipOrder.infrastructure.entities.orderEntities.referenceOrderEntities.OrderTypeEntity;
import dealerShipOrder.infrastructure.jpaRepository.orderJpaRepository.referenceOrderJpaRepositories.OrderStatusReferenceJpaRepository;
import dealerShipOrder.infrastructure.jpaRepository.orderJpaRepository.referenceOrderJpaRepositories.OrderTypeReferenceJpaRepository;
import org.hibernate.Hibernate;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class OrderEntityMapper {

    @Autowired
    protected OrderTypeReferenceJpaRepository orderTypeRepository;

    @Autowired
    protected OrderHistoryEntryEntityMapper historyMapper;

    @Autowired
    protected OrderStatusReferenceJpaRepository orderStatusRepository;

    public OrderEntity toEntity(Order order) {
        if (order == null) return null;

        OrderEntity entity = new OrderEntity();
        entity.setId(toUuid(order.getId()));
        entity.setClientId(order.getClientId());
        entity.setManagerId(order.getManagerId());
        entity.setType(toOrderTypeEntity(order.getType()));
        entity.setStatus(toOrderStatusEntity(order.getStatus()));
        entity.setCompletedAt(toInstant(order.getCompletedAt()));
        entity.setCarId(order.getCarId());
        entity.setConfigurationId(order.getConfigurationId());
        entity.setCarModelId(order.getCarModelId());
        entity.setNotes(order.getNotes());
        entity.setCreatedAt(toInstant(order.getCreatedAt()));
        entity.setUpdatedAt(toInstant(order.getUpdatedAt()));
        entity.setRemoved(false);

        if (order.getHistory() != null && !order.getHistory().isEmpty()) {
            List<OrderHistoryEntryEntity> historyEntities = new ArrayList<>();
            for (OrderHistoryEntry entry : order.getHistory()) {
                OrderHistoryEntryEntity historyEntity = historyMapper.toEntity(entry);
                historyEntity.setOrder(entity);
                historyEntities.add(historyEntity);
            }
            entity.setHistory(historyEntities);
        }

        return entity;
    }

    public Order toDomain(OrderEntity entity) {
        if (entity == null) return null;

        Hibernate.initialize(entity.getStatus());
        Hibernate.initialize(entity.getType());
        Hibernate.initialize(entity.getHistory());

        Order order;
        OrderType orderType = toOrderType(entity.getType());

        if (orderType == OrderType.IN_STOCK) {
            order = Order.createInStockOrder(
                    entity.getId().toString(),
                    entity.getClientId(),
                    entity.getCarId()
            );
        } else {
            order = Order.createCustomOrder(
                    entity.getId().toString(),
                    entity.getClientId(),
                    entity.getConfigurationId(),
                    entity.getCarModelId()
            );
        }

        restoreManagerId(order, entity.getManagerId());

        OrderStatusEntity statusEntity = entity.getStatus();
        if (statusEntity != null) {
            OrderStatus status = OrderStatus.valueOf(statusEntity.getName());
            restoreStatus(order, status);
        } else {
            restoreStatus(order, OrderStatus.CREATED);
        }

        restoreCompletedAt(order, toLocalDateTime(entity.getCompletedAt()));
        restoreNotes(order, entity.getNotes());
        restoreCreatedAt(order, toLocalDateTime(entity.getCreatedAt()));
        restoreUpdatedAt(order, toLocalDateTime(entity.getUpdatedAt()));

        if (entity.getHistory() != null && !entity.getHistory().isEmpty()) {
            List<OrderHistoryEntry> history = entity.getHistory().stream()
                    .map(historyMapper::toDomain)
                    .collect(Collectors.toList());
            restoreHistory(order, history);
        } else {
            restoreHistory(order, new ArrayList<>());
        }

        return order;
    }

    private void restoreHistory(Order order, List<OrderHistoryEntry> history) {
        try {
            java.lang.reflect.Field field = Order.class.getDeclaredField("history");
            field.setAccessible(true);
            field.set(order, new ArrayList<>(history));
        } catch (Exception e) {
            throw new RuntimeException("Failed to restore history", e);
        }
    }

    protected String toUuid(UUID uuid) {
        return uuid == null ? null : uuid.toString();
    }

    protected UUID toUuid(String id) {
        return id == null ? null : UUID.fromString(id);
    }

    protected Instant toInstant(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.atZone(ZoneId.systemDefault()).toInstant();
    }

    protected LocalDateTime toLocalDateTime(Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    protected OrderTypeEntity toOrderTypeEntity(OrderType type) {
        if (type == null) return null;
        return orderTypeRepository.findByName(type.name())
                .orElseThrow(() -> new RuntimeException("Order type not found: " + type.name()));
    }

    protected OrderType toOrderType(OrderTypeEntity entity) {
        if (entity == null) return null;
        return OrderType.valueOf(entity.getName());
    }

    protected OrderStatusEntity toOrderStatusEntity(OrderStatus status) {
        if (status == null) return null;
        return orderStatusRepository.findByName(status.name())
                .orElseThrow(() -> new RuntimeException("Order status not found: " + status.name()));
    }

    protected OrderStatus toOrderStatus(OrderStatusEntity entity) {
        if (entity == null) return null;
        return OrderStatus.valueOf(entity.getName());
    }

    private void restoreManagerId(Order order, String managerId) {
        try {
            java.lang.reflect.Field field = Order.class.getDeclaredField("managerId");
            field.setAccessible(true);
            field.set(order, managerId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to restore managerId", e);
        }
    }

    private void restoreStatus(Order order, OrderStatus status) {
        if (status == null) {
            System.err.println("WARNING: Status is null for order " + order.getId());
            return;
        }
        try {
            java.lang.reflect.Field field = Order.class.getDeclaredField("status");
            field.setAccessible(true);
            field.set(order, status);
        } catch (Exception e) {
            throw new RuntimeException("Failed to restore status", e);
        }
    }

    private void restoreCompletedAt(Order order, LocalDateTime completedAt) {
        try {
            java.lang.reflect.Field field = Order.class.getDeclaredField("completedAt");
            field.setAccessible(true);
            field.set(order, completedAt);
        } catch (Exception e) {
            throw new RuntimeException("Failed to restore completedAt", e);
        }
    }

    private void restoreNotes(Order order, String notes) {
        try {
            java.lang.reflect.Field field = Order.class.getDeclaredField("notes");
            field.setAccessible(true);
            field.set(order, notes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to restore notes", e);
        }
    }

    private void restoreCreatedAt(Order order, LocalDateTime createdAt) {
        try {
            java.lang.reflect.Field field = Order.class.getDeclaredField("createdAt");
            field.setAccessible(true);
            field.set(order, createdAt);
        } catch (Exception e) {
            throw new RuntimeException("Failed to restore createdAt", e);
        }
    }

    private void restoreUpdatedAt(Order order, LocalDateTime updatedAt) {
        try {
            java.lang.reflect.Field field = Order.class.getDeclaredField("updatedAt");
            field.setAccessible(true);
            field.set(order, updatedAt);
        } catch (Exception e) {
            throw new RuntimeException("Failed to restore updatedAt", e);
        }
    }
}