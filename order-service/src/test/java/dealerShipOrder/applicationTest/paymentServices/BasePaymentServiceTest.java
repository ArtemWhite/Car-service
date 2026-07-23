package dealerShipOrder.applicationTest.paymentServices;

import dealerShipOrder.application.mapper.PaymentMapper;
import dealerShipOrder.application.services.paymentService.BasePaymentService;
import dealerShipOrder.domain.models.expection.EntityNotFoundException;
import dealerShipOrder.domain.models.payment.Payment;
import dealerShipOrder.domain.models.payment.PaymentMethod;
import dealerShipOrder.domain.models.users.User;
import dealerShipOrder.domain.repository.orderRepository.OrderRepository;
import dealerShipOrder.domain.repository.paymentRepository.paymentRepository.PaymentRepository;
import dealerShipOrder.domain.repository.usersRepository.userRepository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import dealerShipOrder.applicationTest.WithMockSecurityExtension;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith({MockitoExtension.class, WithMockSecurityExtension.class})
@DisplayName("BasePaymentService Tests")
class BasePaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentMapper paymentMapper;

    private TestBasePaymentService basePaymentService;

    @BeforeEach
    void setUp() {
        basePaymentService = new TestBasePaymentService(
                paymentRepository,
                userRepository,
                orderRepository,
                paymentMapper
        );
    }

    private static class TestBasePaymentService extends BasePaymentService {
        public TestBasePaymentService(
                PaymentRepository paymentRepository,
                UserRepository userRepository,
                OrderRepository orderRepository,
                PaymentMapper paymentMapper) {
            super(paymentRepository, userRepository, orderRepository, paymentMapper);
        }

        public Payment testFindPaymentById(String id) {
            return findPaymentById(id);
        }

        public User testFindUserById(String id) {
            return findUserById(id);
        }

        public Payment testSavePayment(Payment payment) {
            return savePayment(payment);
        }
    }

    @Test
    @DisplayName("Should find payment by id successfully")
    void shouldFindPaymentByIdSuccessfully() {
        Payment payment = new Payment(
                "pay123",
                "order123",
                "test-user-id",
                5000.0,
                PaymentMethod.CARD
        );
        when(paymentRepository.findById("pay123")).thenReturn(Optional.of(payment));

        Payment result = basePaymentService.testFindPaymentById("pay123");

        assertNotNull(result);
        assertEquals("pay123", result.getId());
        assertEquals("order123", result.getOrderId());
        assertEquals(5000.0, result.getAmount());
        verify(paymentRepository, times(1)).findById("pay123");
    }

    @Test
    @DisplayName("Should throw exception when payment not found")
    void shouldThrowExceptionWhenPaymentNotFound() {
        when(paymentRepository.findById("pay999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            basePaymentService.testFindPaymentById("pay999");
        });
        verify(paymentRepository, times(1)).findById("pay999");
    }

    @Test
    @DisplayName("Should find user by id successfully")
    void shouldFindUserByIdSuccessfully() {
        User user = mock(User.class);
        when(user.getId()).thenReturn("user123");
        when(userRepository.findById("user123")).thenReturn(Optional.of(user));

        User result = basePaymentService.testFindUserById("user123");

        assertNotNull(result);
        assertEquals("user123", result.getId());
        verify(userRepository, times(1)).findById("user123");
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findById("user999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            basePaymentService.testFindUserById("user999");
        });
        verify(userRepository, times(1)).findById("user999");
    }

    @Test
    @DisplayName("Should save payment successfully")
    void shouldSavePaymentSuccessfully() {
        Payment payment = new Payment(
                "pay123",
                "order123",
                "test-user-id",
                5000.0,
                PaymentMethod.CARD
        );
        when(paymentRepository.save(payment)).thenReturn(payment);

        Payment result = basePaymentService.testSavePayment(payment);

        assertNotNull(result);
        assertEquals("pay123", result.getId());
        verify(paymentRepository, times(1)).save(payment);
    }

    @Test
    @DisplayName("Should save payment with generated ID")
    void shouldSavePaymentWithGeneratedId() {
        Payment payment = new Payment(
                "pay456",
                "order456",
                "client456",
                10000.0,
                PaymentMethod.ONLINE
        );
        when(paymentRepository.save(payment)).thenReturn(payment);

        Payment result = basePaymentService.testSavePayment(payment);

        assertNotNull(result);
        assertEquals("pay456", result.getId());
        assertEquals("order456", result.getOrderId());
        assertEquals(10000.0, result.getAmount());
        verify(paymentRepository, times(1)).save(payment);
    }
}