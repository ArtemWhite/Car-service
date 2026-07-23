package presentation.dtos.request.carRequestPresentationDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to apply configuration to a car")
public class CarApplyConfigurationPresentationRequest {

    @NotBlank(message = "Car ID is required")
    @Schema(description = "Car ID", example = "car_123e4567")
    private String carId;

    @NotBlank(message = "Configuration ID is required")
    @Schema(description = "Configuration ID", example = "cfg_456h789i")
    private String configurationId;

    @Schema(description = "Selected components (component type -> component ID)")
    private Map<String, String> selectedComponents;
}