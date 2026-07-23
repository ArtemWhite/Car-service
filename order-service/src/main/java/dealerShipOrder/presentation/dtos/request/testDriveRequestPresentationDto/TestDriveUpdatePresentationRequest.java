package dealerShipOrder.presentation.dtos.request.testDriveRequestPresentationDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.Future;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update a test drive")
public class TestDriveUpdatePresentationRequest {

    @Future(message = "Start time must be in the future")
    @Schema(description = "New start time", example = "2025-05-16T15:00:00")
    private LocalDateTime startTime;

    @Schema(description = "New status", example = "CONFIRMED")
    private String status;

    @Schema(description = "Manager ID to assign", example = "manager_456")
    private String managerId;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    @Schema(description = "Additional notes", example = "Client prefers automatic transmission")
    private String notes;

    @Size(max = 500, message = "Cancel reason cannot exceed 500 characters")
    @Schema(description = "Reason for cancellation", example = "Client cancelled due to schedule conflict")
    private String cancelReason;
}