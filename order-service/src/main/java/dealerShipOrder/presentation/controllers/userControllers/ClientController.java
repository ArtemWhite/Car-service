package dealerShipOrder.presentation.controllers.userControllers;

import dealerShipOrder.application.services.userService.client.ClientService;
import dealerShipOrder.presentation.dtos.response.userResponsePresentationDto.ClientPresentationResponse;
import dealerShipOrder.presentation.mappers.UserPresentationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/client")
@RequiredArgsConstructor
@Tag(name = "Client", description = "Client specific operations")
public class ClientController {

    private final ClientService clientService;
    private final UserPresentationMapper mapper;

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
}