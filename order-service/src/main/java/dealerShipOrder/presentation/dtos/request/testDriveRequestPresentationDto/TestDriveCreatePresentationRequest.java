package dealerShipOrder.presentation.dtos.request.testDriveRequestPresentationDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a test drive request")
public class TestDriveCreatePresentationRequest {

    @NotBlank(message = "Car ID is required")
    @Schema(description = "Car ID", example = "car_123e4567")
    private String carId;

    @NotNull(message = "Start time is required")
    @Future(message = "Test drive time must be in the future")
    @Schema(description = "Requested test drive start time", example = "2025-05-15T14:00:00")
    private LocalDateTime startTime;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    @Schema(description = "Additional notes", example = "Client wants to test highway driving")
    private String notes;
}