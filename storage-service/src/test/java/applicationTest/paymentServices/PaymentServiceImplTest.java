package applicationTest.paymentServices;

import application.dtos.response.paymentResponse.PaymentResponse;
import application.mapper.PaymentMapper;
import application.services.paymentService.PaymentServiceImpl;
import domain.exception.EntityNotFoundException;
import domain.models.payment.Payment;
import domain.models.payment.PaymentMethod;
import domain.repository.paymentRepository.PaymentRepository;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService Tests")
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Payment payment;
    private PaymentResponse paymentResponse;

    @BeforeEach
    void setUp() {
        payment = new Payment("pay123", "order123", "client123", 3500000, PaymentMethod.CARD);
        paymentResponse = new PaymentResponse();
    }

    @Test
    @DisplayName("Should get payment by id successfully")
    void shouldGetPaymentByIdSuccessfully() {
        when(paymentRepository.findById("pay123")).thenReturn(Optional.of(payment));
        when(paymentMapper.toResponse(payment)).thenReturn(paymentResponse);

        PaymentResponse result = paymentService.getPaymentById("pay123");

        assertNotNull(result);
        verify(paymentRepository, times(1)).findById("pay123");
        verify(paymentMapper, times(1)).toResponse(payment);
    }

    @Test
    @DisplayName("Should throw exception when payment not found")
    void shouldThrowExceptionWhenPaymentNotFound() {
        when(paymentRepository.findById("pay999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            paymentService.getPaymentById("pay999");
        });
        verify(paymentRepository, times(1)).findById("pay999");
    }

    @Test
    @DisplayName("Should get all payments successfully")
    void shouldGetAllPaymentsSuccessfully() {
        when(paymentRepository.findAll()).thenReturn(List.of(payment));
        when(paymentMapper.toResponseList(anyList())).thenReturn(List.of(paymentResponse));

        List<PaymentResponse> result = paymentService.getAllPayments();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(paymentRepository, times(1)).findAll();
        verify(paymentMapper, times(1)).toResponseList(anyList());
    }

    @Test
    @DisplayName("Should return empty list when no payments")
    void shouldReturnEmptyListWhenNoPayments() {
        when(paymentRepository.findAll()).thenReturn(List.of());

        List<PaymentResponse> result = paymentService.getAllPayments();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(paymentRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should get payments by order id successfully")
    void shouldGetPaymentsByOrderIdSuccessfully() {
        when(paymentRepository.findByOrderId("order123")).thenReturn(List.of(payment));
        when(paymentMapper.toResponseList(anyList())).thenReturn(List.of(paymentResponse));

        List<PaymentResponse> result = paymentService.getPaymentsByOrderId("order123");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(paymentRepository, times(1)).findByOrderId("order123");
        verify(paymentMapper, times(1)).toResponseList(anyList());
    }

    @Test
    @DisplayName("Should return empty list when no payments for order")
    void shouldReturnEmptyListWhenNoPaymentsForOrder() {
        when(paymentRepository.findByOrderId("order999")).thenReturn(List.of());

        List<PaymentResponse> result = paymentService.getPaymentsByOrderId("order999");

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(paymentRepository, times(1)).findByOrderId("order999");
    }
}