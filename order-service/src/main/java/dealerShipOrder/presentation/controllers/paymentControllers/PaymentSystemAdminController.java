package dealerShipOrder.presentation.controllers.paymentControllers;

import dealerShipOrder.application.services.paymentService.systemAdmin.PaymentSystemAdminService;
import dealerShipOrder.presentation.dtos.request.paymentRequestPresentationDto.PaymentRefundPresentationRequest;
import dealerShipOrder.presentation.dtos.response.paymentResponsePresentationDto.PaymentListPresentationResponse;
import dealerShipOrder.presentation.dtos.response.paymentResponsePresentationDto.PaymentPresentationResponse;
import dealerShipOrder.presentation.mappers.PaymentPresentationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
@Tag(name = "Payments (Admin)", description = "Payment management for admins")
public class PaymentSystemAdminController {

    private final PaymentSystemAdminService paymentAdminService;
    private final PaymentPresentationMapper mapper;

    @PostMapping("/{id}/refund")
    @Operation(summary = "Refund a payment")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<PaymentPresentationResponse> refundPayment(
            @PathVariable String id,
            @Valid @RequestBody PaymentRefundPresentationRequest request) {
        var response = paymentAdminService.refundPayment(id, request.getReason());
        return ResponseEntity.ok(mapper.toPresentation(response));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get payments by status")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<PaymentListPresentationResponse> getPaymentsByStatus(@PathVariable String status) {
        var response = paymentAdminService.getPaymentsByStatus(status);
        return ResponseEntity.ok(mapper.toListPresentation(response));
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get payments by date range")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<PaymentListPresentationResponse> getPaymentsByDateRange(
            @RequestParam String from,
            @RequestParam String to) {
        var response = paymentAdminService.getPaymentsByDateRange(from, to);
        return ResponseEntity.ok(mapper.toListPresentation(response));
    }
}