package applicationTest.orderServices;

import application.dtos.request.orderRequest.CreateOrderRequest;
import application.dtos.response.orderResponse.OrderHistoryEntryDto;
import application.dtos.response.orderResponse.OrderResponse;
import application.mapper.OrderMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OrderMapper Tests")
class OrderMapperTest {

    private OrderMapper orderMapper;
    private Order inStockOrder;
    private Order customOrder;

    @BeforeEach
    void setUp() {
        orderMapper = new OrderMapper(null);

        inStockOrder = Order.createInStockOrder("order123", "client123", "car123");
        customOrder = Order.createCustomOrder("order456", "client456", "config456", "model456");

        inStockOrder.assignManager("manager123");
    }

    @Test
    @DisplayName("Should convert CreateOrderRequest to IN_STOCK Order")
    void shouldConvertCreateOrderRequestToInStockOrder() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setClientId("client123");
        request.setCarId("car123");
        request.setOrderType("IN_STOCK");

        Order result = orderMapper.toDomain(request);

        assertNotNull(result);
        assertEquals("client123", result.getClientId());
        assertEquals("car123", result.getCarId());
        assertEquals(OrderType.IN_STOCK, result.getType());
        assertEquals(OrderStatus.CREATED, result.getStatus());
    }

    @Test
    @DisplayName("Should convert CreateOrderRequest to CUSTOM Order")
    void shouldConvertCreateOrderRequestToCustomOrder() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setClientId("client456");
        request.setConfigurationId("config456");
        request.setCarModelId("model456");
        request.setOrderType("CUSTOM");

        Order result = orderMapper.toDomain(request);

        assertNotNull(result);
        assertEquals("client456", result.getClientId());
        assertEquals("config456", result.getConfigurationId());
        assertEquals("model456", result.getCarModelId());
        assertEquals(OrderType.CUSTOM, result.getType());
        assertEquals(OrderStatus.CREATED, result.getStatus());
    }

    @Test
    @DisplayName("Should update order status to AWAITING_PAYMENT")
    void shouldUpdateOrderStatusToAwaitingPayment() {
        Order order = Order.createInStockOrder("order1", "client123", "car123");
        order.assignManager("manager123");

        orderMapper.updateOrderStatus(order, "AWAITING_PAYMENT", null);

        assertEquals(OrderStatus.AWAITING_PAYMENT, order.getStatus());
    }

    @Test
    @DisplayName("Should update order status to PAID")
    void shouldUpdateOrderStatusToPaid() {
        Order order = Order.createInStockOrder("order1", "client123", "car123");
        order.assignManager("manager123");
        order.awaitPayment();

        orderMapper.updateOrderStatus(order, "PAID", null);

        assertEquals(OrderStatus.PAID, order.getStatus());
    }

    @Test
    @DisplayName("Should update order status to CANCELLED with reason")
    void shouldUpdateOrderStatusToCancelledWithReason() {
        Order order = Order.createInStockOrder("order1", "client123", "car123");

        orderMapper.updateOrderStatus(order, "CANCELLED", "Customer request");

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    @DisplayName("Should update order status to STOCK_CONFIRMED for custom order")
    void shouldUpdateOrderStatusToStockConfirmed() {
        Order order = Order.createCustomOrder("order1", "client123", "config123", "model123");

        orderMapper.updateOrderStatus(order, "STOCK_CONFIRMED", null);

        assertEquals(OrderStatus.STOCK_CONFIRMED, order.getStatus());
    }

    @Test
    @DisplayName("Should update order status to AWAITING_DELIVERY")
    void shouldUpdateOrderStatusToAwaitingDelivery() {
        Order order = Order.createCustomOrder("order1", "client123", "config123", "model123");
        order.assignManager("manager123");
        order.confirmByStock();
        order.awaitPayment();
        order.markAsPaid();

        orderMapper.updateOrderStatus(order, "AWAITING_DELIVERY", null);

        assertEquals(OrderStatus.AWAITING_DELIVERY, order.getStatus());
    }

    @Test
    @DisplayName("Should update order status to DELIVERED")
    void shouldUpdateOrderStatusToDelivered() {
        Order order = Order.createCustomOrder("order1", "client123", "config123", "model123");
        order.assignManager("manager123");
        order.confirmByStock();
        order.awaitPayment();
        order.markAsPaid();
        order.waitForDelivery();

        orderMapper.updateOrderStatus(order, "DELIVERED", null);

        assertEquals(OrderStatus.READY_FOR_PICKUP, order.getStatus());
    }

    @Test
    @DisplayName("Should update order status to READY_FOR_PICKUP")
    void shouldUpdateOrderStatusToReadyForPickup() {
        Order order = Order.createInStockOrder("order1", "client123", "car123");
        order.assignManager("manager123");
        order.awaitPayment();
        order.markAsPaid();

        orderMapper.updateOrderStatus(order, "READY_FOR_PICKUP", null);

        assertEquals(OrderStatus.READY_FOR_PICKUP, order.getStatus());
    }

    @Test
    @DisplayName("Should convert Order to OrderResponse")
    void shouldConvertOrderToOrderResponse() {
        OrderResponse result = orderMapper.toResponse(inStockOrder);

        assertNotNull(result);
        assertEquals("order123", result.getId());
        assertEquals("client123", result.getClientId());
        assertEquals(OrderType.IN_STOCK.name(), result.getOrderType());
        assertEquals(OrderStatus.MANAGER_APPROVED.name(), result.getStatus());
        assertNotNull(result.getHistory());
    }

    @Test
    @DisplayName("Should convert Order with manager to OrderResponse")
    void shouldConvertOrderWithManagerToOrderResponse() {
        OrderResponse result = orderMapper.toResponse(inStockOrder);

        assertNotNull(result);
        assertEquals("manager123", result.getManagerId());
    }

    @Test
    @DisplayName("Should convert Custom Order to OrderResponse")
    void shouldConvertCustomOrderToOrderResponse() {
        OrderResponse result = orderMapper.toResponse(customOrder);

        assertNotNull(result);
        assertEquals("order456", result.getId());
        assertEquals("client456", result.getClientId());
        assertEquals("config456", result.getConfigurationId());
        assertEquals("model456", result.getCarModelId());
        assertNull(result.getCarId());
    }

    @Test
    @DisplayName("Should convert In-Stock Order to OrderResponse with car info")
    void shouldConvertInStockOrderToOrderResponseWithCarInfo() {
        OrderResponse result = orderMapper.toResponse(inStockOrder);

        assertEquals("car123", result.getCarId());
        assertEquals("Автомобиль в наличии", result.getCarInfo());
    }

    @Test
    @DisplayName("Should convert list of Orders to list of OrderResponses")
    void shouldConvertListOfOrdersToListOfOrderResponses() {
        List<Order> orders = List.of(inStockOrder, customOrder);

        List<OrderResponse> result = orderMapper.toResponseList(orders);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should return empty list for empty input")
    void shouldReturnEmptyListForEmptyInput() {
        List<Order> orders = List.of();

        List<OrderResponse> result = orderMapper.toResponseList(orders);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should convert OrderHistoryEntry to OrderHistoryEntryDto")
    void shouldConvertOrderHistoryEntryToDto() {
        LocalDateTime now = LocalDateTime.now();
        OrderHistoryEntry entry = new OrderHistoryEntry("CREATED", "Order created", now);

        OrderHistoryEntryDto result = orderMapper.toHistoryDto(entry);

        assertNotNull(result);
        assertEquals("CREATED", result.getAction());
        assertEquals("Order created", result.getDescription());
        assertEquals(now, result.getTimestamp());
    }

    @Test
    @DisplayName("Should convert list of OrderHistoryEntry to list of DTOs")
    void shouldConvertHistoryListToDtoList() {
        LocalDateTime now = LocalDateTime.now();
        List<OrderHistoryEntry> history = List.of(
                new OrderHistoryEntry("CREATED", "Order created", now),
                new OrderHistoryEntry("PAID", "Order paid", now)
        );

        List<OrderHistoryEntryDto> result = orderMapper.toHistoryDtoList(history);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("CREATED", result.get(0).getAction());
        assertEquals("PAID", result.get(1).getAction());
    }
}