package dealerShipOrder.presentation.dtos.request.userRequestPresentationDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.*;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new user")
public class UserCreatePresentationRequest {

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Schema(description = "First name", example = "John")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @Schema(description = "Last name", example = "Doe")
    private String lastName;

    @Size(max = 50, message = "Middle name cannot exceed 50 characters")
    @Schema(description = "Middle name", example = "Michael")
    private String middleName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Email address", example = "john.doe@example.com")
    private String email;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number format")
    @Schema(description = "Phone number", example = "+79123456789")
    private String phone;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Schema(description = "Password", example = "strongPassword123")
    private String password;

    @NotBlank(message = "User type is required")
    @Pattern(regexp = "^(CLIENT|MANAGER|SYSTEM_ADMIN|WAREHOUSE_ADMIN)$",
            message = "User type must be CLIENT, MANAGER, SYSTEM_ADMIN, or WAREHOUSE_ADMIN")
    @Schema(description = "User type", example = "CLIENT")
    private String userType;

    @Schema(description = "Employee ID (for staff users)", example = "EMP001")
    private String employeeId;

    @Schema(description = "Admin level (for system admins)", example = "SUPER_ADMIN")
    private String adminLevel;

    @Schema(description = "Position (for managers)", example = "SALES_MANAGER")
    private String position;

    @Schema(description = "Managed section IDs (for warehouse admins)")
    private Set<String> managedSectionIds;
}