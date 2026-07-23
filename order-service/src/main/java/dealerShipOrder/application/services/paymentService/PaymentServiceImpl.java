package dealerShipOrder.application.services.paymentService;

import dealerShipOrder.application.dtos.response.paymentResponse.PaymentResponse;
import dealerShipOrder.application.mapper.PaymentMapper;
import dealerShipOrder.domain.models.payment.Payment;
import dealerShipOrder.domain.repository.orderRepository.OrderRepository;
import dealerShipOrder.domain.repository.paymentRepository.paymentRepository.PaymentRepository;
import dealerShipOrder.domain.repository.usersRepository.userRepository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PaymentServiceImpl extends BasePaymentService implements PaymentService {

    public PaymentServiceImpl(
            PaymentRepository paymentRepository,
            UserRepository userRepository,
            OrderRepository orderRepository,
            PaymentMapper paymentMapper) {
        super(paymentRepository, userRepository, orderRepository, paymentMapper);
    }

    @Override
    public PaymentResponse getPaymentById(String id) {
        Payment payment = findPaymentById(id);
        return paymentMapper.toResponse(payment);
    }

    @Override
    public List<PaymentResponse> getAllPayments() {
        List<Payment> payments = paymentRepository.findAll();
        return paymentMapper.toResponseList(payments);
    }

    @Override
    public List<PaymentResponse> getPaymentsByOrderId(String orderId) {
        List<Payment> payments = paymentRepository.findByOrderId(orderId);
        return paymentMapper.toResponseList(payments);
    }
}