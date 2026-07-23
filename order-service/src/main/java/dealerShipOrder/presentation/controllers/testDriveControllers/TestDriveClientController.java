package dealerShipOrder.presentation.controllers.testDriveControllers;

import dealerShipOrder.application.services.testDriveService.client.TestDriveClientService;
import dealerShipOrder.presentation.dtos.request.testDriveRequestPresentationDto.TestDriveCreatePresentationRequest;
import dealerShipOrder.presentation.dtos.response.testDriveResponsePresentationDto.TestDriveListPresentationResponse;
import dealerShipOrder.presentation.dtos.response.testDriveResponsePresentationDto.TestDrivePresentationResponse;
import dealerShipOrder.presentation.mappers.TestDrivePresentationMapper;
import org.springframework.validation.annotation.Validated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/client/test-drives")
@RequiredArgsConstructor
@Validated
@Tag(name = "Test Drives (Client)", description = "Test drive operations for clients")
public class TestDriveClientController {

    private final TestDriveClientService testDriveClientService;
    private final TestDrivePresentationMapper mapper;

    @PostMapping
    @Operation(summary = "Create a test drive request")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<TestDrivePresentationResponse> createRequest(@Valid @RequestBody TestDriveCreatePresentationRequest request) {
        var appRequest = mapper.toApplicationWithoutClientId(request);
        var response = testDriveClientService.createRequest(appRequest);
        return ResponseEntity.status(201).body(mapper.toPresentation(response));
    }

    @GetMapping("/my")
    @Operation(summary = "Get my test drive requests")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<TestDriveListPresentationResponse> getMyRequests() {
        var response = testDriveClientService.getMyRequests();
        return ResponseEntity.ok(mapper.toListPresentation(response));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel my test drive request")
    @PreAuthorize("hasRole('CLIENT') and @testDriveSecurity.isOwner(#id, authentication)")
    public ResponseEntity<TestDrivePresentationResponse> cancelRequest(
            @PathVariable String id,
            @RequestParam @NotBlank(message = "Reason is required") String reason) {
        var response = testDriveClientService.cancelRequest(id, reason);
        return ResponseEntity.ok(mapper.toPresentation(response));
    }

    @PostMapping("/{id}/reschedule")
    @Operation(summary = "Reschedule my test drive request")
    @PreAuthorize("hasRole('CLIENT') and @testDriveSecurity.isOwner(#id, authentication)")
    public ResponseEntity<TestDrivePresentationResponse> rescheduleRequest(
            @PathVariable String id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newTime) {
        var response = testDriveClientService.rescheduleRequest(id, newTime);
        return ResponseEntity.ok(mapper.toPresentation(response));
    }
}