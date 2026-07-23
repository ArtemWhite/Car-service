package domainTest.order.orderType;

import domain.models.order.OrderType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

class OrderTypeTest {

    @Test
    @DisplayName("Should have both order types")
    void shouldHaveBothTypes() {
        OrderType[] types = OrderType.values();

        assertEquals(2, types.length);
        assertEquals(OrderType.IN_STOCK, types[0]);
        assertEquals(OrderType.CUSTOM, types[1]);
    }

    @Test
    @DisplayName("Should have correct display names")
    void shouldHaveCorrectDisplayNames() {
        assertEquals("Заказ на автомобиль в наличии", OrderType.IN_STOCK.getDisplayName());
        assertEquals("Заказ на автомобиль с конфигурацией", OrderType.CUSTOM.getDisplayName());
    }
}