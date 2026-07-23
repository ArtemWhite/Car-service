package dealerShipOrder.application.mapper;

import dealerShipOrder.application.dtos.request.paymentRequest.CreatePaymentRequest;
import dealerShipOrder.application.dtos.request.paymentRequest.ProcessPaymentRequest;
import dealerShipOrder.application.dtos.response.paymentResponse.PaymentResponse;
import dealerShipOrder.domain.models.payment.Payment;
import dealerShipOrder.domain.models.payment.PaymentMethod;
import dealerShipOrder.domain.models.payment.PaymentStatus;
import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class PaymentMapper {

    public Payment toDomain(CreatePaymentRequest request) {
        return new Payment(
                UUID.randomUUID().toString(),
                request.getOrderId(),
                request.getClientId(),
                request.getAmount(),
                PaymentMethod.valueOf(request.getMethod())
        );
    }

    public PaymentResponse toResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();

        response.setId(payment.getId());
        response.setOrderId(payment.getOrderId());
        response.setClientId(payment.getClientId());
        response.setAmount(payment.getAmount());
        response.setAmountFormatted(formatAmount(payment.getAmount()));
        response.setMethod(payment.getMethod().name());
        response.setMethodDisplayName(getMethodDisplayName(payment.getMethod()));
        response.setStatus(payment.getStatus().name());
        response.setStatusDisplayName(getStatusDisplayName(payment.getStatus()));
        response.setCreatedAt(payment.getCreatedAt());
        response.setProcessedAt(payment.getProcessedAt());
        response.setTransactionId(payment.getTransactionId());
        response.setFailureReason(payment.getFailureReason());

        response.setCompleted(payment.getStatus() == PaymentStatus.COMPLETED);
        response.setFailed(payment.getStatus() == PaymentStatus.FAILED);
        response.setRefunded(payment.getStatus() == PaymentStatus.REFUNDED);
        response.setPending(payment.getStatus() == PaymentStatus.PENDING);
        response.setProcessing(payment.getStatus() == PaymentStatus.PROCESSING);

        return response;
    }

    public List<PaymentResponse> toResponseList(List<Payment> payments) {
        return payments.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public void updateFromProcessRequest(Payment payment, ProcessPaymentRequest request) {
        if (request.isSuccess()) {
            payment.process();
        } else {
            payment.fail(request.getPaymentDetails() != null ?
                    request.getPaymentDetails() : "Payment failed");
        }
    }

    private String formatAmount(double amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("ru", "RU"));
        format.setCurrency(Currency.getInstance("RUB"));
        return format.format(amount);
    }

    private String getMethodDisplayName(PaymentMethod method) {
        return switch (method) {
            case PaymentMethod.CASH -> "Наличные";
            case PaymentMethod.CARD -> "Банковская карта";
            case PaymentMethod.ONLINE -> "Онлайн-оплата";
            case PaymentMethod.INSTALLMENT -> "Рассрочка";
            default -> method.name();
        };
    }

    private String getStatusDisplayName(PaymentStatus status) {
        return switch (status) {
            case PaymentStatus.PENDING -> "Ожидает оплаты";
            case PaymentStatus.PROCESSING -> "Обрабатывается";
            case PaymentStatus.COMPLETED -> "Оплачен";
            case PaymentStatus.FAILED -> "Ошибка оплаты";
            case PaymentStatus.REFUNDED -> "Возврат";
            default -> status.name();
        };
    }
}