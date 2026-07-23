package dealerShipOrder.presentation.mappers;


import dealerShipOrder.application.dtos.request.paymentRequest.CreatePaymentRequest;
import dealerShipOrder.application.dtos.request.paymentRequest.ProcessPaymentRequest;
import dealerShipOrder.application.dtos.request.paymentRequest.RefundPaymentRequest;
import dealerShipOrder.application.dtos.response.paymentResponse.PaymentListResponse;
import dealerShipOrder.application.dtos.response.paymentResponse.PaymentResponse;
import dealerShipOrder.application.mapper.PaymentMapper;
import dealerShipOrder.presentation.dtos.request.paymentRequestPresentationDto.PaymentCreatePresentationRequest;
import dealerShipOrder.presentation.dtos.request.paymentRequestPresentationDto.PaymentProcessPresentationRequest;
import dealerShipOrder.presentation.dtos.request.paymentRequestPresentationDto.PaymentRefundPresentationRequest;
import dealerShipOrder.presentation.dtos.response.paymentResponsePresentationDto.PaymentListPresentationResponse;
import dealerShipOrder.presentation.dtos.response.paymentResponsePresentationDto.PaymentPresentationResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PaymentPresentationMapper {

    private final PaymentMapper paymentMapper;

    public PaymentPresentationMapper(PaymentMapper paymentMapper) {
        this.paymentMapper = paymentMapper;
    }
    public CreatePaymentRequest toApplication(PaymentCreatePresentationRequest request, String clientId) {
        if (request == null) return null;

        CreatePaymentRequest target = new CreatePaymentRequest();
        target.setOrderId(request.getOrderId());
        target.setClientId(clientId);
        target.setAmount(request.getAmount());
        target.setMethod(request.getMethod());
        target.setCardNumber(request.getCardNumber());
        target.setCardHolderName(request.getCardHolderName());
        target.setExpiryDate(request.getExpiryDate());
        target.setCvv(request.getCvv());

        return target;
    }

    public CreatePaymentRequest toApplicationWithoutClientId(PaymentCreatePresentationRequest request) {
        if (request == null) return null;

        CreatePaymentRequest target = new CreatePaymentRequest();
        target.setOrderId(request.getOrderId());
        target.setAmount(request.getAmount());
        target.setMethod(request.getMethod());
        target.setCardNumber(request.getCardNumber());
        target.setCardHolderName(request.getCardHolderName());
        target.setExpiryDate(request.getExpiryDate());
        target.setCvv(request.getCvv());

        return target;
    }

    public ProcessPaymentRequest toApplication(PaymentProcessPresentationRequest request) {
        if (request == null) return null;

        ProcessPaymentRequest target = new ProcessPaymentRequest();
        target.setTransactionId(request.getTransactionId());
        target.setPaymentDetails(request.getPaymentDetails());
        target.setSuccess(request.getSuccess());

        return target;
    }

    public RefundPaymentRequest toApplication(PaymentRefundPresentationRequest request) {
        if (request == null) return null;

        RefundPaymentRequest target = new RefundPaymentRequest();
        target.setReason(request.getReason());
        target.setAmount(request.getAmount());

        return target;
    }

    public PaymentPresentationResponse toPresentation(PaymentResponse source) {
        if (source == null) return null;

        return PaymentPresentationResponse.builder()
                .id(source.getId())
                .orderId(source.getOrderId())
                .clientId(source.getClientId())
                .amount(source.getAmount())
                .amountFormatted(source.getAmountFormatted())
                .method(source.getMethod())
                .methodDisplayName(source.getMethodDisplayName())
                .status(source.getStatus())
                .statusDisplayName(source.getStatusDisplayName())
                .createdAt(source.getCreatedAt())
                .processedAt(source.getProcessedAt())
                .transactionId(source.getTransactionId())
                .failureReason(source.getFailureReason())
                .pending(source.isPending())
                .processing(source.isProcessing())
                .completed(source.isCompleted())
                .failed(source.isFailed())
                .refunded(source.isRefunded())
                .cardLastFour(source.getCardLastFour())
                .paymentSystem(source.getPaymentSystem())
                .build();
    }

    public PaymentListPresentationResponse toListPresentation(List<PaymentResponse> source) {
        if (source == null || source.isEmpty()) {
            return createEmptyListResponse();
        }

        long completedCount = source.stream()
                .filter(p -> "COMPLETED".equals(p.getStatus()))
                .count();
        long failedCount = source.stream()
                .filter(p -> "FAILED".equals(p.getStatus()))
                .count();
        long pendingCount = source.stream()
                .filter(p -> "PENDING".equals(p.getStatus()) || "PROCESSING".equals(p.getStatus()))
                .count();
        double totalAmount = source.stream()
                .mapToDouble(PaymentResponse::getAmount)
                .sum();

        return PaymentListPresentationResponse.builder()
                .payments(source.stream()
                        .map(this::toPresentation)
                        .collect(Collectors.toList()))
                .totalCount(source.size())
                .totalAmount(totalAmount)
                .totalAmountFormatted(formatAmount(totalAmount))
                .completedCount((int) completedCount)
                .failedCount((int) failedCount)
                .pendingCount((int) pendingCount)
                .build();
    }

    public PaymentListPresentationResponse toListPresentation(PaymentListResponse source) {
        if (source == null || source.getPayments() == null || source.getPayments().isEmpty()) {
            return createEmptyListResponse();
        }

        PaymentListPresentationResponse response = toListPresentation(source.getPayments());

        return PaymentListPresentationResponse.builder()
                .payments(response.getPayments())
                .totalCount(source.getTotalCount())
                .totalAmount(source.getTotalAmount())
                .totalAmountFormatted(source.getTotalAmountFormatted() != null ?
                        source.getTotalAmountFormatted() : formatAmount(source.getTotalAmount()))
                .completedCount(source.getCompletedCount())
                .failedCount(source.getFailedCount())
                .pendingCount(source.getPendingCount())
                .build();
    }

    private PaymentListPresentationResponse createEmptyListResponse() {
        return PaymentListPresentationResponse.builder()
                .payments(List.of())
                .totalCount(0)
                .totalAmount(0.0)
                .totalAmountFormatted(formatAmount(0.0))
                .completedCount(0)
                .failedCount(0)
                .pendingCount(0)
                .build();
    }

    private String formatAmount(double amount) {
        return String.format("%,.0f ₽", amount).replace(',', ' ');
    }
}