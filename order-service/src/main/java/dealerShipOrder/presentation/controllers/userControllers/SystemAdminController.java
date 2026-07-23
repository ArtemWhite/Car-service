package dealerShipOrder.presentation.controllers.userControllers;

import dealerShipOrder.application.services.userService.systemAdmin.SystemAdminService;
import dealerShipOrder.presentation.dtos.request.userRequestPresentationDto.UserCreatePresentationRequest;
import dealerShipOrder.presentation.dtos.request.userRequestPresentationDto.UserFilterPresentationRequest;
import dealerShipOrder.presentation.dtos.request.userRequestPresentationDto.UserUpdatePresentationRequest;
import dealerShipOrder.presentation.dtos.response.userResponsePresentationDto.OperationHistoryListPresentationResponse;
import dealerShipOrder.presentation.dtos.response.userResponsePresentationDto.SystemAdminPresentationResponse;
import dealerShipOrder.presentation.dtos.response.userResponsePresentationDto.UserBasePresentationResponse;
import dealerShipOrder.presentation.dtos.response.userResponsePresentationDto.UserListPresentationResponse;
import dealerShipOrder.presentation.mappers.UserPresentationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "System Admin", description = "System administration operations")
public class SystemAdminController {

    private final SystemAdminService systemAdminService;
    private final UserPresentationMapper mapper;

    @PostMapping("/users")
    @Operation(summary = "Create user")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<UserBasePresentationResponse> createUser(@Valid @RequestBody UserCreatePresentationRequest request) {
        var appRequest = mapper.toApplication(request);
        var response = systemAdminService.createUser(appRequest);
        return ResponseEntity.status(201).body(mapper.toPresentation(response));
    }

    @PutMapping("/users/{userId}")
    @Operation(summary = "Update user")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<UserBasePresentationResponse> updateUser(
            @PathVariable String userId,
            @Valid @RequestBody UserUpdatePresentationRequest request) {
        var appRequest = mapper.toApplication(request, userId);
        var response = systemAdminService.updateUser(userId, appRequest);
        return ResponseEntity.ok(mapper.toPresentation(response));
    }

    @DeleteMapping("/users/{userId}")
    @Operation(summary = "Delete user")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId, @RequestParam String reason) {
        systemAdminService.deleteUser(userId, reason);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/users/{userId}/block")
    @Operation(summary = "Block user")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<UserBasePresentationResponse> blockUser(
            @PathVariable String userId,
            @RequestParam String reason) {
        var response = systemAdminService.blockUser(userId, reason);
        return ResponseEntity.ok(mapper.toPresentation(response));
    }

    @PostMapping("/users/{userId}/unblock")
    @Operation(summary = "Unblock user")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<UserBasePresentationResponse> unblockUser(@PathVariable String userId) {
        var response = systemAdminService.unblockUser(userId);
        return ResponseEntity.ok(mapper.toPresentation(response));
    }

    @GetMapping("/users")
    @Operation(summary = "Get all users")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<UserListPresentationResponse> getAllUsers() {
        var response = systemAdminService.getAllUsers();
        return ResponseEntity.ok(mapper.toUserListPresentation(response));
    }

    @GetMapping("/users/type/{userType}")
    @Operation(summary = "Get users by type")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<UserListPresentationResponse> getUsersByType(@PathVariable String userType) {
        var response = systemAdminService.getUsersByType(userType);
        return ResponseEntity.ok(mapper.toUserListPresentation(response));
    }

    @GetMapping("/users/filters")
    @Operation(summary = "Get users with filters")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<UserListPresentationResponse> getUsersWithFilters(@Valid UserFilterPresentationRequest request) {
        var appFilter = mapper.toApplication(request);
        var response = systemAdminService.getUsersWithFilters(appFilter);
        return ResponseEntity.ok(mapper.toUserListPresentationFromListResponse(response));
    }

    @GetMapping("/users/{userId}/details")
    @Operation(summary = "Get user details")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<UserBasePresentationResponse> getUserDetails(@PathVariable String userId) {
        var response = systemAdminService.getUserDetails(userId);
        return ResponseEntity.ok(mapper.toPresentation(response));
    }

    @PostMapping("/managers/{managerId}/promote")
    @Operation(summary = "Promote manager")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<SystemAdminPresentationResponse> promoteManager(
            @PathVariable String managerId,
            @RequestParam String newPosition) {
        var response = systemAdminService.promoteManager(managerId, newPosition);
        return ResponseEntity.ok(mapper.toSystemAdminPresentation(response));
    }

    @PostMapping("/warehouse-admins/{targetAdminId}/promote")
    @Operation(summary = "Promote warehouse admin")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<SystemAdminPresentationResponse> promoteWarehouseAdmin(
            @PathVariable String targetAdminId,
            @RequestParam String newPosition) {
        var response = systemAdminService.promoteWarehouseAdmin(targetAdminId, newPosition);
        return ResponseEntity.ok(mapper.toSystemAdminPresentation(response));
    }

    @PostMapping("/admins/{targetAdminId}/permissions/{permission}")
    @Operation(summary = "Grant permission to admin")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<SystemAdminPresentationResponse> grantPermission(
            @PathVariable String targetAdminId,
            @PathVariable String permission) {
        var response = systemAdminService.grantPermission(targetAdminId,
                dealerShipOrder.domain.models.users.systemAdmin.SystemPermission.valueOf(permission));
        return ResponseEntity.ok(mapper.toSystemAdminPresentation(response));
    }

    @DeleteMapping("/admins/{targetAdminId}/permissions/{permission}")
    @Operation(summary = "Revoke permission from admin")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<SystemAdminPresentationResponse> revokePermission(
            @PathVariable String targetAdminId,
            @PathVariable String permission) {
        var response = systemAdminService.revokePermission(targetAdminId,
                dealerShipOrder.domain.models.users.systemAdmin.SystemPermission.valueOf(permission));
        return ResponseEntity.ok(mapper.toSystemAdminPresentation(response));
    }

    @PostMapping("/admins/{targetAdminId}/promote")
    @Operation(summary = "Promote admin")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<SystemAdminPresentationResponse> promoteAdmin(
            @PathVariable String targetAdminId,
            @RequestParam String newLevel) {
        var response = systemAdminService.promoteAdmin(targetAdminId, newLevel);
        return ResponseEntity.ok(mapper.toSystemAdminPresentation(response));
    }

    @GetMapping("/audit-log")
    @Operation(summary = "Get audit log")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<OperationHistoryListPresentationResponse> getAuditLog() {
        var response = systemAdminService.getAuditLog();
        return ResponseEntity.ok(mapper.toOperationHistoryListPresentation(response));
    }

    @GetMapping("/audit-log/users/{userId}")
    @Operation(summary = "Get user audit log")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<OperationHistoryListPresentationResponse> getUserAuditLog(@PathVariable String userId) {
        var response = systemAdminService.getUserAuditLog(userId);
        return ResponseEntity.ok(mapper.toOperationHistoryListPresentation(response));
    }

    @PostMapping("/users/{userId}/deactivate")
    @Operation(summary = "Deactivate user")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<UserBasePresentationResponse> deactivateUser(@PathVariable String userId) {
        var response = systemAdminService.deactivateUser(userId);
        return ResponseEntity.ok(mapper.toPresentation(response));
    }
}