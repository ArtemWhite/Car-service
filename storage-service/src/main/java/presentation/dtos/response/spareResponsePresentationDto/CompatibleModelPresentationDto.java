package presentation.dtos.response.spareResponsePresentationDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Compatible car model information")
public class CompatibleModelPresentationDto {

    @Schema(description = "Car model ID", example = "model_123")
    private String modelId;

    @Schema(description = "Car model name", example = "Camry")
    private String modelName;

    @Schema(description = "Brand name", example = "Toyota")
    private String brandName;

    @Schema(description = "Whether compatible", example = "true")
    private boolean compatible;
}