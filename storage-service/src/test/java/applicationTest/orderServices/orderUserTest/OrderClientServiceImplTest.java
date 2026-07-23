package applicationTest.orderServices.orderUserTest;

import application.dtos.request.orderRequest.CreateOrderRequest;
import application.dtos.response.orderResponse.OrderResponse;
import application.mapper.OrderMapper;
import application.services.orderService.client.OrderClientServiceImpl;
import domain.exception.DomainValidationException;
import domain.exception.EntityNotFoundException;
import domain.models.car.Car;
import domain.models.order.Order;
import domain.models.order.OrderStatus;
import domain.models.users.client.Client;
import domain.repository.carRepository.CarRepository;
import domain.repository.orderRepository.OrderRepository;
import domain.repository.userRepository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderClientService Tests")
class OrderClientServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private CarRepository carRepository;

    @Mock
    private Car car;

    @InjectMocks
    private OrderClientServiceImpl orderClientService;

    private Client client;
    private Order order;
    private CreateOrderRequest createRequest;
    private OrderResponse orderResponse;

    @BeforeEach
    void setUp() {
        client = new Client("emp123", "John", "Doe", null, "john@email.com", "+1234567890", "password123");
        order = Order.createInStockOrder("order123", "client123", "car123");
        orderResponse = new OrderResponse();

        createRequest = new CreateOrderRequest();
        createRequest.setClientId("client123");
        createRequest.setCarId("car123");
        createRequest.setOrderType("IN_STOCK");

    }

    @Test
    @DisplayName("Should create order successfully")
    void shouldCreateOrderSuccessfully() {
        when(userRepository.findById("client123")).thenReturn(Optional.of(client));
        when(carRepository.findById("car123")).thenReturn(Optional.of(car));
        when(car.isAvailableForPurchase()).thenReturn(true);
        when(orderMapper.toDomain(createRequest)).thenReturn(order);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);
        when(userRepository.save(any(Client.class))).thenReturn(client);

        OrderResponse result = orderClientService.createOrder(createRequest);

        assertNotNull(result);
        verify(car).reserve();
        verify(carRepository).save(car);
        verify(orderRepository, times(1)).save(order);
        verify(userRepository, times(1)).save(client);
        assertTrue(client.getOrderHistory().contains(order.getId()));
    }

    @Test
    @DisplayName("Should throw exception when client not found")
    void shouldThrowExceptionWhenClientNotFound() {
        when(userRepository.findById("client123")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            orderClientService.createOrder(createRequest);
        });

        verify(carRepository, never()).findById(anyString());
    }

    @Test
    @DisplayName("Should get client orders successfully")
    void shouldGetClientOrdersSuccessfully() {
        when(userRepository.findById("client123")).thenReturn(Optional.of(client));
        when(orderRepository.findByClientId("client123")).thenReturn(List.of(order));
        when(orderMapper.toResponseList(anyList())).thenReturn(List.of(orderResponse));

        List<OrderResponse> result = orderClientService.getMyOrders();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRepository, times(1)).findById("client123");
        verify(orderRepository, times(1)).findByClientId("client123");
        verify(orderMapper, times(1)).toResponseList(anyList());
    }

    @Test
    @DisplayName("Should return empty list when client has no orders")
    void shouldReturnEmptyListWhenClientHasNoOrders() {
        when(userRepository.findById("client123")).thenReturn(Optional.of(client));
        when(orderRepository.findByClientId("client123")).thenReturn(List.of());
        when(orderMapper.toResponseList(anyList())).thenReturn(List.of());

        List<OrderResponse> result = orderClientService.getMyOrders();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userRepository, times(1)).findById("client123");
        verify(orderRepository, times(1)).findByClientId("client123");
        verify(orderMapper, times(1)).toResponseList(anyList());
    }

    @Test
    @DisplayName("Should throw exception when client not found for orders")
    void shouldThrowExceptionWhenClientNotFoundForOrders() {
        when(userRepository.findById("client999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            orderClientService.getMyOrders();
        });
    }

    @Test
    @DisplayName("Should cancel order successfully")
    void shouldCancelOrderSuccessfully() {
        when(orderRepository.findById("order123")).thenReturn(Optional.of(order));
        when(carRepository.findById("car123")).thenReturn(Optional.of(car));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        orderClientService.cancelOrder("order123", "Changed mind");

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verify(car).markAsAvailable();
        verify(carRepository).save(car);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    @DisplayName("Should throw exception when order does not belong to client")
    void shouldThrowExceptionWhenOrderDoesNotBelongToClient() {
        Order otherOrder = Order.createInStockOrder("order123", "otherClient", "car123");

        when(orderRepository.findById("order123")).thenReturn(Optional.of(otherOrder));

        assertThrows(DomainValidationException.class, () -> {
            orderClientService.cancelOrder("order123", "Reason");
        });

        verify(carRepository, never()).findById(anyString());
    }
}