package dealerShipOrder.infrastructure.mappers.orderEntitiesMappers;

import dealerShipOrder.domain.models.order.OrderHistoryEntry;
import dealerShipOrder.infrastructure.entities.orderEntities.OrderHistoryEntryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Mapper(componentModel = "spring", imports = {UUID.class})
public abstract class OrderHistoryEntryEntityMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "action", source = "action")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "timestamp", expression = "java(toInstant(entry.getTimestamp()))")
    @Mapping(target = "removed", constant = "false")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract OrderHistoryEntryEntity toEntity(OrderHistoryEntry entry);

    public OrderHistoryEntry toDomain(OrderHistoryEntryEntity entity) {
        if (entity == null) return null;

        String orderId = entity.getOrder() != null ? entity.getOrder().getId().toString() : null;
        String action = entity.getAction();
        String description = entity.getDescription();
        LocalDateTime timestamp = toLocalDateTime(entity.getTimestamp());

        return new OrderHistoryEntry(orderId, action, description, timestamp);
    }

    protected Instant toInstant(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.atZone(ZoneId.systemDefault()).toInstant();
    }

    protected LocalDateTime toLocalDateTime(Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
}