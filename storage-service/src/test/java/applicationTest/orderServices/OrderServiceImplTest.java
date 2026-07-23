package applicationTest.orderServices;

import application.dtos.request.orderRequest.OrderFilterRequest;
import application.dtos.response.orderResponse.OrderResponse;
import application.mapper.OrderMapper;
import application.services.orderService.OrderServiceImpl;
import domain.exception.EntityNotFoundException;
import domain.models.order.Order;
import domain.repository.orderRepository.OrderRepository;
import domain.repository.userRepository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Tests")
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order order;
    private OrderResponse orderResponse;

    @BeforeEach
    void setUp() {
        order = Order.createInStockOrder("order123", "client123", "car123");
        orderResponse = new OrderResponse();
    }

    @Test
    @DisplayName("Should get order by id successfully")
    void shouldGetOrderByIdSuccessfully() {
        when(orderRepository.findById("order123")).thenReturn(Optional.of(order));
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);

        OrderResponse result = orderService.getOrderById("order123");

        assertNotNull(result);
        verify(orderRepository, times(1)).findById("order123");
        verify(orderMapper, times(1)).toResponse(order);
    }

    @Test
    @DisplayName("Should throw exception when order not found")
    void shouldThrowExceptionWhenOrderNotFound() {
        when(orderRepository.findById("order999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            orderService.getOrderById("order999");
        });
    }

    @Test
    @DisplayName("Should get all orders successfully")
    void shouldGetAllOrdersSuccessfully() {
        when(orderRepository.findAll()).thenReturn(List.of(order));
        when(orderMapper.toResponseList(anyList())).thenReturn(List.of(orderResponse));

        List<OrderResponse> result = orderService.getAllOrders();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderRepository, times(1)).findAll();
        verify(orderMapper, times(1)).toResponseList(anyList());
    }

    @Test
    @DisplayName("Should return empty list when no orders")
    void shouldReturnEmptyListWhenNoOrders() {
        when(orderRepository.findAll()).thenReturn(List.of());

        List<OrderResponse> result = orderService.getAllOrders();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(orderRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should filter orders by status")
    void shouldFilterOrdersByStatus() {
        OrderFilterRequest filter = new OrderFilterRequest();
        filter.setStatus("PAID");

        Order paidOrder = Order.createInStockOrder("order1", "client123", "car123");
        paidOrder.assignManager("manager1");
        paidOrder.awaitPayment();
        paidOrder.markAsPaid();

        Order pendingOrder = Order.createInStockOrder("order2", "client456", "car456");

        when(orderRepository.findAll()).thenReturn(List.of(paidOrder, pendingOrder));
        when(orderMapper.toResponseList(anyList())).thenReturn(List.of(orderResponse));

        List<OrderResponse> result = orderService.getOrdersWithFilters(filter);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderMapper, times(1)).toResponseList(anyList());
    }

    @Test
    @DisplayName("Should filter orders by type")
    void shouldFilterOrdersByType() {
        OrderFilterRequest filter = new OrderFilterRequest();
        filter.setOrderType("CUSTOM");

        Order customOrder = Order.createCustomOrder("order1", "client123", "config123", "model123");
        Order inStockOrder = Order.createInStockOrder("order2", "client456", "car456");

        when(orderRepository.findAll()).thenReturn(List.of(customOrder, inStockOrder));
        when(orderMapper.toResponseList(anyList())).thenReturn(List.of(orderResponse));

        List<OrderResponse> result = orderService.getOrdersWithFilters(filter);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderMapper, times(1)).toResponseList(anyList());
    }

    @Test
    @DisplayName("Should filter orders by date range")
    void shouldFilterOrdersByDateRange() {
        OrderFilterRequest filter = new OrderFilterRequest();
        filter.setDateFrom(LocalDateTime.now().minusDays(1));
        filter.setDateTo(LocalDateTime.now().plusDays(1));

        Order newOrder = Order.createInStockOrder("order1", "client123", "car123");

        when(orderRepository.findAll()).thenReturn(List.of(newOrder));
        when(orderMapper.toResponseList(anyList())).thenReturn(List.of(orderResponse));

        List<OrderResponse> result = orderService.getOrdersWithFilters(filter);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should combine multiple filters")
    void shouldCombineMultipleFilters() {
        OrderFilterRequest filter = new OrderFilterRequest();
        filter.setStatus("CREATED");
        filter.setOrderType("IN_STOCK");
        filter.setClientId("client123");

        Order matchingOrder = Order.createInStockOrder("order1", "client123", "car123");
        Order otherOrder = Order.createInStockOrder("order2", "client456", "car456");

        when(orderRepository.findAll()).thenReturn(List.of(matchingOrder, otherOrder));
        when(orderMapper.toResponseList(anyList())).thenReturn(List.of(orderResponse));

        List<OrderResponse> result = orderService.getOrdersWithFilters(filter);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should return empty list when no orders match filters")
    void shouldReturnEmptyListWhenNoOrdersMatchFilters() {
        OrderFilterRequest filter = new OrderFilterRequest();
        filter.setStatus("COMPLETED");

        Order activeOrder = Order.createInStockOrder("order1", "client123", "car123");

        when(orderRepository.findAll()).thenReturn(List.of(activeOrder));
        when(orderMapper.toResponseList(anyList())).thenReturn(List.of());

        List<OrderResponse> result = orderService.getOrdersWithFilters(filter);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}