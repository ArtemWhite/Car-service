package presentation.dtos.response.spareResponsePresentationDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "Spare part compatibility response")
public class SparePartCompatibilityPresentationResponse {

    @Schema(description = "Spare part ID", example = "sp_123e4567")
    private String sparePartId;

    @Schema(description = "Spare part name", example = "Oil Filter")
    private String sparePartName;

    @Schema(description = "List of compatible models")
    private List<CompatibleModelPresentationDto> compatibleModels;
}