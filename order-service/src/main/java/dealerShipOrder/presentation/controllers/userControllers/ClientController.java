package dealerShipOrder.presentation.controllers.userControllers;

import dealerShipOrder.application.services.userService.client.ClientService;
import dealerShipOrder.application.services.orderService.client.OrderClientService;
import dealerShipOrder.application.services.testDriveService.client.TestDriveClientService;
import dealerShipOrder.presentation.dtos.response.userResponsePresentationDto.ClientPresentationResponse;
import dealerShipOrder.presentation.dtos.response.orderResponsePresentationDto.OrderPresentationResponse;
import dealerShipOrder.presentation.dtos.response.testDriveResponsePresentationDto.TestDrivePresentationResponse;
import dealerShipOrder.presentation.mappers.UserPresentationMapper;
import dealerShipOrder.presentation.mappers.OrderPresentationMapper;
import dealerShipOrder.presentation.mappers.TestDrivePresentationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/client")
@RequiredArgsConstructor
@Tag(name = "Client", description = "Client specific operations")
public class ClientController {

    private final ClientService clientService;
    private final OrderClientService orderClientService;
    private final TestDriveClientService testDriveClientService;
    private final UserPresentationMapper mapper;
    private final OrderPresentationMapper orderMapper;
    private final TestDrivePresentationMapper testDriveMapper;

    @GetMapping("/me")
    @Operation(summary = "Get current client profile")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ClientPresentationResponse> getMyProfile() {
        var response = clientService.getMyProfile();
        return ResponseEntity.ok(mapper.toPresentation(response));
    }

    @PostMapping("/me/newsletter/subscribe")
    @Operation(summary = "Subscribe to newsletter")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ClientPresentationResponse> subscribeToNewsletter() {
        var response = clientService.subscribeToNewsletter();
        return ResponseEntity.ok(mapper.toPresentation(response));
    }

    @PostMapping("/me/newsletter/unsubscribe")
    @Operation(summary = "Unsubscribe from newsletter")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ClientPresentationResponse> unsubscribeFromNewsletter() {
        var response = clientService.unsubscribeFromNewsletter();
        return ResponseEntity.ok(mapper.toPresentation(response));
    }

    @PutMapping("/me/contact-method")
    @Operation(summary = "Set preferred contact method")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ClientPresentationResponse> setPreferredContactMethod(@RequestParam String method) {
        var response = clientService.setPreferredContactMethod(method);
        return ResponseEntity.ok(mapper.toPresentation(response));
    }

    @GetMapping("/me/orders")
    @Operation(summary = "Get my orders")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<List<OrderPresentationResponse>> getMyOrders() {
        var response = orderClientService.getMyOrders();
        return ResponseEntity.ok(response.stream().map(orderMapper::toPresentation).collect(Collectors.toList()));
    }

    @GetMapping("/me/test-drives")
    @Operation(summary = "Get my test drives")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<List<TestDrivePresentationResponse>> getMyTestDrives() {
        var response = testDriveClientService.getMyRequests();
        return ResponseEntity.ok(response.stream().map(testDriveMapper::toPresentation).collect(Collectors.toList()));
    }
}