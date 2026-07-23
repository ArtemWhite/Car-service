package presentation.dtos.request.carRequestPresentationDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to send test drive request")
public class CarSendTestDrivePresentationRequest {

    @NotNull(message = "Requested time is required")
    @Future(message = "Test drive time must be in the future")
    @Schema(description = "Requested test drive time", example = "2025-05-15T14:00:00")
    private LocalDateTime requestedTime;
}