package presentation.controllers.sparePartControllers;

import application.services.sparePartService.systemAdmin.SparePartSystemAdminService;
import presentation.dtos.request.spareRequestPresentationDto.SparePartCreatePresentationRequest;
import presentation.dtos.request.spareRequestPresentationDto.SparePartUpdatePresentationRequest;
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
@RequestMapping("/api/admin/spare-parts")
@RequiredArgsConstructor
@Tag(name = "Spare Parts (Admin)", description = "Spare part management for admins")
public class SparePartSystemAdminController {

    private final SparePartSystemAdminService sparePartAdminService;
    private final SparePartPresentationMapper mapper;

    @PostMapping
    @Operation(summary = "Create a new spare part")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<SparePartPresentationResponse> createSparePart(@Valid @RequestBody SparePartCreatePresentationRequest request) {
        var appRequest = mapper.toApplication(request);
        var response = sparePartAdminService.createSparePart(appRequest);
        return ResponseEntity.status(201).body(mapper.toPresentation(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update spare part")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<SparePartPresentationResponse> updateSparePart(
            @PathVariable String id,
            @Valid @RequestBody SparePartUpdatePresentationRequest request) {
        var appRequest = mapper.toApplication(request);
        var response = sparePartAdminService.updateSparePart(id, appRequest);
        return ResponseEntity.ok(mapper.toPresentation(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete spare part")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Void> deleteSparePart(@PathVariable String id, @RequestParam String reason) {
        sparePartAdminService.deleteSparePart(id, reason);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{sparePartId}/compatible-models/{modelId}")
    @Operation(summary = "Add compatible model to spare part")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Void> addCompatibleModel(
            @PathVariable String sparePartId,
            @PathVariable String modelId) {
        sparePartAdminService.addCompatibleModel(sparePartId, modelId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{sparePartId}/compatible-models/{modelId}")
    @Operation(summary = "Remove compatible model from spare part")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Void> removeCompatibleModel(
            @PathVariable String sparePartId,
            @PathVariable String modelId) {
        sparePartAdminService.removeCompatibleModel(sparePartId, modelId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get spare part by ID for admin")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<SparePartPresentationResponse> getSparePartById(@PathVariable String id) {
        var response = sparePartAdminService.getSparePartById(id);
        return ResponseEntity.ok(mapper.toPresentation(response));
    }
}