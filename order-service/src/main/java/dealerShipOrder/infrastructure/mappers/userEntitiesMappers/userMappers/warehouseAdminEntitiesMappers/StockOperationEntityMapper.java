package dealerShipOrder.infrastructure.mappers.userEntitiesMappers.userMappers.warehouseAdminEntitiesMappers;

import dealerShipOrder.domain.models.users.warehouseAdmin.ItemType;
import dealerShipOrder.domain.models.users.warehouseAdmin.OperationType;
import dealerShipOrder.domain.models.users.warehouseAdmin.StockOperation;
import dealerShipOrder.infrastructure.entities.userEntities.warehouseAdminEntities.StockOperationEntity;
import dealerShipOrder.infrastructure.entities.userEntities.warehouseAdminEntities.WarehouseAdminEntity;
import dealerShipOrder.infrastructure.jpaRepository.userJpaRepositories.warehouseAdminJpaRepositories.ItemTypeJpaRepository;
import dealerShipOrder.infrastructure.jpaRepository.userJpaRepositories.warehouseAdminJpaRepositories.OperationTypeJpaRepository;
import dealerShipOrder.infrastructure.jpaRepository.userJpaRepositories.warehouseAdminJpaRepositories.WarehouseAdminJpaRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Mapper(componentModel = "spring")
public abstract class StockOperationEntityMapper {

    @Autowired
    protected WarehouseAdminJpaRepository warehouseAdminRepository;

    @Autowired
    protected OperationTypeJpaRepository operationTypeRepository;

    @Autowired
    protected ItemTypeJpaRepository itemTypeRepository;

    @Mapping(target = "id", expression = "java(toUuid(operation.getId()))")
    @Mapping(target = "admin", expression = "java(toWarehouseAdminEntity(operation.getAdminId()))")
    @Mapping(target = "type", expression = "java(operation.getType().name())")
    @Mapping(target = "itemId", source = "itemId")
    @Mapping(target = "itemType", expression = "java(operation.getItemType().name())")
    @Mapping(target = "fromSection", source = "fromSection")
    @Mapping(target = "toSection", source = "toSection")
    @Mapping(target = "fromLocation", source = "fromLocation")
    @Mapping(target = "toLocation", source = "toLocation")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "reason", source = "reason")
    @Mapping(target = "documentNumber", source = "documentNumber")
    @Mapping(target = "timestamp", expression = "java(toInstant(operation.getTimestamp()))")
    @Mapping(target = "removed", constant = "false")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract StockOperationEntity toEntity(StockOperation operation);

    protected WarehouseAdminEntity toWarehouseAdminEntity(String adminId) {
        if (adminId == null) return null;
        try {
            UUID uuid = UUID.fromString(adminId);
            return warehouseAdminRepository.findById(uuid)
                    .orElseThrow(() -> new RuntimeException("WarehouseAdmin not found: " + adminId));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid admin ID format: " + adminId);
        }
    }

    public StockOperation toDomain(StockOperationEntity entity) {
        if (entity == null) return null;

        String adminId = entity.getAdmin() != null ? entity.getAdmin().getId().toString() : null;
        OperationType type = OperationType.valueOf(entity.getType());

        return switch (type) {
            case ARRIVAL -> StockOperation.createArrival(
                    adminId,
                    entity.getItemId(),
                    ItemType.valueOf(entity.getItemType()),
                    entity.getToSection(),
                    entity.getToLocation(),
                    entity.getQuantity()
            );
            case REMOVAL -> StockOperation.createRemoval(
                    adminId,
                    entity.getItemId(),
                    ItemType.valueOf(entity.getItemType()),
                    entity.getFromSection(),
                    entity.getFromLocation(),
                    entity.getQuantity(),
                    entity.getReason()
            );
            case MOVE -> StockOperation.createMove(
                    adminId,
                    entity.getItemId(),
                    ItemType.valueOf(entity.getItemType()),
                    entity.getFromSection(),
                    entity.getFromLocation(),
                    entity.getToSection(),
                    entity.getToLocation(),
                    entity.getReason()
            );
            case WRITE_OFF -> StockOperation.createWriteOff(
                    adminId,
                    entity.getItemId(),
                    ItemType.valueOf(entity.getItemType()),
                    entity.getFromSection(),
                    entity.getFromLocation(),
                    entity.getQuantity(),
                    entity.getReason()
            );
            case INVENTORY_START -> StockOperation.createInventoryStart(
                    adminId,
                    entity.getFromSection()
            );
            case INVENTORY_COMPLETE -> StockOperation.createInventoryComplete(
                    adminId,
                    entity.getFromSection(),
                    entity.getReason()
            );
            case DISCREPANCY -> createDiscrepancyFromEntity(entity);
            case UPDATE -> StockOperation.createUpdate(
                    adminId,
                    entity.getItemId(),
                    ItemType.valueOf(entity.getItemType()),
                    entity.getFromSection(),
                    entity.getFromLocation(),
                    entity.getReason()
            );
            case QUANTITY_CHANGE -> StockOperation.createQuantityChange(
                    adminId,
                    entity.getItemId(),
                    ItemType.valueOf(entity.getItemType()),
                    entity.getFromSection(),
                    entity.getFromLocation(),
                    entity.getQuantity(),
                    entity.getReason()
            );
            case SHIFT_START -> StockOperation.createShiftStart(adminId);
            case SHIFT_END -> StockOperation.createShiftEnd(adminId);
            default -> throw new RuntimeException("Unknown operation type: " + type);
        };
    }

    private StockOperation createDiscrepancyFromEntity(StockOperationEntity entity) {
        try {
            java.lang.reflect.Constructor<StockOperation> constructor =
                    StockOperation.class.getDeclaredConstructor(StockOperation.Builder.class);
            constructor.setAccessible(true);

            String adminId = entity.getAdmin() != null ? entity.getAdmin().getId().toString() : null;

            StockOperation.Builder builder = new StockOperation.Builder()
                    .adminId(adminId)
                    .type(OperationType.DISCREPANCY)
                    .itemId(entity.getItemId())
                    .itemType(ItemType.valueOf(entity.getItemType()))
                    .fromSection(entity.getFromSection())
                    .fromLocation(entity.getFromLocation())
                    .quantity(entity.getQuantity())
                    .reason(entity.getReason())
                    .timestamp(toLocalDateTime(entity.getTimestamp()));

            return constructor.newInstance(builder);

        } catch (Exception e) {
            throw new RuntimeException("Failed to restore StockOperation", e);
        }
    }

    protected UUID toUuid(String id) {
        if (id == null) return null;
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    protected Instant toInstant(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.atZone(ZoneId.systemDefault()).toInstant();
    }

    protected LocalDateTime toLocalDateTime(Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
}