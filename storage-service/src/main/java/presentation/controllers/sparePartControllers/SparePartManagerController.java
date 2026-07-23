package presentation.controllers.sparePartControllers;

import application.services.sparePartService.manager.SparePartManagerService;
import presentation.dtos.response.spareResponsePresentationDto.SparePartListPresentationResponse;
import presentation.mappers.SparePartPresentationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/manager/spare-parts")
@RequiredArgsConstructor
@Tag(name = "Spare Parts (Manager)", description = "Spare part management for managers")
public class SparePartManagerController {

    private final SparePartManagerService sparePartManagerService;
    private final SparePartPresentationMapper mapper;

    @GetMapping("/low-stock")
    @Operation(summary = "Get low stock spare parts")
    @PreAuthorize("hasAnyRole('MANAGER', 'SYSTEM_ADMIN')")
    public ResponseEntity<SparePartListPresentationResponse> getLowStockParts(@RequestParam(defaultValue = "5") int threshold) {
        var response = sparePartManagerService.getLowStockParts(threshold);
        return ResponseEntity.ok(mapper.toListPresentation(response));
    }

    @GetMapping("/out-of-stock")
    @Operation(summary = "Get out of stock spare parts")
    @PreAuthorize("hasAnyRole('MANAGER', 'SYSTEM_ADMIN')")
    public ResponseEntity<SparePartListPresentationResponse> getOutOfStockParts() {
        var response = sparePartManagerService.getOutOfStockParts();
        return ResponseEntity.ok(mapper.toListPresentation(response));
    }

    @PostMapping("/{id}/restock")
    @Operation(summary = "Request restock for spare part")
    @PreAuthorize("hasAnyRole('MANAGER', 'SYSTEM_ADMIN')")
    public ResponseEntity<Void> requestRestock(@PathVariable String id, @RequestParam int quantity) {
        sparePartManagerService.requestRestock(id, quantity);
        return ResponseEntity.ok().build();
    }
}