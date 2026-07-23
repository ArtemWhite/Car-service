package dealerShipOrder.applicationTest.paymentServices.paymentUserTest;

import dealerShipOrder.application.dtos.response.paymentResponse.PaymentResponse;
import dealerShipOrder.application.mapper.PaymentMapper;
import dealerShipOrder.application.services.paymentService.systemAdmin.PaymentSystemAdminServiceImpl;
import dealerShipOrder.domain.models.expection.DomainValidationException;
import dealerShipOrder.domain.models.expection.EntityNotFoundException;
import dealerShipOrder.domain.models.payment.Payment;
import dealerShipOrder.domain.models.payment.PaymentMethod;
import dealerShipOrder.domain.models.payment.PaymentStatus;
import dealerShipOrder.domain.models.users.systemAdmin.SystemAdmin;
import dealerShipOrder.domain.models.users.systemAdmin.AdminLevel;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith({MockitoExtension.class, WithMockSecurityExtension.class})
@DisplayName("PaymentSystemAdminService Tests")
class PaymentSystemAdminServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @InjectMocks
    private PaymentSystemAdminServiceImpl paymentAdminService;

    private SystemAdmin admin;
    private Payment payment;
    private PaymentResponse paymentResponse;

    @BeforeEach
    void setUp() {
        admin = new SystemAdmin("Admin", "User", null, "admin@email.com", "+123", "pass", "emp1", AdminLevel.ADMIN);
        payment = new Payment("pay123", "order123", "test-user-id", 3500000, PaymentMethod.CARD);
        paymentResponse = new PaymentResponse();
    }

    @Test
    @DisplayName("Should refund payment successfully")
    void shouldRefundPaymentSuccessfully() {
        payment.process();
        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(admin));
        when(paymentRepository.findById("pay123")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(paymentMapper.toResponse(payment)).thenReturn(paymentResponse);

        PaymentResponse result = paymentAdminService.refundPayment("pay123", "Customer request");

        assertNotNull(result);
        assertEquals(PaymentStatus.REFUNDED, payment.getStatus());
        verify(paymentRepository, times(1)).save(payment);
    }

    @Test
    @DisplayName("Should log action when refunding payment")
    void shouldLogActionWhenRefundingPayment() {
        payment.process();
        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(admin));
        when(paymentRepository.findById("pay123")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(paymentMapper.toResponse(payment)).thenReturn(paymentResponse);

        paymentAdminService.refundPayment("pay123", "Customer request");

        assertFalse(admin.getAuditLog().isEmpty());
        assertEquals("REFUND_PAYMENT", admin.getAuditLog().get(0).getAction());
        assertTrue(admin.getAuditLog().get(0).getDetails().contains("Customer request"));
    }

    @Test
    @DisplayName("Should throw exception when admin not found")
    void shouldThrowExceptionWhenAdminNotFound() {
        when(userRepository.findById("admin999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            paymentAdminService.refundPayment("pay123", "Reason");
        });
    }

    @Test
    @DisplayName("Should throw exception when payment not found")
    void shouldThrowExceptionWhenPaymentNotFound() {
        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(admin));
        when(paymentRepository.findById("pay999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            paymentAdminService.refundPayment("pay999", "Reason");
        });
    }

    @Test
    @DisplayName("Should throw exception when payment not completed")
    void shouldThrowExceptionWhenPaymentNotCompleted() {
        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(admin));
        when(paymentRepository.findById("pay123")).thenReturn(Optional.of(payment));

        assertThrows(DomainValidationException.class, () -> {
            paymentAdminService.refundPayment("pay123", "Reason");
        });
    }

    @Test
    @DisplayName("Should get payments by status")
    void shouldGetPaymentsByStatus() {
        when(paymentRepository.findByStatus(PaymentStatus.PENDING)).thenReturn(List.of(payment));
        when(paymentMapper.toResponseList(anyList())).thenReturn(List.of(paymentResponse));

        List<PaymentResponse> result = paymentAdminService.getPaymentsByStatus("PENDING");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(paymentMapper, times(1)).toResponseList(anyList());
    }

    @Test
    @DisplayName("Should return empty list when no payments with status")
    void shouldReturnEmptyListWhenNoPaymentsWithStatus() {
        when(paymentRepository.findByStatus(PaymentStatus.PENDING)).thenReturn(List.of());

        List<PaymentResponse> result = paymentAdminService.getPaymentsByStatus("PENDING");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should get payments by different statuses")
    void shouldGetPaymentsByDifferentStatuses() {
        when(paymentRepository.findByStatus(PaymentStatus.COMPLETED)).thenReturn(List.of(payment));
        when(paymentRepository.findByStatus(PaymentStatus.FAILED)).thenReturn(List.of());
        when(paymentRepository.findByStatus(PaymentStatus.REFUNDED)).thenReturn(List.of());

        when(paymentMapper.toResponseList(List.of(payment))).thenReturn(List.of(paymentResponse));
        when(paymentMapper.toResponseList(List.of())).thenReturn(List.of());

        List<PaymentResponse> completed = paymentAdminService.getPaymentsByStatus("COMPLETED");
        List<PaymentResponse> failed = paymentAdminService.getPaymentsByStatus("FAILED");
        List<PaymentResponse> refunded = paymentAdminService.getPaymentsByStatus("REFUNDED");

        assertEquals(1, completed.size());
        assertTrue(failed.isEmpty());
        assertTrue(refunded.isEmpty());
    }

    @Test
    @DisplayName("Should throw exception on invalid status")
    void shouldThrowExceptionOnInvalidStatus() {
        assertThrows(DomainValidationException.class, () -> {
            paymentAdminService.getPaymentsByStatus("INVALID_STATUS");
        });
    }

    @Test
    @DisplayName("Should get payments by date range")
    void shouldGetPaymentsByDateRange() {
        LocalDateTime from = LocalDateTime.now().minusDays(7);
        LocalDateTime to = LocalDateTime.now();

        when(paymentRepository.findByCreatedAtBetween(from, to)).thenReturn(List.of(payment));
        when(paymentMapper.toResponseList(anyList())).thenReturn(List.of(paymentResponse));

        List<PaymentResponse> result = paymentAdminService.getPaymentsByDateRange(
                from.toString(), to.toString()
        );

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(paymentMapper, times(1)).toResponseList(anyList());
    }

    @Test
    @DisplayName("Should return empty list when no payments in date range")
    void shouldReturnEmptyListWhenNoPaymentsInDateRange() {
        LocalDateTime from = LocalDateTime.now().minusDays(7);
        LocalDateTime to = LocalDateTime.now();

        when(paymentRepository.findByCreatedAtBetween(from, to)).thenReturn(List.of());

        List<PaymentResponse> result = paymentAdminService.getPaymentsByDateRange(
                from.toString(), to.toString()
        );

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should handle same from and to dates")
    void shouldHandleSameFromAndToDates() {
        LocalDateTime date = LocalDateTime.now();

        when(paymentRepository.findByCreatedAtBetween(date, date)).thenReturn(List.of());

        List<PaymentResponse> result = paymentAdminService.getPaymentsByDateRange(
                date.toString(), date.toString()
        );

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}