package dealerShipOrder.presentation.dtos.request.testDriveRequestPresentationDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to filter test drives")
public class TestDriveFilterPresentationRequest {

    @Schema(description = "Client ID", example = "user_123")
    private String clientId;

    @Schema(description = "Car ID", example = "car_123")
    private String carId;

    @Schema(description = "Manager ID", example = "manager_456")
    private String managerId;

    @Schema(description = "Test drive status", example = "PENDING")
    private String status;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(description = "Date from", example = "2025-05-01T00:00:00")
    private LocalDateTime dateFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(description = "Date to", example = "2025-05-31T23:59:59")
    private LocalDateTime dateTo;

    @Schema(description = "Only upcoming test drives", example = "true")
    private Boolean upcoming;

    @Schema(description = "Only past test drives", example = "false")
    private Boolean past;

    @Min(value = 0, message = "Page number must be 0 or greater")
    @Schema(description = "Page number (0-based)", example = "0", defaultValue = "0")
    private Integer page = 0;

    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size cannot exceed 100")
    @Schema(description = "Page size", example = "20", defaultValue = "20")
    private Integer size = 20;

    @Schema(description = "Sort field", example = "startTime", defaultValue = "createdAt")
    private String sortBy = "createdAt";

    @Schema(description = "Sort direction", example = "DESC", defaultValue = "DESC")
    private String sortDirection = "DESC";
}