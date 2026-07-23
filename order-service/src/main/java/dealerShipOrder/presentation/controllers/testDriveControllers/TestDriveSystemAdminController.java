package dealerShipOrder.presentation.controllers.testDriveControllers;

import dealerShipOrder.application.services.testDriveService.systemAdmin.TestDriveSystemAdminService;
import dealerShipOrder.presentation.dtos.request.testDriveRequestPresentationDto.TestDriveUpdatePresentationRequest;
import dealerShipOrder.presentation.dtos.response.testDriveResponsePresentationDto.TestDriveListPresentationResponse;
import dealerShipOrder.presentation.dtos.response.testDriveResponsePresentationDto.TestDrivePresentationResponse;
import dealerShipOrder.presentation.mappers.TestDrivePresentationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/admin/test-drives")
@RequiredArgsConstructor
@Tag(name = "Test Drives (Admin)", description = "Test drive management for admins")
public class TestDriveSystemAdminController {

    private final TestDriveSystemAdminService testDriveAdminService;
    private final TestDrivePresentationMapper mapper;

    @PutMapping("/{id}")
    @Operation(summary = "Update test drive request")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<TestDrivePresentationResponse> updateRequest(
            @PathVariable String id,
            @Valid @RequestBody TestDriveUpdatePresentationRequest request) {
        var appRequest = mapper.toApplication(request);
        var response = testDriveAdminService.updateRequest(id, appRequest);
        return ResponseEntity.ok(mapper.toPresentation(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete test drive request")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Void> deleteRequest(@PathVariable String id, @RequestParam String reason) {
        testDriveAdminService.deleteRequest(id, reason);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get test drives by status")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<TestDriveListPresentationResponse> getRequestsByStatus(@PathVariable String status) {
        var response = testDriveAdminService.getRequestsByStatus(status);
        return ResponseEntity.ok(mapper.toListPresentation(response));
    }
}