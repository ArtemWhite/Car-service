package dealerShipOrder.presentation.controllers.userControllers;

import dealerShipOrder.application.services.userService.manager.ManagerService;
import dealerShipOrder.presentation.dtos.response.userResponsePresentationDto.ManagerPresentationResponse;
import dealerShipOrder.presentation.dtos.response.userResponsePresentationDto.UserBasePresentationResponse;
import dealerShipOrder.presentation.dtos.response.userResponsePresentationDto.UserListPresentationResponse;
import dealerShipOrder.presentation.mappers.UserPresentationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/manager")
@RequiredArgsConstructor
@Tag(name = "Manager", description = "Manager specific operations")
public class ManagerController {

    private final ManagerService managerService;
    private final UserPresentationMapper mapper;

    @GetMapping("/me")
    @Operation(summary = "Get current manager profile")
    @PreAuthorize("hasAnyRole('MANAGER', 'SYSTEM_ADMIN')")
    public ResponseEntity<UserBasePresentationResponse> getMyProfile() {
        var response = managerService.getMyProfile();
        return ResponseEntity.ok(mapper.toPresentation(response));
    }

    @PutMapping("/me/availability")
    @Operation(summary = "Set availability")
    @PreAuthorize("hasAnyRole('MANAGER', 'SYSTEM_ADMIN')")
    public ResponseEntity<ManagerPresentationResponse> setAvailability(@RequestParam boolean available) {
        var response = managerService.setAvailability(available);
        return ResponseEntity.ok(mapper.toPresentation(response));
    }

    @GetMapping("/all")
    @Operation(summary = "Get all managers")
    @PreAuthorize("hasAnyRole('MANAGER', 'SYSTEM_ADMIN')")
    public ResponseEntity<UserListPresentationResponse> getAllManagers() {
        var response = managerService.getAllManagers();
        return ResponseEntity.ok(mapper.toUserListPresentation(response));
    }

    @GetMapping("/available")
    @Operation(summary = "Get available managers")
    @PreAuthorize("hasAnyRole('MANAGER', 'SYSTEM_ADMIN')")
    public ResponseEntity<UserListPresentationResponse> getAvailableManagers() {
        var response = managerService.getAvailableManagers();
        return ResponseEntity.ok(mapper.toUserListPresentation(response));
    }
}