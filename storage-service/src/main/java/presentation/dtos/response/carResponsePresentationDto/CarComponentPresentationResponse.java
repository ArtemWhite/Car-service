package presentation.dtos.response.carResponsePresentationDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Car component response")
public class CarComponentPresentationResponse {

    @Schema(description = "Component ID", example = "comp_123e4567")
    private String id;

    @Schema(description = "Component type code", example = "WHEELS")
    private String type;

    @Schema(description = "Component type display name", example = "Alloy Wheels")
    private String typeDisplayName;

    @Schema(description = "Component name", example = "19-inch Black Alloy Wheels")
    private String name;

    @Schema(description = "Component description", example = "Sport design, 5-spoke")
    private String description;

    @Schema(description = "Component price (formatted)", example = "+50,000 ₽")
    private String price;

    @Schema(description = "Component price value", example = "50000.0")
    private Double priceValue;

    @Schema(description = "Whether this component is selected", example = "true")
    private boolean selected;

    @Schema(description = "Whether this component is compatible", example = "true")
    private boolean compatible;
}