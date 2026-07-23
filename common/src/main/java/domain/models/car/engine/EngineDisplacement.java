package domain.models.car.engine;

import domain.exception.DomainValidationException;

import java.util.Locale;

public class EngineDisplacement
{
    private final double liters;

    public EngineDisplacement(double liters)
    {
        if (liters < 0)
            throw new DomainValidationException("Invalid displacement: " + liters);

        this.liters = liters;
    }

    public static EngineDisplacement of(double liters)
    {
        return new EngineDisplacement(liters);
    }

    public double getLiters()
    {
        return liters;
    }

    @Override
    public String toString() {
        return String.format(Locale.of("ru", "RU"), "%.1f л", liters);
    }

}
