package dealerShipOrder.presentation.dtos.response.userResponsePresentationDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Manager response")
public class ManagerPresentationResponse extends UserBasePresentationResponse {

    @Schema(description = "Position code", example = "SALES_MANAGER")
    private String position;

    @Schema(description = "Position display name", example = "Sales Manager")
    private String positionDisplayName;

    @Schema(description = "Number of assigned orders", example = "15")
    private Integer assignedOrdersCount;

    @Schema(description = "Number of managed test drives", example = "8")
    private Integer managedTestDrivesCount;

    @Schema(description = "Whether manager is available", example = "true")
    private Boolean available;
}