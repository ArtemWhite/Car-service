package domain.models.car.engine;

import domain.exception.DomainValidationException;
import lombok.Getter;

@Getter
public class Engine
{
    private final String id;
    private final EngineFuelType engineFuelType;
    private final EngineDisplacement engineDisplacement;
    private final EnginePower enginePower;

    public Engine(String id, EngineFuelType engineFuelType, EngineDisplacement engineDisplacement, EnginePower enginePower)
    {
        this.id = id;
        this.engineFuelType = engineFuelType;
        this.engineDisplacement = engineDisplacement;
        this.enginePower = enginePower;

        if (engineFuelType == EngineFuelType.ELECTRIC && engineDisplacement.getLiters() > 0)
            throw new DomainValidationException("Electric engine should have 0 displacement");
    }

    public String getDescription()
    {
        if (engineFuelType == EngineFuelType.ELECTRIC)
        {
            return String.format("%s, мощность: %s", engineFuelType.getDisplayName(), enginePower.toString());
        }

        return String.format("%s, %s, %s", engineFuelType.getDisplayName(), engineDisplacement.toString(), enginePower.toString());
    }
}
