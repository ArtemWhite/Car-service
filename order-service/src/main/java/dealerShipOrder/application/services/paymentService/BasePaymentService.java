package dealerShipOrder.application.services.paymentService;

import dealerShipOrder.application.mapper.PaymentMapper;
import dealerShipOrder.domain.repository.paymentRepository.paymentRepository.PaymentRepository;
import dealerShipOrder.domain.repository.usersRepository.userRepository.UserRepository;
import dealerShipOrder.domain.models.expection.EntityNotFoundException;
import dealerShipOrder.domain.models.payment.Payment;
import dealerShipOrder.domain.models.users.User;
import dealerShipOrder.domain.repository.orderRepository.OrderRepository;

public abstract class BasePaymentService {

    protected final PaymentRepository paymentRepository;
    protected final UserRepository userRepository;
    protected final OrderRepository orderRepository;
    protected final PaymentMapper paymentMapper;

    public BasePaymentService(
            PaymentRepository paymentRepository,
            UserRepository userRepository,
            OrderRepository orderRepository,
            PaymentMapper paymentMapper) {
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.paymentMapper = paymentMapper;
    }

    protected Payment findPaymentById(String paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found: " + paymentId));
    }

    protected User findUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
    }

    protected Payment savePayment(Payment payment) {
        return paymentRepository.save(payment);
    }
}