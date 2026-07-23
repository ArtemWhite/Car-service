package dealerShipOrder.applicationTest.orderServices.orderUserTest;

import dealerShipOrder.application.dtos.response.orderResponse.OrderResponse;
import dealerShipOrder.application.mapper.OrderMapper;
import dealerShipOrder.application.services.orderService.manager.OrderManagerServiceImpl;
import dealerShipOrder.domain.models.expection.DomainValidationException;
import dealerShipOrder.domain.models.expection.EntityNotFoundException;
import dealerShipOrder.domain.models.order.Order;
import dealerShipOrder.domain.models.order.OrderStatus;
import dealerShipOrder.domain.models.users.manager.Manager;
import dealerShipOrder.domain.repository.orderRepository.OrderRepository;
import dealerShipOrder.domain.repository.usersRepository.userRepository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import dealerShipOrder.applicationTest.WithMockSecurityExtension;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith({MockitoExtension.class, WithMockSecurityExtension.class})
@DisplayName("OrderManagerService Tests")
class OrderManagerServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderManagerServiceImpl orderManagerService;

    private Manager manager;
    private Order order;
    private OrderResponse orderResponse;

    @BeforeEach
    void setUp() {
        manager = new Manager("John", "Doe", null, "john@email.com", "+123", "pass", "emp123");
        order = Order.createInStockOrder("123","test-user-id", "car123");
        orderResponse = new OrderResponse();
    }

    @Test
    @DisplayName("Should assign manager to order successfully")
    void shouldAssignManagerToOrderSuccessfully() {
        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(manager));
        when(orderRepository.findById("order123")).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(userRepository.save(any(Manager.class))).thenReturn(manager);

        orderManagerService.assignManager("order123");

        assertEquals("test-user-id", order.getManagerId());
        assertEquals(OrderStatus.MANAGER_APPROVED, order.getStatus());
        assertTrue(manager.getAssignedOrders().contains("order123"));
        verify(orderRepository, times(1)).save(order);
        verify(userRepository, times(1)).save(manager);
    }

    @Test
    @DisplayName("Should throw exception when manager not found")
    void shouldThrowExceptionWhenManagerNotFound() {
        // Given
        when(userRepository.findById("manager999")).thenReturn(Optional.empty());

        // Then
        assertThrows(EntityNotFoundException.class, () -> {
            orderManagerService.assignManager("order123");
        });
    }

    @Test
    @DisplayName("Should throw exception when order not found")
    void shouldThrowExceptionWhenOrderNotFound() {
        // Given
        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(manager));
        when(orderRepository.findById("order999")).thenReturn(Optional.empty());

        // Then
        assertThrows(EntityNotFoundException.class, () -> {
            orderManagerService.assignManager("order999");
        });
    }

    @Test
    @DisplayName("Should throw exception when manager already assigned")
    void shouldThrowExceptionWhenManagerAlreadyAssigned() {
        // Given
        order.assignManager("otherManager");
        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(manager));
        when(orderRepository.findById("order123")).thenReturn(Optional.of(order));

        // Then
        assertThrows(DomainValidationException.class, () -> {
            orderManagerService.assignManager("order123");
        });
    }

    @Test
    @DisplayName("Should get manager orders successfully")
    void shouldGetManagerOrdersSuccessfully() {
        when(orderRepository.findByManagerId("test-user-id")).thenReturn(List.of(order));
        when(orderMapper.toResponseList(anyList())).thenReturn(List.of(orderResponse));

        List<OrderResponse> result = orderManagerService.getMyOrders();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderRepository, times(1)).findByManagerId("test-user-id");
        verify(orderMapper, times(1)).toResponseList(anyList());
    }

    @Test
    @DisplayName("Should return empty list when manager has no orders")
    void shouldReturnEmptyListWhenManagerHasNoOrders() {
        when(orderRepository.findByManagerId("test-user-id")).thenReturn(List.of());
        when(orderMapper.toResponseList(anyList())).thenReturn(List.of());

        List<OrderResponse> result = orderManagerService.getMyOrders();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(orderRepository, times(1)).findByManagerId("test-user-id");
        verify(orderMapper, times(1)).toResponseList(anyList());
    }

    @Test
    @DisplayName("Should get pending orders successfully")
    void shouldGetPendingOrdersSuccessfully() {
        when(orderRepository.findByStatus(OrderStatus.CREATED)).thenReturn(List.of(order));
        when(orderMapper.toResponseList(anyList())).thenReturn(List.of(orderResponse));

        List<OrderResponse> result = orderManagerService.getPendingOrders();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderRepository, times(1)).findByStatus(OrderStatus.CREATED);
        verify(orderMapper, times(1)).toResponseList(anyList());
    }

    @Test
    @DisplayName("Should return empty list when no pending orders")
    void shouldReturnEmptyListWhenNoPendingOrders() {
        when(orderRepository.findByStatus(OrderStatus.CREATED)).thenReturn(List.of());

        List<OrderResponse> result = orderManagerService.getPendingOrders();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should confirm in-stock order successfully")
    void shouldConfirmInStockOrderSuccessfully() {
        when(orderRepository.findById("order123")).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        orderManagerService.confirmOrder("order123");

        assertEquals("test-user-id", order.getManagerId());
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    @DisplayName("Should confirm custom order with stock confirmation")
    void shouldConfirmCustomOrderWithStockConfirmation() {
        Order customOrder = Order.createCustomOrder("order123", "test-user-id", "config123", "model123");
        when(orderRepository.findById("order123")).thenReturn(Optional.of(customOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(customOrder);

        orderManagerService.confirmOrder("order123");

        assertEquals("test-user-id", customOrder.getManagerId());
        assertEquals(OrderStatus.STOCK_CONFIRMED, customOrder.getStatus());
        verify(orderRepository, times(1)).save(customOrder);
    }

    @Test
    @DisplayName("Should throw exception when order not found for confirmation")
    void shouldThrowExceptionWhenOrderNotFoundForConfirmation() {
        when(orderRepository.findById("order999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            orderManagerService.confirmOrder("order999");
        });
    }
}