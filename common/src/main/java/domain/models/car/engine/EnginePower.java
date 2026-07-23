package domain.models.car.engine;

import domain.exception.DomainValidationException;

import java.util.Locale;

public class EnginePower
{
    private final double horsePower;

    public EnginePower(double horsePower)
    {
        if (horsePower <= 0)
            throw new DomainValidationException("Invalid engine power: " + horsePower);
        this.horsePower = horsePower;
    }

    public static EnginePower of(double horsePower)
    {
        return new EnginePower(horsePower);
    }

    public double getHorsePower()
    {
        return horsePower;
    }

    @Override
    public String toString() {
        return String.format(Locale.of("ru", "RU"), "%.1f л.с.", horsePower);
    }
}
