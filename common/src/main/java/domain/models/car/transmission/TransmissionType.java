package domain.models.car.transmission;

public enum TransmissionType
{
    MANUAL("МКПП"),
    AUTOMATIC("АКПП");

    private final String displayName;

    TransmissionType(String displayName)
    {
        this.displayName = displayName;
    }

    public String getDisplayName()
    {
        return displayName;
    }
}
