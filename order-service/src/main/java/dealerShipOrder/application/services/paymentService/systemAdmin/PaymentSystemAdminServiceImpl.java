package dealerShipOrder.application.services.paymentService.systemAdmin;

import dealerShipOrder.application.dtos.response.paymentResponse.PaymentResponse;
import dealerShipOrder.application.mapper.PaymentMapper;
import dealerShipOrder.application.services.paymentService.BasePaymentService;
import dealerShipOrder.domain.repository.paymentRepository.paymentRepository.PaymentRepository;
import dealerShipOrder.domain.repository.usersRepository.userRepository.UserRepository;
import domain.exception.DomainValidationException;
import dealerShipOrder.domain.models.payment.Payment;
import dealerShipOrder.domain.models.payment.PaymentStatus;
import dealerShipOrder.domain.models.users.User;
import dealerShipOrder.domain.models.users.systemAdmin.SystemAdmin;
import dealerShipOrder.domain.repository.orderRepository.OrderRepository;
import infrastructure.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional
public class PaymentSystemAdminServiceImpl extends BasePaymentService implements PaymentSystemAdminService {

    public PaymentSystemAdminServiceImpl(
            PaymentRepository paymentRepository,
            UserRepository userRepository,
            OrderRepository orderRepository,
            PaymentMapper paymentMapper) {
        super(paymentRepository, userRepository, orderRepository, paymentMapper);
    }

    @Override
    public PaymentResponse refundPayment(String paymentId, String reason) {
        String adminId = SecurityUtils.getCurrentUserId();
        User user = findUserById(adminId);
        if (!(user instanceof SystemAdmin admin)) {
            throw new DomainValidationException("User is not a system admin");
        }

        Payment payment = findPaymentById(paymentId);

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new DomainValidationException("Only completed payments can be refunded");
        }

        payment.refund();
        Payment updated = savePayment(payment);

        admin.logAction("REFUND_PAYMENT", "Refunded payment: " + paymentId + ". Reason: " + reason);
        userRepository.save(admin);

        return paymentMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByStatus(String status) {
        PaymentStatus paymentStatus;
        try {
            paymentStatus = PaymentStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new DomainValidationException("Invalid payment status: " + status);
        }

        return paymentMapper.toResponseList(
                paymentRepository.findByStatus(paymentStatus)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByDateRange(String from, String to) {
        LocalDateTime fromDate;
        LocalDateTime toDate;

        try {
            fromDate = LocalDateTime.parse(from, DateTimeFormatter.ISO_DATE_TIME);
            toDate = LocalDateTime.parse(to, DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception e) {
            throw new DomainValidationException("Invalid date format. Use ISO format (yyyy-MM-ddTHH:mm:ss)");
        }

        if (fromDate.isAfter(toDate)) {
            throw new DomainValidationException("Start date cannot be after end date");
        }

        return paymentMapper.toResponseList(
                paymentRepository.findByCreatedAtBetween(fromDate, toDate)
        );
    }
}