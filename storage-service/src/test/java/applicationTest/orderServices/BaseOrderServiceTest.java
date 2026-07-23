package applicationTest.orderServices;

import application.services.orderService.BaseOrderService;
import domain.exception.EntityNotFoundException;
import domain.models.order.Order;
import domain.models.users.User;
import domain.repository.orderRepository.OrderRepository;
import domain.repository.userRepository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BaseOrderService Tests")
class BaseOrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    private TestBaseOrderService baseOrderService;

    @BeforeEach
    void setUp() {
        baseOrderService = new TestBaseOrderService(orderRepository, userRepository);
    }

    private static class TestBaseOrderService extends BaseOrderService {
        public TestBaseOrderService(OrderRepository orderRepository, UserRepository userRepository) {
            super(orderRepository, userRepository);
        }

        public Order testFindOrderById(String id) {
            return findOrderById(id);
        }

        public User testFindUserById(String id) {
            return findUserById(id);
        }

        public Order testSaveOrder(Order order) {
            return saveOrder(order);
        }
    }

    @Test
    @DisplayName("Should find order by id successfully")
    void shouldFindOrderByIdSuccessfully() {
        Order order = mock(Order.class);
        when(orderRepository.findById("order123")).thenReturn(Optional.of(order));

        Order result = baseOrderService.testFindOrderById("order123");

        assertNotNull(result);
        assertEquals(order, result);
    }

    @Test
    @DisplayName("Should throw exception when order not found")
    void shouldThrowExceptionWhenOrderNotFound() {
        when(orderRepository.findById("order66")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            baseOrderService.testFindOrderById("order66");
        });
    }

    @Test
    @DisplayName("Should find user by id successfully")
    void shouldFindUserByIdSuccessfully() {
        User user = mock(User.class);
        when(userRepository.findById("user123")).thenReturn(Optional.of(user));

        User result = baseOrderService.testFindUserById("user123");

        assertNotNull(result);
        assertEquals(user, result);
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findById("user999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            baseOrderService.testFindUserById("user999");
        });
    }

    @Test
    @DisplayName("Should save order successfully")
    void shouldSaveOrderSuccessfully() {
        Order order = mock(Order.class);
        when(orderRepository.save(order)).thenReturn(order);

        Order result = baseOrderService.testSaveOrder(order);

        assertNotNull(result);
        verify(orderRepository, times(1)).save(order);
    }
}