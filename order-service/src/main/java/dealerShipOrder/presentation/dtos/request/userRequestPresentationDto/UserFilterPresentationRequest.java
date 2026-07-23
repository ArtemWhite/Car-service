package dealerShipOrder.presentation.dtos.request.userRequestPresentationDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to filter users")
public class UserFilterPresentationRequest {

    @Schema(description = "User type", example = "CLIENT")
    private String userType;

    @Schema(description = "User status", example = "ACTIVE")
    private String status;

    @Schema(description = "Email filter", example = "john@example.com")
    private String email;

    @Schema(description = "Phone filter", example = "+79123456789")
    private String phone;

    @Schema(description = "First name filter", example = "John")
    private String firstName;

    @Schema(description = "Last name filter", example = "Doe")
    private String lastName;

    @Schema(description = "Only active users", example = "true")
    private Boolean active;

    @Schema(description = "Manager position filter", example = "SALES_MANAGER")
    private String managerPosition;

    @Schema(description = "Admin level filter", example = "SUPER_ADMIN")
    private String adminLevel;

    @Min(value = 0, message = "Page number must be 0 or greater")
    @Schema(description = "Page number (0-based)", example = "0", defaultValue = "0")
    private Integer page = 0;

    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size cannot exceed 100")
    @Schema(description = "Page size", example = "20", defaultValue = "20")
    private Integer size = 20;

    @Schema(description = "Sort field", example = "lastName", defaultValue = "createdAt")
    private String sortBy = "createdAt";

    @Schema(description = "Sort direction", example = "ASC", defaultValue = "DESC")
    private String sortDirection = "DESC";
}