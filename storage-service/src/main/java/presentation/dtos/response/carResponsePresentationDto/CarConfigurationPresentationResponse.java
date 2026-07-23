package presentation.dtos.response.carResponsePresentationDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "Car configuration response")
public class CarConfigurationPresentationResponse {

    @Schema(description = "Configuration ID", example = "cfg_123e4567")
    private String id;

    @Schema(description = "Configuration name", example = "Luxury Plus")
    private String name;

    @Schema(description = "Model name", example = "Toyota Camry")
    private String modelName;

    @Schema(description = "Base price (formatted)", example = "2,200,000 ₽")
    private String basePrice;

    @Schema(description = "Base price value", example = "2200000.0")
    private Double basePriceValue;

    @Schema(description = "Total price (formatted)", example = "2,500,000 ₽")
    private String totalPrice;

    @Schema(description = "Total price value", example = "2500000.0")
    private Double totalPriceValue;

    @Schema(description = "Base components included by default")
    private List<CarComponentPresentationResponse> baseComponents;

    @Schema(description = "Available components that can be added")
    private List<CarComponentPresentationResponse> availableComponents;

    @Schema(description = "Selected components for current car")
    private List<CarComponentPresentationResponse> selectedComponents;
}