package presentation.controllers.carControllers;

import application.services.carService.adminSystem.CarSystemAdminService;
import presentation.dtos.request.carRequestPresentationDto.*;
import presentation.dtos.response.carResponsePresentationDto.CarPresentationResponse;
import presentation.mappers.CarPresentationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/admin/cars")
@RequiredArgsConstructor
@Tag(name = "Cars (Admin)", description = "Car management for admins")
public class CarAdminController {

    private final CarSystemAdminService adminService;
    private final CarPresentationMapper mapper;

    @PostMapping
    @Operation(summary = "Create a new car")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<CarPresentationResponse> createCar(@Valid @RequestBody CarCreatePresentationRequest request) {
        var appRequest = mapper.toApplication(request);
        var response = adminService.createCar(appRequest);
        return ResponseEntity.status(201).body(mapper.toPresentation(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update car")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<CarPresentationResponse> updateCar(
            @PathVariable String id,
            @Valid @RequestBody CarUpdatePresentationRequest request) {
        var appRequest = mapper.toApplication(request);
        var response = adminService.updateCar(id, appRequest);
        return ResponseEntity.ok(mapper.toPresentation(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete car (soft delete)")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Void> deleteCar(@PathVariable String id, @RequestParam String reason) {
        adminService.deleteCar(id, reason);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Change car status")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<CarPresentationResponse> changeCarStatus(
            @PathVariable String id,
            @Valid @RequestBody CarChangeStatusPresentationRequest request) {
        var response = adminService.changeCarStatus(id, request.getStatus());
        return ResponseEntity.ok(mapper.toPresentation(response));
    }
}