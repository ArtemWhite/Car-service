package dealerShipOrder.applicationTest.paymentServices.paymentUserTest;

import dealerShipOrder.application.dtos.request.paymentRequest.CreatePaymentRequest;
import dealerShipOrder.application.dtos.request.paymentRequest.ProcessPaymentRequest;
import dealerShipOrder.application.dtos.response.paymentResponse.PaymentResponse;
import dealerShipOrder.application.mapper.PaymentMapper;
import dealerShipOrder.application.services.paymentService.client.PaymentClientServiceImpl;
import dealerShipOrder.domain.models.expection.DomainValidationException;
import dealerShipOrder.domain.models.expection.EntityNotFoundException;
import domain.models.car.Car;
import domain.models.car.CarModel;
import domain.models.car.Price;
import domain.models.car.engine.Engine;
import domain.models.car.engine.EngineDisplacement;
import domain.models.car.engine.EngineFuelType;
import domain.models.car.engine.EnginePower;
import domain.models.car.transmission.Transmission;
import domain.models.car.transmission.TransmissionType;
import domain.models.car.types.CarBody;
import domain.models.car.types.CarBrand;
import domain.models.car.types.CarColor;
import domain.models.car.types.DriveType;
import dealerShipOrder.domain.models.order.Order;
import dealerShipOrder.domain.models.payment.Payment;
import dealerShipOrder.domain.models.payment.PaymentMethod;
import dealerShipOrder.domain.models.payment.PaymentStatus;
import dealerShipOrder.domain.models.users.client.Client;
import domain.repository.carRepository.CarRepository;
import domain.repository.carRepository.ConfigurationRepository;
import dealerShipOrder.domain.repository.orderRepository.OrderRepository;
import dealerShipOrder.domain.repository.paymentRepository.paymentRepository.PaymentRepository;
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
@DisplayName("PaymentClientService Tests")
class PaymentClientServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private CarRepository carRepository;

    @Mock
    private ConfigurationRepository configurationRepository;

    @InjectMocks
    private PaymentClientServiceImpl paymentClientService;

    private Client client;
    private Order order;
    private Payment payment;
    private CreatePaymentRequest createRequest;
    private ProcessPaymentRequest processRequest;
    private PaymentResponse paymentResponse;
    private Car car;

    @BeforeEach
    void setUp() {
        client = new Client("test-user-id", "John", "Doe", null, "john@email.com", "+1234567890", "password123");

        order = Order.createInStockOrder("order123", "test-user-id", "car123");
        String managerId = "test-user-id";
        order.assignManager(managerId);
        order.awaitPayment();

        CarModel carModel = new CarModel("model123", "X5", CarBrand.BMW, "G05");
        Engine engine = new Engine("engine123", EngineFuelType.PETROL, EngineDisplacement.of(2.0), EnginePower.of(249.0));
        Transmission transmission = new Transmission(TransmissionType.AUTOMATIC, 8);
        transmission.setId("transmission123");

        car = new Car(
                "car123",
                CarBrand.BMW,
                carModel,
                CarBody.SEDAN,
                CarColor.BLACK,
                DriveType.FRONT,
                engine,
                transmission,
                Price.of(3500000.0, "RUB")
        );

        payment = new Payment("pay123", "order123", "test-user-id", 3500000, PaymentMethod.CARD);
        paymentResponse = new PaymentResponse();

        createRequest = new CreatePaymentRequest();
        createRequest.setOrderId("order123");
        createRequest.setClientId("test-user-id");
        createRequest.setAmount(3500000.0);
        createRequest.setMethod("CARD");

        processRequest = new ProcessPaymentRequest();
        processRequest.setTransactionId("TXN123");
        processRequest.setSuccess(true);
    }

    @Test
    @DisplayName("Should create payment successfully")
    void shouldCreatePaymentSuccessfully() {
        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(client));
        when(orderRepository.findById("order123")).thenReturn(Optional.of(order));
        when(paymentRepository.existsByOrderId("order123")).thenReturn(false);
        when(carRepository.findById("car123")).thenReturn(Optional.of(car));
        when(paymentMapper.toDomain(createRequest)).thenReturn(payment);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(paymentMapper.toResponse(payment)).thenReturn(paymentResponse);

        PaymentResponse result = paymentClientService.createPayment(createRequest);

        assertNotNull(result);
        verify(paymentRepository, times(1)).save(payment);
        verify(carRepository, times(1)).findById("car123");
    }

    @Test
    @DisplayName("Should create payment with PENDING status")
    void shouldCreatePaymentWithPendingStatus() {
        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(client));
        when(orderRepository.findById("order123")).thenReturn(Optional.of(order));
        when(paymentRepository.existsByOrderId("order123")).thenReturn(false);
        when(carRepository.findById("car123")).thenReturn(Optional.of(car));
        when(paymentMapper.toDomain(createRequest)).thenReturn(payment);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(paymentMapper.toResponse(payment)).thenReturn(paymentResponse);

        paymentClientService.createPayment(createRequest);

        assertEquals(PaymentStatus.PENDING, payment.getStatus());
        verify(carRepository, times(1)).findById("car123");
    }

    @Test
    @DisplayName("Should throw exception when client not found")
    void shouldThrowExceptionWhenClientNotFound() {
        when(userRepository.findById("test-user-id")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            paymentClientService.createPayment(createRequest);
        });
    }

    @Test
    @DisplayName("Should throw exception when order not found")
    void shouldThrowExceptionWhenOrderNotFound() {
        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(client));
        when(orderRepository.findById("order123")).thenReturn(Optional.empty());

        assertThrows(DomainValidationException.class, () -> {
            paymentClientService.createPayment(createRequest);
        });
    }

    @Test
    @DisplayName("Should throw exception when order does not belong to client")
    void shouldThrowExceptionWhenOrderDoesNotBelongToClient() {
        Order otherOrder = Order.createInStockOrder("order456", "otherClient", "car456");
        String managerId = "test-user-id";
        otherOrder.assignManager(managerId);
        otherOrder.awaitPayment();

        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(client));
        when(orderRepository.findById("order123")).thenReturn(Optional.of(otherOrder));

        assertThrows(DomainValidationException.class, () -> {
            paymentClientService.createPayment(createRequest);
        });
    }

    @Test
    @DisplayName("Should throw exception when payment already exists")
    void shouldThrowExceptionWhenPaymentAlreadyExists() {
        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(client));
        when(orderRepository.findById("order123")).thenReturn(Optional.of(order));
        when(paymentRepository.existsByOrderId("order123")).thenReturn(true);

        assertThrows(DomainValidationException.class, () -> {
            paymentClientService.createPayment(createRequest);
        });
    }

    @Test
    @DisplayName("Should throw exception when payment amount does not match order amount")
    void shouldThrowExceptionWhenPaymentAmountMismatch() {
        CreatePaymentRequest invalidRequest = new CreatePaymentRequest();
        invalidRequest.setOrderId("order123");
        invalidRequest.setClientId("test-user-id");
        invalidRequest.setAmount(3000000.0);
        invalidRequest.setMethod("CARD");

        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(client));
        when(orderRepository.findById("order123")).thenReturn(Optional.of(order));
        when(paymentRepository.existsByOrderId("order123")).thenReturn(false);
        when(carRepository.findById("car123")).thenReturn(Optional.of(car));

        assertThrows(DomainValidationException.class, () -> {
            paymentClientService.createPayment(invalidRequest);
        });
    }

    @Test
    @DisplayName("Should process payment successfully")
    void shouldProcessPaymentSuccessfully() {
        when(paymentRepository.findById("pay123")).thenReturn(Optional.of(payment));
        when(orderRepository.findById("order123")).thenReturn(Optional.of(order));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(paymentMapper.toResponse(payment)).thenReturn(paymentResponse);

        doAnswer(invocation -> {
            payment.process();
            return null;
        }).when(paymentMapper).updateFromProcessRequest(any(Payment.class), any(ProcessPaymentRequest.class));

        PaymentResponse result = paymentClientService.processPayment("pay123", processRequest);

        assertNotNull(result);
        assertEquals(PaymentStatus.COMPLETED, payment.getStatus());
        assertNotNull(payment.getTransactionId());
        assertNotNull(payment.getProcessedAt());
        verify(paymentRepository, times(1)).save(payment);
        verify(orderRepository, times(1)).findById("order123");
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    @DisplayName("Should fail payment when success is false")
    void shouldFailPaymentWhenSuccessFalse() {
        ProcessPaymentRequest failRequest = new ProcessPaymentRequest();
        failRequest.setSuccess(false);
        failRequest.setPaymentDetails("Insufficient funds");

        when(paymentRepository.findById("pay123")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(paymentMapper.toResponse(payment)).thenReturn(paymentResponse);

        doAnswer(invocation -> {
            payment.fail("Insufficient funds");
            return null;
        }).when(paymentMapper).updateFromProcessRequest(any(Payment.class), any(ProcessPaymentRequest.class));

        PaymentResponse result = paymentClientService.processPayment("pay123", failRequest);

        assertNotNull(result);
        assertEquals(PaymentStatus.FAILED, payment.getStatus());
        assertEquals("Insufficient funds", payment.getFailureReason());
        verify(orderRepository, never()).findById(anyString());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw exception when processing already processed payment")
    void shouldThrowExceptionWhenProcessingAlreadyProcessedPayment() {
        payment.process();
        when(paymentRepository.findById("pay123")).thenReturn(Optional.of(payment));

        assertThrows(DomainValidationException.class, () -> {
            paymentClientService.processPayment("pay123", processRequest);
        });
    }

    @Test
    @DisplayName("Should throw exception when payment not found")
    void shouldThrowExceptionWhenPaymentNotFoundForProcessing() {
        when(paymentRepository.findById("pay999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            paymentClientService.processPayment("pay999", processRequest);
        });
    }

    @Test
    @DisplayName("Should get payment status successfully")
    void shouldGetPaymentStatusSuccessfully() {
        when(paymentRepository.findById("pay123")).thenReturn(Optional.of(payment));
        when(paymentMapper.toResponse(payment)).thenReturn(paymentResponse);

        PaymentResponse result = paymentClientService.getPaymentStatus("pay123");

        assertNotNull(result);
    }

    @Test
    @DisplayName("Should throw exception when payment not found for status")
    void shouldThrowExceptionWhenPaymentNotFoundForStatus() {
        when(paymentRepository.findById("pay999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            paymentClientService.getPaymentStatus("pay999");
        });
    }

    @Test
    @DisplayName("Should get client payments successfully")
    void shouldGetClientPaymentsSuccessfully() {
        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(client));
        when(paymentRepository.findByClientId("test-user-id")).thenReturn(List.of(payment));
        when(paymentMapper.toResponseList(anyList())).thenReturn(List.of(paymentResponse));

        List<PaymentResponse> result = paymentClientService.getMyPayments();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(paymentMapper, times(1)).toResponseList(anyList());
    }

    @Test
    @DisplayName("Should return empty list when client has no payments")
    void shouldReturnEmptyListWhenClientHasNoPayments() {
        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(client));
        when(paymentRepository.findByClientId("test-user-id")).thenReturn(List.of());

        List<PaymentResponse> result = paymentClientService.getMyPayments();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should throw exception when client not found for payments")
    void shouldThrowExceptionWhenClientNotFoundForPayments() {
        when(userRepository.findById("client999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            paymentClientService.getMyPayments();
        });
    }
}