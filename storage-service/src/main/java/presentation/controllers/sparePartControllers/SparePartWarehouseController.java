package presentation.controllers.sparePartControllers;

import application.services.sparePartService.warehouseAdmin.SparePartWarehouseAdminService;
import presentation.dtos.request.spareRequestPresentationDto.SparePartStockUpdatePresentationRequest;
import presentation.dtos.response.spareResponsePresentationDto.SparePartPresentationResponse;
import presentation.mappers.SparePartPresentationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/warehouse/spare-parts")
@RequiredArgsConstructor
@Tag(name = "Spare Parts (Warehouse)", description = "Warehouse operations for spare parts")
public class SparePartWarehouseController {

    private final SparePartWarehouseAdminService sparePartWarehouseService;
    private final SparePartPresentationMapper mapper;

    @PatchMapping("/stock")
    @Operation(summary = "Update stock quantity")
    @PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<SparePartPresentationResponse> updateStock(@Valid @RequestBody SparePartStockUpdatePresentationRequest request) {
        var appRequest = mapper.toApplication(request);
        var response = sparePartWarehouseService.updateStock(appRequest);
        return ResponseEntity.ok(mapper.toPresentation(response));
    }

    @PostMapping("/{id}/receive")
    @Operation(summary = "Receive shipment")
    @PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<SparePartPresentationResponse> receiveShipment(@PathVariable String id, @RequestParam int quantity) {
        var response = sparePartWarehouseService.receiveShipment(id, quantity);
        return ResponseEntity.ok(mapper.toPresentation(response));
    }

    @PatchMapping("/{id}/location")
    @Operation(summary = "Move to location")
    @PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<SparePartPresentationResponse> moveToLocation(
            @PathVariable String id,
            @RequestParam String section,
            @RequestParam String location) {
        var response = sparePartWarehouseService.moveToLocation(id, section, location);
        return ResponseEntity.ok(mapper.toPresentation(response));
    }

    @PostMapping("/{id}/write-off")
    @Operation(summary = "Write off spare parts")
    @PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<SparePartPresentationResponse> writeOff(
            @PathVariable String id,
            @RequestParam int quantity,
            @RequestParam String reason) {
        var response = sparePartWarehouseService.writeOff(id, quantity, reason);
        return ResponseEntity.ok(mapper.toPresentation(response));
    }
}