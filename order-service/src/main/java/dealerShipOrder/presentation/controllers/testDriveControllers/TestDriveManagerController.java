package dealerShipOrder.presentation.controllers.testDriveControllers;

import dealerShipOrder.application.services.testDriveService.manager.TestDriveManagerService;
import dealerShipOrder.presentation.dtos.response.testDriveResponsePresentationDto.TestDriveListPresentationResponse;
import dealerShipOrder.presentation.dtos.response.testDriveResponsePresentationDto.TestDrivePresentationResponse;
import dealerShipOrder.presentation.mappers.TestDrivePresentationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/manager/test-drives")
@RequiredArgsConstructor
@Tag(name = "Test Drives (Manager)", description = "Test drive management for managers")
public class TestDriveManagerController {

    private final TestDriveManagerService testDriveManagerService;
    private final TestDrivePresentationMapper mapper;

    @PostMapping("/{id}/assign")
    @Operation(summary = "Assign test drive to manager")
    @PreAuthorize("hasAnyRole('MANAGER', 'SYSTEM_ADMIN')")
    public ResponseEntity<TestDrivePresentationResponse> assignManager(@PathVariable String id) {
        var response = testDriveManagerService.assignManager(id);
        return ResponseEntity.ok(mapper.toPresentation(response));
    }

    @GetMapping("/my")
    @Operation(summary = "Get my assigned test drives")
    @PreAuthorize("hasAnyRole('MANAGER', 'SYSTEM_ADMIN')")
    public ResponseEntity<TestDriveListPresentationResponse> getMyRequests() {
        var response = testDriveManagerService.getMyRequests();
        return ResponseEntity.ok(mapper.toListPresentation(response));
    }

    @GetMapping("/pending")
    @Operation(summary = "Get all pending test drive requests")
    @PreAuthorize("hasAnyRole('MANAGER', 'SYSTEM_ADMIN')")
    public ResponseEntity<TestDriveListPresentationResponse> getPendingRequests() {
        var response = testDriveManagerService.getPendingRequests();
        return ResponseEntity.ok(mapper.toListPresentation(response));
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "Confirm test drive request with time")
    @PreAuthorize("hasAnyRole('MANAGER', 'SYSTEM_ADMIN')")
    public ResponseEntity<TestDrivePresentationResponse> confirmRequest(
            @PathVariable String id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime time) {
        var response = testDriveManagerService.confirmRequest(id, time);
        return ResponseEntity.ok(mapper.toPresentation(response));
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Complete test drive")
    @PreAuthorize("hasAnyRole('MANAGER', 'SYSTEM_ADMIN')")
    public ResponseEntity<TestDrivePresentationResponse> completeRequest(@PathVariable String id) {
        var response = testDriveManagerService.completeRequest(id);
        return ResponseEntity.ok(mapper.toPresentation(response));
    }

    @PostMapping("/{id}/no-show")
    @Operation(summary = "Mark test drive as no-show")
    @PreAuthorize("hasAnyRole('MANAGER', 'SYSTEM_ADMIN')")
    public ResponseEntity<TestDrivePresentationResponse> markNoShow(@PathVariable String id) {
        var response = testDriveManagerService.markNoShow(id);
        return ResponseEntity.ok(mapper.toPresentation(response));
    }
}