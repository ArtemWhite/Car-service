package dealerShipOrder.presentation.dtos.response.userResponsePresentationDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "On duty status response")
public class OnDutyPresentationResponse {

    @Schema(description = "Whether on duty", example = "true")
    private boolean onDuty;
}