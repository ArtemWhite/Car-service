package dealerShipOrder.infrastructure.adapters.userAdapters.userReferencesAdapters;

import dealerShipOrder.domain.models.users.warehouseAdmin.StockOperation;
import dealerShipOrder.infrastructure.entities.userEntities.warehouseAdminEntities.StockOperationEntity;
import dealerShipOrder.infrastructure.jpaRepository.userJpaRepositories.warehouseAdminJpaRepositories.StockOperationJpaRepository;
import dealerShipOrder.infrastructure.mappers.userEntitiesMappers.userMappers.warehouseAdminEntitiesMappers.StockOperationEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class StockOperationAdapter {

    private final StockOperationJpaRepository jpaRepository;
    private final StockOperationEntityMapper mapper;

    public StockOperation save(StockOperation operation) {
        StockOperationEntity entity = mapper.toEntity(operation);
        StockOperationEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    public List<StockOperation> findByAdminId(String adminId) {
        try {
            UUID uuid = UUID.fromString(adminId);
            return jpaRepository.findByAdminId(uuid).stream()
                    .map(mapper::toDomain)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            return List.of();
        }
    }

    public List<StockOperation> findByOperationType(String type) {
        return jpaRepository.findByOperationType(type).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public List<StockOperation> findByDateRange(LocalDateTime start, LocalDateTime end) {
        Instant startInstant = start != null ? start.atZone(ZoneId.systemDefault()).toInstant() : null;
        Instant endInstant = end != null ? end.atZone(ZoneId.systemDefault()).toInstant() : null;

        return jpaRepository.findByDateRange(startInstant, endInstant).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public List<StockOperation> findByItemId(String itemId) {
        return jpaRepository.findByItemId(itemId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}