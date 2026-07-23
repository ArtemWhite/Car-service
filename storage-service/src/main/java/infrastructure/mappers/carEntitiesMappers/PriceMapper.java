package infrastructure.mappers.carEntitiesMappers;

import domain.models.car.Price;
import org.mapstruct.Mapper;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public abstract class PriceMapper {

    public BigDecimal toBigDecimal(Price price) {
        return price == null ? null : BigDecimal.valueOf(price.getAmount().doubleValue());
    }

    public Price toDomain(BigDecimal amount) {
        return amount == null ? null : Price.of(amount.doubleValue(), "RUB");
    }
}