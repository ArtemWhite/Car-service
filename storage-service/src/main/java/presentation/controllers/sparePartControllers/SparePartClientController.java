package presentation.controllers.sparePartControllers;

import application.services.sparePartService.client.SparePartClientService;
import presentation.dtos.response.spareResponsePresentationDto.SparePartListPresentationResponse;
import presentation.dtos.response.spareResponsePresentationDto.SparePartPresentationResponse;
import presentation.mappers.SparePartPresentationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/client/spare-parts")
@RequiredArgsConstructor
@Tag(name = "Spare Parts (Client)", description = "Spare part operations for clients")
public class SparePartClientController {

    private final SparePartClientService sparePartClientService;
    private final SparePartPresentationMapper mapper;

    @GetMapping("/compatible/{carModelId}")
    @Operation(summary = "Find compatible spare parts for car model")
    @PreAuthorize("hasAnyRole('CLIENT', 'MANAGER', 'SYSTEM_ADMIN')")
    public ResponseEntity<SparePartListPresentationResponse> findCompatibleSpareParts(@PathVariable String carModelId) {
        var response = sparePartClientService.findCompatibleSpareParts(carModelId);
        return ResponseEntity.ok(mapper.toListPresentation(response));
    }

    @GetMapping("/{id}/details")
    @Operation(summary = "Get spare part details")
    @PreAuthorize("hasAnyRole('CLIENT', 'MANAGER', 'SYSTEM_ADMIN')")
    public ResponseEntity<SparePartPresentationResponse> getSparePartDetails(@PathVariable String id) {
        var response = sparePartClientService.getSparePartDetails(id);
        return ResponseEntity.ok(mapper.toPresentation(response));
    }

    @GetMapping("/search")
    @Operation(summary = "Search spare parts by query")
    @PreAuthorize("hasAnyRole('CLIENT', 'MANAGER', 'SYSTEM_ADMIN')")
    public ResponseEntity<SparePartListPresentationResponse> searchSpareParts(@RequestParam String query) {
        var response = sparePartClientService.searchSpareParts(query);
        return ResponseEntity.ok(mapper.toListPresentation(response));
    }
}