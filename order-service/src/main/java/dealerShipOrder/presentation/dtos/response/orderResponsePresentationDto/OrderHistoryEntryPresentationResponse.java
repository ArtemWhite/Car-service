package dealerShipOrder.presentation.dtos.response.orderResponsePresentationDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Order history entry response")
public class OrderHistoryEntryPresentationResponse {

    @Schema(description = "Action type", example = "STATUS_CHANGED")
    private String action;

    @Schema(description = "Action description", example = "Order status changed from CREATED to PAID")
    private String description;

    @Schema(description = "Timestamp of the action", example = "2024-01-15T10:30:00")
    private LocalDateTime timestamp;
}