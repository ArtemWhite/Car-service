package dealerShipOrder.domain.models.users.warehouseAdmin;

public enum WarehousePosition
{
    WAREHOUSE_WORKER("Кладовщик"),
    STOREKEEPER("Кладовщик"),
    SENIOR_WAREHOUSE_ADMIN("Старший кладовщик"),
    SENIOR_STOREKEEPER("Старший кладовщик"),
    WAREHOUSE_MANAGER("Заведующий складом");

    private final String displayName;

    WarehousePosition(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }
}
