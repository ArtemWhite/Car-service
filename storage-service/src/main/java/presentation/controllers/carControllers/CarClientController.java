package presentation.controllers.carControllers;

import application.services.carService.client.CarClientService;
import presentation.dtos.request.carRequestPresentationDto.CarApplyConfigurationPresentationRequest;
import presentation.dtos.request.carRequestPresentationDto.CarSendTestDrivePresentationRequest;
import presentation.mappers.CarPresentationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/client/cars")
@RequiredArgsConstructor
@Tag(name = "Cars (Client)", description = "Car operations for clients")
public class CarClientController {

    private final CarClientService clientService;
    private final CarPresentationMapper mapper;

    @PostMapping("/apply-configuration")
    @Operation(summary = "Apply configuration to a car")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Void> applyConfiguration(@Valid @RequestBody CarApplyConfigurationPresentationRequest request) {
        var appRequest = mapper.toApplication(request);
        clientService.applyConfiguration(appRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/test-drive")
    @Operation(summary = "Request a test drive")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Void> requestTestDrive(
            @PathVariable String id,
            @Valid @RequestBody CarSendTestDrivePresentationRequest request) {
        clientService.sendTestDriveRequest(id, request.getRequestedTime());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/{id}/order")
    @Operation(summary = "Make an order for a car")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Void> makeOrder(@PathVariable String id) {
        clientService.makeOrderOnCar(id);
        return ResponseEntity.ok().build();
    }
}