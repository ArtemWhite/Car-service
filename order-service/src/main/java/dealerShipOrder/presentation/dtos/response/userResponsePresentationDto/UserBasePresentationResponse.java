package dealerShipOrder.presentation.dtos.response.userResponsePresentationDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@Schema(description = "Base user response")
public class UserBasePresentationResponse {

    @Schema(description = "User ID", example = "user_123e4567")
    private String id;

    @Schema(description = "First name", example = "John")
    private String firstName;

    @Schema(description = "Last name", example = "Doe")
    private String lastName;

    @Schema(description = "Middle name", example = "Michael")
    private String middleName;

    @Schema(description = "Full name", example = "John Michael Doe")
    private String fullName;

    @Schema(description = "Email address", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Phone number", example = "+79123456789")
    private String phone;

    @Schema(description = "User type", example = "CLIENT")
    private String userType;

    @Schema(description = "User status code", example = "ACTIVE")
    private String status;

    @Schema(description = "User status display name", example = "Active")
    private String statusDisplayName;

    @Schema(description = "Registration date")
    private LocalDateTime registeredAt;

    @Schema(description = "Last active date")
    private LocalDateTime lastActiveAt;

    @Schema(description = "Employee ID (for staff)", example = "EMP001")
    private String employeeId;
}