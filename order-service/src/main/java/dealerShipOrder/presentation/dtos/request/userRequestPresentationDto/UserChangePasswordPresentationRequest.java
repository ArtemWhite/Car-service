package dealerShipOrder.presentation.dtos.request.userRequestPresentationDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to change user password")
public class UserChangePasswordPresentationRequest {

    @NotBlank(message = "Old password is required")
    @Schema(description = "Current password", example = "oldPassword123")
    private String oldPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Schema(description = "New password", example = "newStrongPassword123")
    private String newPassword;
}