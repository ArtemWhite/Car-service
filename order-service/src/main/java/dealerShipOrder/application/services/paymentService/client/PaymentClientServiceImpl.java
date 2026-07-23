package dealerShipOrder.application.services.paymentService.client;

import dealerShipOrder.application.dtos.request.paymentRequest.CreatePaymentRequest;
import dealerShipOrder.application.dtos.request.paymentRequest.ProcessPaymentRequest;
import dealerShipOrder.application.dtos.response.paymentResponse.PaymentResponse;
import dealerShipOrder.application.mapper.PaymentMapper;
import dealerShipOrder.application.services.paymentService.BasePaymentService;
import dealerShipOrder.domain.repository.paymentRepository.paymentRepository.PaymentRepository;
import dealerShipOrder.domain.repository.usersRepository.userRepository.UserRepository;
import dealerShipOrder.domain.models.expection.DomainValidationException;
import dealerShipOrder.domain.models.expection.EntityNotFoundException;
import domain.models.car.Car;
import domain.models.car.CarConfiguration;
import dealerShipOrder.domain.models.order.Order;
import dealerShipOrder.domain.models.order.OrderStatus;
import dealerShipOrder.domain.models.payment.Payment;
import dealerShipOrder.domain.models.payment.PaymentStatus;
import dealerShipOrder.domain.models.users.User;
import dealerShipOrder.domain.models.users.client.Client;
import domain.repository.carRepository.CarRepository;
import domain.repository.carRepository.ConfigurationRepository;
import dealerShipOrder.domain.repository.orderRepository.OrderRepository;
import dealerShipOrder.infrastructure.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PaymentClientServiceImpl extends BasePaymentService implements PaymentClientService {

    private final CarRepository carRepository;
    private final ConfigurationRepository configurationRepository;

    public PaymentClientServiceImpl(
            PaymentRepository paymentRepository,
            UserRepository userRepository,
            OrderRepository orderRepository,
            PaymentMapper paymentMapper,
            CarRepository carRepository,
            ConfigurationRepository configurationRepository) {
        super(paymentRepository, userRepository, orderRepository, paymentMapper);
        this.carRepository = carRepository;
        this.configurationRepository = configurationRepository;
    }

    @Override
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        String clientId = SecurityUtils.getCurrentUserId();
        request.setClientId(clientId);

        User user = findUserById(clientId);
        if (!(user instanceof Client client)) {
            throw new DomainValidationException("User is not a client");
        }

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new DomainValidationException("Order not found"));

        if (!order.getClientId().equals(client.getId())) {
            throw new DomainValidationException("Order does not belong to this client");
        }

        if (paymentRepository.existsByOrderId(request.getOrderId())) {
            throw new DomainValidationException("Payment already exists for this order");
        }

        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new DomainValidationException("Cannot create payment for " + order.getStatus().name().toLowerCase() + " order");
        }

        if (order.getStatus() != OrderStatus.AWAITING_PAYMENT) {
            throw new DomainValidationException("Order is not awaiting payment");
        }

        double expectedAmount = getOrderAmount(order);
        if (Math.abs(request.getAmount() - expectedAmount) > 0.01) {
            throw new DomainValidationException("Payment amount does not match order amount. Expected: " + expectedAmount + ", got: " + request.getAmount());
        }

        Payment payment = paymentMapper.toDomain(request);
        Payment saved = savePayment(payment);

        return paymentMapper.toResponse(saved);
    }

    @Override
    public PaymentResponse processPayment(String paymentId, ProcessPaymentRequest request) {
        String clientId = SecurityUtils.getCurrentUserId();
        Payment payment = findPaymentById(paymentId);

        if (!payment.getClientId().equals(clientId)) {
            throw new DomainValidationException("Payment does not belong to this client");
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new DomainValidationException("Payment cannot be processed");
        }

        if (request.getTransactionId() != null && !request.getTransactionId().isEmpty()) {
            payment.setTransactionId(request.getTransactionId());
        }

        paymentMapper.updateFromProcessRequest(payment, request);
        Payment updated = savePayment(payment);

        if (request.isSuccess()) {
            Order order = orderRepository.findById(payment.getOrderId())
                    .orElseThrow(() -> new EntityNotFoundException("Order not found: " + payment.getOrderId()));

            if (order.getStatus() != OrderStatus.AWAITING_PAYMENT) {
                throw new DomainValidationException("Order is not awaiting payment");
            }

            order.markAsPaid();
            orderRepository.save(order);
        }

        return paymentMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentStatus(String paymentId) {
        String clientId = SecurityUtils.getCurrentUserId();
        Payment payment = findPaymentById(paymentId);

        if (!payment.getClientId().equals(clientId)) {
            throw new DomainValidationException("Payment does not belong to this client");
        }

        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getMyPayments() {
        String clientId = SecurityUtils.getCurrentUserId();
        User user = findUserById(clientId);
        if (!(user instanceof Client)) {
            throw new DomainValidationException("User is not a client");
        }

        return paymentMapper.toResponseList(
                paymentRepository.findByClientId(clientId)
        );
    }

    private double getOrderAmount(Order order) {
        if (order.getCarId() != null) {
            Car car = carRepository.findById(order.getCarId())
                    .orElseThrow(() -> new EntityNotFoundException("Car not found: " + order.getCarId()));
            return car.getPrice().getAmount().doubleValue();
        } else if (order.getConfigurationId() != null) {
            CarConfiguration configuration = configurationRepository.findById(order.getConfigurationId())
                    .orElseThrow(() -> new EntityNotFoundException("Configuration not found: " + order.getConfigurationId()));
            return configuration.getBasePrice().getAmount().doubleValue();
        }
        throw new DomainValidationException("Cannot determine order amount: no carId or configurationId");
    }
}