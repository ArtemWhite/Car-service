package dealerShipOrder.presentation.dtos.response.testDriveResponsePresentationDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Test drive response")
public class TestDrivePresentationResponse {

    @Schema(description = "Test drive ID", example = "td_123e4567")
    private String id;

    @Schema(description = "Client ID", example = "user_123")
    private String clientId;

    @Schema(description = "Client name", example = "John Doe")
    private String clientName;

    @Schema(description = "Car ID", example = "car_789")
    private String carId;

    @Schema(description = "Car information", example = "Toyota Camry 2024, Black")
    private String carInfo;

    @Schema(description = "Manager ID", example = "manager_456")
    private String managerId;

    @Schema(description = "Manager name", example = "Jane Smith")
    private String managerName;

    @Schema(description = "Requested test drive time", example = "2025-05-15T14:00:00")
    private LocalDateTime requestedTime;

    @Schema(description = "Confirmed time (if confirmed)", example = "2025-05-16T10:00:00")
    private LocalDateTime confirmedTime;

    @Schema(description = "Test drive status code", example = "PENDING")
    private String status;

    @Schema(description = "Test drive status display name", example = "Pending")
    private String statusDisplayName;

    @Schema(description = "Additional notes", example = "Client wants to test highway driving")
    private String notes;

    @Schema(description = "Whether test drive is upcoming", example = "true")
    private boolean upcoming;

    @Schema(description = "Whether test drive is past", example = "false")
    private boolean past;

    @Schema(description = "Whether client can cancel", example = "true")
    private boolean canCancel;

    @Schema(description = "Whether client can reschedule", example = "true")
    private boolean canReschedule;
}