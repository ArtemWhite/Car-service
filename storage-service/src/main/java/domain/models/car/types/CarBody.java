package domain.models.car.types;

public enum CarBody
{
    SEDAN("Седан"),
    UNIVERSAL("Универсал"),
    COUPE("Купе"),
    VAN("Фургон"),
    PICKUP("Пикап"),
    CABRIOLET("Кабриолет"),
    HATCHBACK("Хэтчбек");

    private final String displayName;

    CarBody(String displayName)
    {
        this.displayName = displayName;
    }

    public String getDisplayName()
    {
        return displayName;
    }
}
