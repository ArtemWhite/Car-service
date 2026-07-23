package dealerShipOrder.presentation.controllers.userControllers;

import dealerShipOrder.application.services.userService.warehouseAdmin.WarehouseAdminService;
import dealerShipOrder.presentation.dtos.response.userResponsePresentationDto.ManagedSectionsPresentationResponse;
import dealerShipOrder.presentation.dtos.response.userResponsePresentationDto.OnDutyPresentationResponse;
import dealerShipOrder.presentation.dtos.response.userResponsePresentationDto.OperationHistoryListPresentationResponse;
import dealerShipOrder.presentation.dtos.response.userResponsePresentationDto.WarehouseAdminPresentationResponse;
import dealerShipOrder.presentation.mappers.UserPresentationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/warehouse-admin")
@RequiredArgsConstructor
@Tag(name = "Warehouse Admin", description = "Warehouse admin operations")
public class WarehouseAdminController {

    private final WarehouseAdminService warehouseAdminService;
    private final UserPresentationMapper mapper;

    @PostMapping("/sections/{sectionId}/assign")
    @Operation(summary = "Assign to section")
    @PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<WarehouseAdminPresentationResponse> assignToSection(@PathVariable String sectionId) {
        var response = warehouseAdminService.assignToSection(sectionId);
        return ResponseEntity.ok(mapper.toWarehouseAdminPresentation(response));
    }

    @DeleteMapping("/sections/{sectionId}/remove")
    @Operation(summary = "Remove from section")
    @PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<WarehouseAdminPresentationResponse> removeFromSection(@PathVariable String sectionId) {
        var response = warehouseAdminService.removeFromSection(sectionId);
        return ResponseEntity.ok(mapper.toWarehouseAdminPresentation(response));
    }

    @GetMapping("/sections")
    @Operation(summary = "Get managed sections")
    @PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<ManagedSectionsPresentationResponse> getManagedSections() {
        var response = warehouseAdminService.getManagedSections();
        return ResponseEntity.ok(mapper.toManagedSectionsPresentation(response));
    }

    @PostMapping("/shift/start")
    @Operation(summary = "Start shift")
    @PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<WarehouseAdminPresentationResponse> startShift() {
        var response = warehouseAdminService.startShift();
        return ResponseEntity.ok(mapper.toWarehouseAdminPresentation(response));
    }

    @PostMapping("/shift/end")
    @Operation(summary = "End shift")
    @PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<WarehouseAdminPresentationResponse> endShift() {
        var response = warehouseAdminService.endShift();
        return ResponseEntity.ok(mapper.toWarehouseAdminPresentation(response));
    }

    @GetMapping("/shift/status")
    @Operation(summary = "Check if on duty")
    @PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<OnDutyPresentationResponse> isOnDuty() {
        var response = warehouseAdminService.isOnDuty();
        return ResponseEntity.ok(mapper.toOnDutyPresentation(response));
    }

    @GetMapping("/history")
    @Operation(summary = "Get operation history")
    @PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<OperationHistoryListPresentationResponse> getOperationHistory() {
        var response = warehouseAdminService.getOperationHistory();
        return ResponseEntity.ok(mapper.toOperationHistoryListPresentation(response));
    }

    @GetMapping("/history/date-range")
    @Operation(summary = "Get operations by date range")
    @PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<OperationHistoryListPresentationResponse> getOperationsByDate(
            @RequestParam String from,
            @RequestParam String to) {
        var response = warehouseAdminService.getOperationsByDate(from, to);
        return ResponseEntity.ok(mapper.toOperationHistoryListPresentation(response));
    }

    @GetMapping("/history/type/{operationType}")
    @Operation(summary = "Get operations by type")
    @PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<OperationHistoryListPresentationResponse> getOperationsByType(@PathVariable String operationType) {
        var response = warehouseAdminService.getOperationsByType(operationType);
        return ResponseEntity.ok(mapper.toOperationHistoryListPresentation(response));
    }
}