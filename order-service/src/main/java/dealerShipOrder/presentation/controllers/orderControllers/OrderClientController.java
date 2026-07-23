package dealerShipOrder.presentation.controllers.orderControllers;

import dealerShipOrder.application.services.orderService.client.OrderClientService;
import dealerShipOrder.presentation.dtos.request.orderRequestPresentationDto.OrderCreatePresentationRequest;
import dealerShipOrder.presentation.dtos.response.orderResponsePresentationDto.OrderListPresentationResponse;
import dealerShipOrder.presentation.dtos.response.orderResponsePresentationDto.OrderPresentationResponse;
import dealerShipOrder.presentation.mappers.OrderPresentationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/client/orders")
@RequiredArgsConstructor
@Tag(name = "Orders (Client)", description = "Order operations for clients")
public class OrderClientController {

    private final OrderClientService orderClientService;
    private final OrderPresentationMapper mapper;

    @PostMapping
    @Operation(summary = "Create a new order")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<OrderPresentationResponse> createOrder(@Valid @RequestBody OrderCreatePresentationRequest request) {
        var appRequest = mapper.toApplicationWithoutClientId(request);
        var response = orderClientService.createOrder(appRequest);
        return ResponseEntity.status(201).body(mapper.toPresentation(response));
    }

    @GetMapping("/my")
    @Operation(summary = "Get my orders (current client)")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<OrderListPresentationResponse> getMyOrders() {
        var response = orderClientService.getMyOrders();
        return ResponseEntity.ok(mapper.toListPresentation(response));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel my order")
    @PreAuthorize("hasRole('CLIENT') and @orderSecurity.isOwner(#id, authentication)")
    public ResponseEntity<Void> cancelOrder(@PathVariable String id, @RequestParam String reason) {
        orderClientService.cancelOrder(id, reason);
        return ResponseEntity.ok().build();
    }
}