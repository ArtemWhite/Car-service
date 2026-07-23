package domainTest.order.creation;

import domain.models.order.Order;
import domain.models.order.OrderStatus;
import domain.models.order.OrderType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.*;

public class OrderCreationTest
{
    @Test
    @DisplayName("Should create in-stock order using factory method")
    void shouldCreateInStockOrder() {
        String orderId = "order212";
        String clientId = "client123";
        String carId = "car456";

        Order order = Order.createInStockOrder(orderId,clientId, carId);

        assertNotNull(order);
        assertNotNull(order.getId());
        assertEquals(clientId, order.getClientId());
        assertEquals(carId, order.getCarId());
        assertEquals(OrderType.IN_STOCK, order.getType());
        assertEquals(OrderStatus.CREATED, order.getStatus());
        assertNull(order.getManagerId());
        assertNull(order.getConfigurationId());
        assertNull(order.getCarModelId());
        assertEquals(1, order.getHistory().size());
    }

    @Test
    @DisplayName("Should create custom order using factory method")
    void shouldCreateCustomOrder() {
        String orderId = "order3452";
        String clientId = "client123";
        String configId = "config789";
        String modelId = "model321";

        Order order = Order.createCustomOrder(orderId, clientId, configId, modelId);

        assertNotNull(order);
        assertNotNull(order.getId());
        assertEquals(clientId, order.getClientId());
        assertEquals(configId, order.getConfigurationId());
        assertEquals(modelId, order.getCarModelId());
        assertEquals(OrderType.CUSTOM, order.getType());
        assertEquals(OrderStatus.CREATED, order.getStatus());
        assertNull(order.getCarId());
        assertNull(order.getManagerId());
        assertEquals(1, order.getHistory().size());
    }
}

