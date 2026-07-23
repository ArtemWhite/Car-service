package presentation.controllers.carControllers;

import application.services.carService.manager.CarManagerService;
import presentation.dtos.response.carResponsePresentationDto.CarListConfigurationPresentationResponse;
import presentation.dtos.response.carResponsePresentationDto.CarListPresentationResponse;
import presentation.mappers.CarPresentationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/manager/cars")
@RequiredArgsConstructor
@Tag(name = "Cars (Manager)", description = "Car management for managers")
public class CarManagerController {

    private final CarManagerService managerService;
    private final CarPresentationMapper mapper;

    @PostMapping("/{id}/test-drive-fleet")
    @Operation(summary = "Add car to test drive fleet")
    @PreAuthorize("hasAnyRole('MANAGER', 'SYSTEM_ADMIN')")
    public ResponseEntity<Void> addToTestDriveFleet(@PathVariable String id) {
        managerService.addCarToTestDriveFleet(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/test-drive-fleet")
    @Operation(summary = "Remove car from test drive fleet")
    @PreAuthorize("hasAnyRole('MANAGER', 'SYSTEM_ADMIN')")
    public ResponseEntity<Void> removeFromTestDriveFleet(@PathVariable String id) {
        managerService.removeCarFromTestDriveFleet(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/test-drive-fleet")
    @Operation(summary = "Get all cars in test drive fleet")
    @PreAuthorize("hasAnyRole('MANAGER', 'SYSTEM_ADMIN')")
    public ResponseEntity<CarListPresentationResponse> getTestDriveFleet() {
        var response = managerService.getTestDriveFleet();
        return ResponseEntity.ok(mapper.toPresentation(response));
    }

    @GetMapping("/orders/available-cars")
    @Operation(summary = "Get cars with paid orders")
    @PreAuthorize("hasAnyRole('MANAGER', 'SYSTEM_ADMIN')")
    public ResponseEntity<CarListPresentationResponse> getOrdersOnAvailableCars() {
        var response = managerService.getOrdersOnAvailableCars();
        return ResponseEntity.ok(mapper.toPresentation(response));
    }

    @GetMapping("/orders/configuration-cars")
    @Operation(summary = "Get configurations for custom orders")
    @PreAuthorize("hasAnyRole('MANAGER', 'SYSTEM_ADMIN')")
    public ResponseEntity<CarListConfigurationPresentationResponse> getOrdersOnConfigurationCars() {
        var response = managerService.getOrdersOnConfigurationCars();
        return ResponseEntity.ok(mapper.toConfigurationListPresentation(response));
    }
}