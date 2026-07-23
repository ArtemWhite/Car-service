package domain.models.car;

import domain.exception.DomainValidationException;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Currency;

public class Price
{
    @Getter
    private final BigDecimal amount;
    @Getter
    private final Currency currency;
    private final boolean isDiscounted;

    public Price(BigDecimal amount, Currency currency, boolean isDiscounted)
    {
        this.amount = amount;
        this.currency = currency;
        this.isDiscounted = isDiscounted;
    }

    public static Price of(double amount, String currencyCode)
    {
        if (amount < 0)
            throw new DomainValidationException("Negative amount cant be");
        return new Price(BigDecimal.valueOf(amount), Currency.getInstance(currencyCode), false);
    }

    public Price applyDiscount(double percent)
    {
        BigDecimal newAmount = amount.multiply(BigDecimal.valueOf(1 - percent/100));
        return new Price(newAmount, currency, true);
    }

    public Price add(Price other)
    {
        if (!this.currency.equals(other.currency)) {
            throw new DomainValidationException("Currency mismatch");
        }
        return new Price(this.amount.add(other.amount), this.currency, this.isDiscounted);
    }

    public boolean isDiscounted()
    {
        return isDiscounted;
    }

}
