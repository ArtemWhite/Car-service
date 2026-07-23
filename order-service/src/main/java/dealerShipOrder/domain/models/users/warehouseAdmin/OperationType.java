package dealerShipOrder.domain.models.users.warehouseAdmin;

import lombok.Getter;

@Getter
public enum OperationType
{
    ARRIVAL("Поступление"),
    REMOVAL("Списание"),
    MOVE("Перемещение"),
    WRITE_OFF("Списание (брак/утилизация)"),
    INVENTORY_START("Начало инвентаризации"),
    INVENTORY_COMPLETE("Завершение инвентаризации"),
    DISCREPANCY("Расхождение"),

    UPDATE("Обновлён"),
    QUANTITY_CHANGE("Количество изменилось"),
    SHIFT_START("Смена началась"),
    SHIFT_END("Смена закончилась");

    private final String displayName;

    OperationType(String displayName) {
        this.displayName = displayName;
    }

}