package dealerShipOrder.domain.models.users.warehouseAdmin;

import lombok.Getter;

@Getter
public enum ItemType
{
    CAR("Автомобиль"),
    SPARE_PART("Запчасть");

    private final String displayName;

    ItemType(String displayName) {
        this.displayName = displayName;
    }

}
