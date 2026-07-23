package domain.models.car.types;

public enum CarBrand
{
    BMW("БМВ","Германия"),
    LADA("Лада","Россия"),
    RENAULT("Рено","Италия"),
    AUDI("Ауди","Германия"),
    MERCEDES("Мерседес","Германия"),
    TOYOTA("Тойота", "Япония");

    private final String countryMade;
    private final String displayName;

    CarBrand(String displayName, String countryMade)
    {
        this.countryMade = countryMade;
        this.displayName = displayName;
    }

    public String getCountryMade()
    {
        return countryMade;
    }

    public String getDisplayName()
    {
        return displayName;
    }
}
