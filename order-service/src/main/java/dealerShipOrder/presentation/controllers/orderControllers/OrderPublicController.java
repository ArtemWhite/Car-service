package dealerShipOrder.presentation.controllers.orderControllers;

import dealerShipOrder.application.services.orderService.OrderService;
import dealerShipOrder.presentation.dtos.request.orderRequestPresentationDto.OrderFilterPresentationRequest;
import dealerShipOrder.presentation.dtos.response.orderResponsePresentationDto.OrderListPresentationResponse;
import dealerShipOrder.presentation.dtos.response.orderResponsePresentationDto.OrderPresentationResponse;
import dealerShipOrder.presentation.mappers.OrderPresentationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders (Public)", description = "Public order endpoints")
public class OrderPublicController {

    private final OrderService orderService;
    private final OrderPresentationMapper mapper;

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<OrderPresentationResponse> getOrder(@PathVariable String id) {
        var response = orderService.getOrderById(id);
        return ResponseEntity.ok(mapper.toPresentation(response));
    }

    @GetMapping
    @Operation(summary = "Get all orders with filters")
    public ResponseEntity<OrderListPresentationResponse> getAllOrders(@Valid OrderFilterPresentationRequest request) {
        var appFilter = mapper.toApplication(request);
        var response = orderService.getOrdersWithFilters(appFilter);
        return ResponseEntity.ok(mapper.toListPresentation(response));
    }
}