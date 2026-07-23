package domain.models.car.transmission;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Transmission
{
    private String id;
    private final TransmissionType transmissionType;
    private final int gears;

    public Transmission(TransmissionType transmissionType, int gears)
    {
        this.transmissionType = transmissionType;
        this.gears = gears;
    }

    public String getFullName()
    {
        return transmissionType.getDisplayName() + " " + gears + "ст.";
    }

}
