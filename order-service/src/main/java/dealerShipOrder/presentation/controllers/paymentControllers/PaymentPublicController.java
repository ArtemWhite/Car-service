package dealerShipOrder.presentation.controllers.paymentControllers;

import dealerShipOrder.application.services.paymentService.PaymentService;
import dealerShipOrder.presentation.dtos.response.paymentResponsePresentationDto.PaymentListPresentationResponse;
import dealerShipOrder.presentation.dtos.response.paymentResponsePresentationDto.PaymentPresentationResponse;
import dealerShipOrder.presentation.mappers.PaymentPresentationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments (Public)", description = "Public payment endpoints")
public class PaymentPublicController {

    private final PaymentService paymentService;
    private final PaymentPresentationMapper mapper;

    @GetMapping("/{id}")
    @Operation(summary = "Get payment by ID")
    public ResponseEntity<PaymentPresentationResponse> getPayment(@PathVariable String id) {
        var response = paymentService.getPaymentById(id);
        return ResponseEntity.ok(mapper.toPresentation(response));
    }

    @GetMapping
    @Operation(summary = "Get all payments")
    public ResponseEntity<PaymentListPresentationResponse> getAllPayments() {
        var response = paymentService.getAllPayments();
        return ResponseEntity.ok(mapper.toListPresentation(response));
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get payments by order ID")
    public ResponseEntity<PaymentListPresentationResponse> getPaymentsByOrderId(@PathVariable String orderId) {
        var response = paymentService.getPaymentsByOrderId(orderId);
        return ResponseEntity.ok(mapper.toListPresentation(response));
    }
}