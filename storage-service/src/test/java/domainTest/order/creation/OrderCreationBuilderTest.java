package domainTest.order.creation;

import domain.models.order.Order;
import domain.models.order.OrderType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.*;

class OrderCreationBuilderTest {

    @Test
    @DisplayName("Should build in-stock order with builder")
    void shouldBuildInStockOrder() {
        Order order = new Order.Builder()
                .clientId("client123")
                .type(OrderType.IN_STOCK)
                .carId("car456")
                .notes("Test notes")
                .build();

        assertEquals("client123", order.getClientId());
        assertEquals(OrderType.IN_STOCK, order.getType());
        assertEquals("car456", order.getCarId());
        assertEquals("Test notes", order.getNotes());
        assertNull(order.getConfigurationId());
        assertNull(order.getCarModelId());
    }

    @Test
    @DisplayName("Should build custom order with builder")
    void shouldBuildCustomOrder() {
        Order order = new Order.Builder()
                .clientId("client123")
                .type(OrderType.CUSTOM)
                .configurationId("config789")
                .carModelId("model321")
                .notes("Custom order")
                .build();

        assertEquals("client123", order.getClientId());
        assertEquals(OrderType.CUSTOM, order.getType());
        assertEquals("config789", order.getConfigurationId());
        assertEquals("model321", order.getCarModelId());
        assertEquals("Custom order", order.getNotes());
        assertNull(order.getCarId());
    }

    @Test
    @DisplayName("Should set manager ID via builder")
    void shouldSetManagerId() {
        Order order = new Order.Builder()
                .clientId("client123")
                .type(OrderType.IN_STOCK)
                .carId("car456")
                .managerId("manager789")
                .build();

        assertEquals("manager789", order.getManagerId());
    }

    @Test
    @DisplayName("Should throw exception when clientId is null")
    void shouldThrowWhenClientIdNull() {
        assertThrows(NullPointerException.class, () -> {
            new Order.Builder()
                    .type(OrderType.IN_STOCK)
                    .carId("car456")
                    .build();
        });
    }

    @Test
    @DisplayName("Should throw exception when type is null")
    void shouldThrowWhenTypeNull() {
        assertThrows(NullPointerException.class, () -> {
            new Order.Builder()
                    .clientId("client123")
                    .build();
        });
    }

    @Test
    @DisplayName("Should throw exception when in-stock order missing carId")
    void shouldThrowWhenInStockMissingCarId() {
        assertThrows(NullPointerException.class, () -> {
            new Order.Builder()
                    .clientId("client123")
                    .type(OrderType.IN_STOCK)
                    .build();
        });
    }

    @Test
    @DisplayName("Should throw exception when custom order missing configuration")
    void shouldThrowWhenCustomMissingConfiguration() {
        assertThrows(NullPointerException.class, () -> {
            new Order.Builder()
                    .clientId("client123")
                    .type(OrderType.CUSTOM)
                    .carModelId("model321")
                    .build();
        });
    }

    @Test
    @DisplayName("Should throw exception when custom order missing model")
    void shouldThrowWhenCustomMissingModel() {
        assertThrows(NullPointerException.class, () -> {
            new Order.Builder()
                    .clientId("client123")
                    .type(OrderType.CUSTOM)
                    .configurationId("config789")
                    .build();
        });
    }
}