package dealerShipOrder.domain.models.order;

import lombok.Getter;

@Getter
public enum OrderStatus
{
    CREATED("Оформлен"),
    MANAGER_APPROVED("Согласован менеджером"),
    AWAITING_PAYMENT("Ожидает оплаты"),
    PAID("Оплачен"),
    READY_FOR_PICKUP("Автомобиль готов к выдаче"),
    COMPLETED("Завершён"),
    CANCELLED("Отменён"),

    STOCK_CONFIRMED("Согласован складом"),
    AWAITING_DELIVERY("Ожидает доставки автомобиля");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

}
