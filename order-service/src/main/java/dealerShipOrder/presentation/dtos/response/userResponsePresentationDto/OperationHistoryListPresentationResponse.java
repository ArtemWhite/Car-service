package dealerShipOrder.presentation.dtos.response.userResponsePresentationDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "Operation history list response")
public class OperationHistoryListPresentationResponse {

    @Schema(description = "List of operations")
    private List<OperationHistoryPresentationResponse> operations;

    @Schema(description = "Total count", example = "50")
    private int totalCount;
}