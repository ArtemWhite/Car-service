package dealerShipOrder.presentation.controllers.orderControllers;

import dealerShipOrder.application.services.orderService.manager.OrderManagerService;
import dealerShipOrder.presentation.dtos.response.orderResponsePresentationDto.OrderListPresentationResponse;
import dealerShipOrder.presentation.mappers.OrderPresentationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/manager/orders")
@RequiredArgsConstructor
@Tag(name = "Orders (Manager)", description = "Order management for managers")
public class OrderManagerController {

    private final OrderManagerService orderManagerService;
    private final OrderPresentationMapper mapper;

    @PostMapping("/{id}/assign")
    @Operation(summary = "Assign order to manager")
    @PreAuthorize("hasAnyRole('MANAGER', 'SYSTEM_ADMIN')")
    public ResponseEntity<Void> assignOrder(@PathVariable String id) {
        orderManagerService.assignManager(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/my")
    @Operation(summary = "Get orders assigned to me")
    @PreAuthorize("hasAnyRole('MANAGER', 'SYSTEM_ADMIN')")
    public ResponseEntity<OrderListPresentationResponse> getMyOrders() {
        var response = orderManagerService.getMyOrders();
        return ResponseEntity.ok(mapper.toListPresentation(response));
    }

    @GetMapping("/pending")
    @Operation(summary = "Get all pending orders (not assigned)")
    @PreAuthorize("hasAnyRole('MANAGER', 'SYSTEM_ADMIN')")
    public ResponseEntity<OrderListPresentationResponse> getPendingOrders() {
        var response = orderManagerService.getPendingOrders();
        return ResponseEntity.ok(mapper.toListPresentation(response));
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "Confirm order (for custom orders)")
    @PreAuthorize("hasAnyRole('MANAGER', 'SYSTEM_ADMIN')")
    public ResponseEntity<Void> confirmOrder(@PathVariable String id) {
        orderManagerService.confirmOrder(id);
        return ResponseEntity.ok().build();
    }
}