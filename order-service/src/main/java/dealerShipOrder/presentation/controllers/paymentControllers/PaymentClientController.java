package dealerShipOrder.presentation.controllers.paymentControllers;

import dealerShipOrder.application.services.paymentService.client.PaymentClientService;
import dealerShipOrder.presentation.dtos.request.paymentRequestPresentationDto.PaymentCreatePresentationRequest;
import dealerShipOrder.presentation.dtos.request.paymentRequestPresentationDto.PaymentProcessPresentationRequest;
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
@RequestMapping("/api/client/payments")
@RequiredArgsConstructor
@Tag(name = "Payments (Client)", description = "Payment operations for clients")
public class PaymentClientController {

    private final PaymentClientService paymentClientService;
    private final PaymentPresentationMapper mapper;

    @PostMapping
    @Operation(summary = "Create a new payment")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<PaymentPresentationResponse> createPayment(@Valid @RequestBody PaymentCreatePresentationRequest request) {
        var appRequest = mapper.toApplicationWithoutClientId(request);
        var response = paymentClientService.createPayment(appRequest);
        return ResponseEntity.status(201).body(mapper.toPresentation(response));
    }

    @PostMapping("/{id}/process")
    @Operation(summary = "Process a payment")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<PaymentPresentationResponse> processPayment(
            @PathVariable String id,
            @Valid @RequestBody PaymentProcessPresentationRequest request) {
        var appRequest = mapper.toApplication(request);
        var response = paymentClientService.processPayment(id, appRequest);
        return ResponseEntity.ok(mapper.toPresentation(response));
    }

    @GetMapping("/{id}/status")
    @Operation(summary = "Get payment status")
    @PreAuthorize("hasRole('CLIENT') and @paymentSecurity.isOwner(#id, authentication)")
    public ResponseEntity<PaymentPresentationResponse> getPaymentStatus(@PathVariable String id) {
        var response = paymentClientService.getPaymentStatus(id);
        return ResponseEntity.ok(mapper.toPresentation(response));
    }

    @GetMapping("/my")
    @Operation(summary = "Get my payments (current client)")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<PaymentListPresentationResponse> getMyPayments() {
        var response = paymentClientService.getMyPayments();
        return ResponseEntity.ok(mapper.toListPresentation(response));
    }
}