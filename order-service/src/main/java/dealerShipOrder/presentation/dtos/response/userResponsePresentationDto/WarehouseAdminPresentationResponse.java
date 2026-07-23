package dealerShipOrder.presentation.dtos.response.userResponsePresentationDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Warehouse admin response")
public class WarehouseAdminPresentationResponse extends UserBasePresentationResponse {

    @Schema(description = "Warehouse position", example = "SECTION_LEAD")
    private String warehousePosition;

    @Schema(description = "Managed section IDs")
    private Set<String> managedSectionIds;

    @Schema(description = "Whether on duty", example = "true")
    private Boolean onDuty;
}