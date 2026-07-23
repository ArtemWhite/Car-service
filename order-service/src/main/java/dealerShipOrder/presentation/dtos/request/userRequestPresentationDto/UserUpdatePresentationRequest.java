package dealerShipOrder.presentation.dtos.request.userRequestPresentationDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update a user")
public class UserUpdatePresentationRequest {

    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Schema(description = "First name", example = "John")
    private String firstName;

    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @Schema(description = "Last name", example = "Doe")
    private String lastName;

    @Size(max = 50, message = "Middle name cannot exceed 50 characters")
    @Schema(description = "Middle name", example = "Michael")
    private String middleName;

    @Email(message = "Invalid email format")
    @Schema(description = "Email address", example = "john.doe@example.com")
    private String email;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number format")
    @Schema(description = "Phone number", example = "+79123456789")
    private String phone;

    @Pattern(regexp = "^(ACTIVE|INACTIVE|BLOCKED)$", message = "Status must be ACTIVE, INACTIVE, or BLOCKED")
    @Schema(description = "User status", example = "ACTIVE")
    private String status;

    @Schema(description = "Position (for managers)", example = "SALES_MANAGER")
    private String position;

    @Schema(description = "Whether manager is available", example = "true")
    private Boolean available;

    @Schema(description = "Warehouse position (for warehouse admins)", example = "SECTION_LEAD")
    private String warehousePosition;

    @Schema(description = "Managed section IDs (for warehouse admins)")
    private Set<String> managedSectionIds;

    @Schema(description = "Preferred contact method (for clients)", example = "EMAIL")
    private String preferredContactMethod;

    @Schema(description = "Whether subscribed to newsletter", example = "true")
    private Boolean newsletterSubscribed;
}