package infrastructure.mappers;

import domain.models.assembly.AssemblyOrder;
import infrastructure.entities.AssemblyOrderEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class AssemblyOrderMapper {

    public AssemblyOrderEntity toEntity(AssemblyOrder domain) {
        if (domain == null) return null;

        AssemblyOrderEntity entity = new AssemblyOrderEntity();
        entity.setId(UUID.fromString(domain.getId()));
        entity.setSourceOrderId(domain.getSourceOrderId());
        entity.setOrderType(domain.getOrderType());
        entity.setCarId(domain.getCarId());
        entity.setConfigurationId(domain.getConfigurationId());
        entity.setCarModelId(domain.getCarModelId());
        entity.setStatus(domain.getStatus());
        entity.setResponsibleWarehouseAdminId(domain.getResponsibleWarehouseAdminId());
        entity.setCreatedAt(domain.getCreatedAt() != null ? domain.getCreatedAt() : Instant.now());
        entity.setUpdatedAt(domain.getUpdatedAt() != null ? domain.getUpdatedAt() : Instant.now());
        entity.setRemoved(domain.isRemoved());
        return entity;
    }

    public AssemblyOrder toDomain(AssemblyOrderEntity entity) {
        if (entity == null) return null;

        return AssemblyOrder.builder()
                .id(entity.getId().toString())
                .sourceOrderId(entity.getSourceOrderId())
                .orderType(entity.getOrderType())
                .carId(entity.getCarId())
                .configurationId(entity.getConfigurationId())
                .carModelId(entity.getCarModelId())
                .status(entity.getStatus())
                .responsibleWarehouseAdminId(entity.getResponsibleWarehouseAdminId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .removed(entity.isRemoved())
                .build();
    }
}