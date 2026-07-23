package dealerShipOrder.domain.models.order;

public enum OrderType {
    IN_STOCK("Заказ на автомобиль в наличии"),
    CUSTOM("Заказ на автомобиль с конфигурацией");

    private final String displayName;

    OrderType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }
}