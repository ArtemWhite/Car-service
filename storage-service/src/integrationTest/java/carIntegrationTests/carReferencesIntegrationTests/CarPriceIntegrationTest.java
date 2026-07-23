package carIntegrationTests.carReferencesIntegrationTests;

import carIntegrationTests.BaseIntegrationTest;
import domain.models.car.Price;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
class CarPriceIntegrationTest extends BaseIntegrationTest {

    @Test
    @Transactional
    @Rollback
    void shouldCreatePrice() {
        Price price = Price.of(2500000.0, "RUB");

        assertThat(price.getAmount().doubleValue()).isEqualTo(2500000.0);
        assertThat(price.getCurrency().getCurrencyCode()).isEqualTo("RUB");
        assertThat(price.isDiscounted()).isFalse();
    }

    @Test
    @Transactional
    @Rollback
    void shouldApplyDiscount() {
        Price original = Price.of(2500000.0, "RUB");
        Price discounted = original.applyDiscount(10); // 10% скидка

        assertThat(discounted.getAmount().doubleValue()).isEqualTo(2250000.0);
        assertThat(discounted.isDiscounted()).isTrue();
    }

    @Test
    @Transactional
    @Rollback
    void shouldApplyZeroPercentDiscount() {
        Price original = Price.of(2500000.0, "RUB");
        Price discounted = original.applyDiscount(0);

        assertThat(discounted.getAmount().doubleValue()).isEqualTo(2500000.0);
        assertThat(discounted.isDiscounted()).isTrue(); // Создаётся новый объект
    }

    @Test
    @Transactional
    @Rollback
    void shouldApplyHundredPercentDiscount() {
        Price original = Price.of(2500000.0, "RUB");
        Price discounted = original.applyDiscount(100);

        assertThat(discounted.getAmount().doubleValue()).isEqualTo(0.0);
    }

    @Test
    @Transactional
    @Rollback
    void shouldAddPrices() {
        Price base = Price.of(2500000.0, "RUB");
        Price extra = Price.of(50000.0, "RUB");
        Price total = base.add(extra);

        assertThat(total.getAmount().doubleValue()).isEqualTo(2550000.0);
    }

    @Test
    @Transactional
    @Rollback
    void shouldAddMultiplePrices() {
        Price base = Price.of(2500000.0, "RUB");
        Price extra1 = Price.of(50000.0, "RUB");
        Price extra2 = Price.of(30000.0, "RUB");

        Price total = base.add(extra1).add(extra2);

        assertThat(total.getAmount().doubleValue()).isEqualTo(2580000.0);
    }

    @Test
    @Transactional
    @Rollback
    void shouldFailAddDifferentCurrencies() {
        Price rub = Price.of(2500000.0, "RUB");
        Price usd = Price.of(30000.0, "USD");

        assertThatThrownBy(() -> rub.add(usd))
                .isInstanceOf(domain.exception.DomainValidationException.class)
                .hasMessageContaining("Currency mismatch");
    }

    @Test
    @Transactional
    @Rollback
    void shouldFailCreateNegativePrice() {
        assertThatThrownBy(() -> Price.of(-1000.0, "RUB"))
                .isInstanceOf(domain.exception.DomainValidationException.class)
                .hasMessageContaining("Negative amount cant be");
    }

    @Test
    @Transactional
    @Rollback
    void shouldFailCreatePriceWithInvalidCurrency() {
        assertThatThrownBy(() -> Price.of(2500000.0, "INVALID"))
                .isInstanceOf(java.lang.IllegalArgumentException.class);
    }

    @Test
    void shouldFormatPriceCorrectly() {
        Price price = Price.of(2500000.0, "RUB");
        String formatted = String.format("%,.0f %s", price.getAmount(), price.getCurrency().getSymbol());

        String onlyDigits = formatted.replaceAll("\\D+", "");

        assertThat(onlyDigits).isEqualTo("2500000");

        assertThat(formatted).matches(".*[₽рубRUB?].*");
    }
}