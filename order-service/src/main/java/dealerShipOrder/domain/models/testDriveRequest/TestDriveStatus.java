package dealerShipOrder.domain.models.testDriveRequest;

import lombok.Getter;

@Getter
public enum TestDriveStatus
{
    PENDING("Ожидает подтверждения"),
    CONFIRMED("Подтверждён"),
    COMPLETED("Проведён"),
    CANCELLED("Отменён"),
    NO_SHOW("Клиент не пришёл");

    private final String displayName;

    TestDriveStatus(String displayName) {
        this.displayName = displayName;
    }

}
