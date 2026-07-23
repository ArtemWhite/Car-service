package dealerShipOrder.domain.models.users.warehouseAdmin;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
public class StockOperation {
    private final String id;
    private final String adminId;
    private final OperationType type;
    private final String itemId;
    private final ItemType itemType;
    private final String fromSection;
    private final String toSection;
    private final String fromLocation;
    private final String toLocation;
    private final int quantity;
    private final String reason;
    private final LocalDateTime timestamp;
    private final String documentNumber;

    private StockOperation(Builder builder) {
        this.id = UUID.randomUUID().toString();
        this.adminId = builder.adminId;
        this.type = builder.type;
        this.itemId = builder.itemId;
        this.itemType = builder.itemType;
        this.fromSection = builder.fromSection;
        this.toSection = builder.toSection;
        this.fromLocation = builder.fromLocation;
        this.toLocation = builder.toLocation;
        this.quantity = builder.quantity;
        this.reason = builder.reason;
        this.timestamp = builder.timestamp;
        this.documentNumber = generateDocumentNumber();
    }

    public static StockOperation createUpdate(String adminId, String itemId, ItemType itemType,
                                              String section, String location, String reason) {
        return new Builder()
                .adminId(adminId)
                .type(OperationType.UPDATE)
                .itemId(itemId)
                .itemType(itemType)
                .fromSection(section)
                .fromLocation(location)
                .reason(reason)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static StockOperation createQuantityChange(String adminId, String itemId, ItemType itemType,
                                                      String section, String location,
                                                      int newQuantity, String reason) {
        return new Builder()
                .adminId(adminId)
                .type(OperationType.QUANTITY_CHANGE)
                .itemId(itemId)
                .itemType(itemType)
                .fromSection(section)
                .fromLocation(location)
                .quantity(newQuantity)
                .reason(reason)
                .build();
    }

    public static StockOperation createShiftStart(String adminId) {
        return new Builder()
                .adminId(adminId)
                .type(OperationType.SHIFT_START)
                .reason("Shift started")
                .build();
    }

    public static StockOperation createShiftEnd(String adminId) {
        return new Builder()
                .adminId(adminId)
                .type(OperationType.SHIFT_END)
                .reason("Shift ended")
                .build();
    }

    private String generateDocumentNumber() {
        String prefix = switch (type) {
            case ARRIVAL -> "IN";
            case REMOVAL -> "OUT";
            case MOVE -> "MOV";
            case INVENTORY_START -> "INV_SR";
            case INVENTORY_COMPLETE -> "INV_COMP";
            case WRITE_OFF -> "WRT";
            case UPDATE -> "UPD";
            case DISCREPANCY -> "DCP";
            case QUANTITY_CHANGE -> "QNT_CHG";
            case SHIFT_START -> "SHT_ST";
            case SHIFT_END -> "SHT_ED";
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
        return prefix + "-" + timestamp.getYear() +
                String.format("%02d", timestamp.getMonthValue()) +
                String.format("%02d", timestamp.getDayOfMonth()) +
                "-" + id.substring(0, 6);
    }

    public static StockOperation createArrival(String adminId, String itemId, ItemType itemType,
                                               String section, String location, int quantity) {
        return new Builder()
                .adminId(adminId)
                .type(OperationType.ARRIVAL)
                .itemId(itemId)
                .itemType(itemType)
                .toSection(section)
                .toLocation(location)
                .quantity(quantity)
                .reason("Поступление на склад")
                .build();
    }

    public static StockOperation createRemoval(String adminId, String itemId, ItemType itemType,
                                               String section, String location, int quantity, String reason) {
        return new Builder()
                .adminId(adminId)
                .type(OperationType.REMOVAL)
                .itemId(itemId)
                .itemType(itemType)
                .fromSection(section)
                .fromLocation(location)
                .quantity(quantity)
                .reason(reason)
                .build();
    }

    public static StockOperation createMove(String adminId, String itemId, ItemType itemType,
                                            String fromSection, String fromLocation,
                                            String toSection, String toLocation, String reason) {
        return new Builder()
                .adminId(adminId)
                .type(OperationType.MOVE)
                .itemId(itemId)
                .itemType(itemType)
                .fromSection(fromSection)
                .fromLocation(fromLocation)
                .toSection(toSection)
                .toLocation(toLocation)
                .quantity(1)
                .reason(reason)
                .build();
    }

    public static StockOperation createWriteOff(String adminId, String itemId, ItemType itemType,
                                                String section, String location, int quantity, String reason) {
        return new Builder()
                .adminId(adminId)
                .type(OperationType.WRITE_OFF)
                .itemId(itemId)
                .itemType(itemType)
                .fromSection(section)
                .fromLocation(location)
                .quantity(quantity)
                .reason("Списание: " + reason)
                .build();
    }

    public static StockOperation createInventoryStart(String adminId, String section) {
        return new Builder()
                .adminId(adminId)
                .type(OperationType.INVENTORY_START)
                .fromSection(section)
                .reason("Начало инвентаризации")
                .build();
    }

    public static StockOperation createInventoryComplete(String adminId, String section, String report) {
        return new Builder()
                .adminId(adminId)
                .type(OperationType.INVENTORY_COMPLETE)
                .fromSection(section)
                .reason("Инвентаризация завершена: " + report)
                .build();
    }

    public static StockOperation createDiscrepancy(String adminId, String itemId, ItemType itemType,
                                                   String section, String location,
                                                   int expected, int actual, String comment) {
        return new Builder()
                .adminId(adminId)
                .type(OperationType.DISCREPANCY)
                .itemId(itemId)
                .itemType(itemType)
                .fromSection(section)
                .fromLocation(location)
                .quantity(actual - expected)
                .reason(String.format("Расхождение: ожидалось %d, фактически %d. %s",
                        expected, actual, comment))
                .build();
    }

    public String getDescription() {
        return switch (type) {
            case ARRIVAL -> String.format("Поступление: %s %d шт. в %s %s",
                    itemType, quantity, toSection, toLocation);
            case REMOVAL -> String.format("Списание: %s %d шт. из %s %s. Причина: %s",
                    itemType, quantity, fromSection, fromLocation, reason);
            case MOVE -> String.format("Перемещение: %s из %s %s в %s %s",
                    itemType, fromSection, fromLocation, toSection, toLocation);
            case WRITE_OFF -> String.format("Списание: %s %d шт. Причина: %s",
                    itemType, quantity, reason);
            case INVENTORY_START -> "Начало инвентаризации в секции " + fromSection;
            case INVENTORY_COMPLETE -> "Завершение инвентаризации: " + reason;
            case DISCREPANCY -> String.format("Расхождение: %s %s, %s",
                    itemType, itemId, reason);
            case UPDATE -> String.format("Обновление информации: %s %s в %s %s. Причина: %s",
                    itemType, itemId, fromSection, fromLocation, reason);
            case QUANTITY_CHANGE -> String.format("Изменение количества: %s %s, новый остаток: %d шт. Причина: %s",
                    itemType, itemId, quantity, reason);
            case SHIFT_START -> String.format("Начало смены (админ: %s)", adminId);
            case SHIFT_END -> String.format("Завершение смены (админ: %s)", adminId);
        };
    }

    public static class Builder {
        private String adminId;
        private OperationType type;
        private String itemId;
        private ItemType itemType;
        private String fromSection;
        private String toSection;
        private String fromLocation;
        private String toLocation;
        private int quantity = 1;
        private String reason = "";
        private LocalDateTime timestamp = LocalDateTime.now();

        public Builder adminId(String adminId) {
            this.adminId = adminId;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        public Builder type(OperationType type) {
            this.type = type;
            return this;
        }

        public Builder itemId(String itemId) {
            this.itemId = itemId;
            return this;
        }

        public Builder itemType(ItemType itemType) {
            this.itemType = itemType;
            return this;
        }

        public Builder fromSection(String fromSection) {
            this.fromSection = fromSection;
            return this;
        }

        public Builder toSection(String toSection) {
            this.toSection = toSection;
            return this;
        }

        public Builder fromLocation(String fromLocation) {
            this.fromLocation = fromLocation;
            return this;
        }

        public Builder toLocation(String toLocation) {
            this.toLocation = toLocation;
            return this;
        }

        public Builder quantity(int quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public StockOperation build() {
            Objects.requireNonNull(adminId, "Admin ID required");
            Objects.requireNonNull(type, "Operation type required");

            if (type != OperationType.INVENTORY_START &&
                    type != OperationType.INVENTORY_COMPLETE &&
                    type != OperationType.SHIFT_START &&
                    type != OperationType.SHIFT_END) {
                Objects.requireNonNull(itemId, "Item ID required for this operation");
                Objects.requireNonNull(itemType, "Item type required for this operation");
            }

            return new StockOperation(this);
        }
    }
}