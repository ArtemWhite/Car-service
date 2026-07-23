package presentation.controllers.sparePartControllers;

import application.services.sparePartService.SparePartService;
import presentation.dtos.request.spareRequestPresentationDto.SparePartFilterPresentationRequest;
import presentation.dtos.response.spareResponsePresentationDto.SparePartListPresentationResponse;
import presentation.dtos.response.spareResponsePresentationDto.SparePartPresentationResponse;
import presentation.mappers.SparePartPresentationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/spare-parts")
@RequiredArgsConstructor
@Tag(name = "Spare Parts (Public)", description = "Public spare part endpoints")
public class SparePartPublicController {

    private final SparePartService sparePartService;
    private final SparePartPresentationMapper mapper;

    @GetMapping("/{id}")
    @Operation(summary = "Get spare part by ID")
    public ResponseEntity<SparePartPresentationResponse> getSparePart(@PathVariable String id) {
        var response = sparePartService.getSparePartById(id);
        return ResponseEntity.ok(mapper.toPresentation(response));
    }

    @GetMapping
    @Operation(summary = "Get all spare parts with filters")
    public ResponseEntity<SparePartListPresentationResponse> getAllSpareParts(@Valid SparePartFilterPresentationRequest request) {
        var appFilter = mapper.toApplication(request);
        var response = sparePartService.getSparePartsWithFilters(appFilter);
        return ResponseEntity.ok(mapper.toListPresentation(response));
    }

    @GetMapping("/type/{spareType}")
    @Operation(summary = "Get spare parts by type")
    public ResponseEntity<SparePartListPresentationResponse> getSparePartsByType(@PathVariable String spareType) {
        var response = sparePartService.getSparePartsByType(spareType);
        return ResponseEntity.ok(mapper.toListPresentation(response));
    }
}