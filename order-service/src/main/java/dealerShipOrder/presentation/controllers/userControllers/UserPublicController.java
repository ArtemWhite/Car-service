package dealerShipOrder.presentation.controllers.userControllers;

import dealerShipOrder.application.services.userService.UserService;
import dealerShipOrder.presentation.dtos.request.userRequestPresentationDto.UserChangePasswordPresentationRequest;
import dealerShipOrder.presentation.dtos.request.userRequestPresentationDto.UserUpdatePresentationRequest;
import dealerShipOrder.presentation.dtos.response.userResponsePresentationDto.UserBasePresentationResponse;
import dealerShipOrder.presentation.mappers.UserPresentationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users (Public)", description = "Public user endpoints")
public class UserPublicController {

    private final UserService userService;
    private final UserPresentationMapper mapper;

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<UserBasePresentationResponse> getUserById(@PathVariable String id) {
        var response = userService.getUserById(id);
        return ResponseEntity.ok(mapper.toPresentation(response));
    }

    @PutMapping("/me")
    @Operation(summary = "Update own profile")
    public ResponseEntity<UserBasePresentationResponse> updateOwnProfile(@Valid @RequestBody UserUpdatePresentationRequest request) {
        var appRequest = mapper.toApplicationForOwnProfile(request);
        var response = userService.updateOwnProfile(appRequest);
        return ResponseEntity.ok(mapper.toPresentation(response));
    }

    @PostMapping("/me/password")
    @Operation(summary = "Change own password")
    public ResponseEntity<UserBasePresentationResponse> changeOwnPassword(@Valid @RequestBody UserChangePasswordPresentationRequest request) {
        var appRequest = mapper.toApplicationForOwnProfile(request);
        var response = userService.changeOwnPassword(appRequest);
        return ResponseEntity.ok(mapper.toPresentation(response));
    }
}