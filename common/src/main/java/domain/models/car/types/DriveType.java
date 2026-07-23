package domain.models.car.types;

public enum DriveType
{
    FRONT("Передний","FWD"),
    REAR("Задний","RWD"),
    FULL("Полный", "4WD/AWD"),
    PART_TIME("Подключаемый", "4WD");

    private final String displayName;
    private final String codeName;

    DriveType(String displayName, String codeName)
    {
        this.displayName = displayName;
        this.codeName = codeName;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public String getCodeName()
    {
        return codeName;
    }
}
