package domain.models.car.types;

public enum CarStatus
{
    AVAILABLE("Доступен для продажи"),
    RESERVED("Зарезервирован"),
    SOLD("Продан"),
    IN_STOCK("На складе"),
    ON_TEST_DRIVE("На тест-драйве"),
    TEST_DRIVE_AVAILABLE("Доступен для тест-драйва"),
    IN_SERVICE("В сервисе"),
    BOOKED("Забронирован"),
    UNAVAILABLE("Недоступен");

    private final String displayName;

    CarStatus(String displayName)
    {
        this.displayName = displayName;
    }

    public String getDisplayName()
    {
        return displayName;
    }
}
