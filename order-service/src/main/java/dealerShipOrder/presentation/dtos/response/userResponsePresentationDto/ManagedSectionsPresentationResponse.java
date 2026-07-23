package dealerShipOrder.presentation.dtos.response.userResponsePresentationDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
@Schema(description = "Managed sections response")
public class ManagedSectionsPresentationResponse {

    @Schema(description = "Managed section IDs")
    private Set<String> sectionIds;

    @Schema(description = "Count of sections", example = "3")
    private int count;
}