package domain.models.car.componentModels;

public enum ComponentType
{
    WHEELS("Колёса"),
    TRANSMISSION("Трансмиссия"),
    ENGINE("Двигатель"),
    STEERING_WHEEL("Руль"),
    INTERIOR("Интерьер"),
    SUSPENSION("Подвеска"),
    ELECTRONICS("Электроника");

    private final String displayName;

    ComponentType(String displayName)
    {
        this.displayName = displayName;
    }

    public String getDisplayName()
    {
        return displayName;
    }
}
