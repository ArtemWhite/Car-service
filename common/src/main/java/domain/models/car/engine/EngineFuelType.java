package domain.models.car.engine;

public enum EngineFuelType
{
    ELECTRIC("Электро"),
    PETROL("Бензин"),
    DIESEL("Дизель"),
    HYBRID("Гибрид");

    private final String displayName;

    EngineFuelType(String displayName)
    {
        this.displayName = displayName;
    }

    public String getDisplayName()
    {
        return displayName;
    }
}
