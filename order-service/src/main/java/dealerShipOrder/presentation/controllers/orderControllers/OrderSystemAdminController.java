package dealerShipOrder.presentation.controllers.orderControllers;

import dealerShipOrder.application.services.orderService.systemAdmin.OrderSystemAdminService;
import dealerShipOrder.presentation.dtos.request.orderRequestPresentationDto.OrderUpdatePresentationRequest;
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
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@Tag(name = "Orders (Admin)", description = "Order management for admins")
public class OrderSystemAdminController {

    private final OrderSystemAdminService orderAdminService;
    private final OrderPresentationMapper mapper;

    @GetMapping
    @Operation(summary = "Get all orders (admin view)")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<OrderListPresentationResponse> getAllOrders() {
        var response = orderAdminService.getAllOrdersForAdmin();
        return ResponseEntity.ok(mapper.toListPresentation(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update order")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<OrderPresentationResponse> updateOrder(
            @PathVariable String id,
            @Valid @RequestBody OrderUpdatePresentationRequest request) {
        var appRequest = mapper.toApplicationWithoutUserId(request);
        var response = orderAdminService.updateOrder(id, appRequest);
        return ResponseEntity.ok(mapper.toPresentation(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete order")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Void> deleteOrder(@PathVariable String id, @RequestParam String reason) {
        orderAdminService.deleteOrder(id, reason);
        return ResponseEntity.noContent().build();
    }
}