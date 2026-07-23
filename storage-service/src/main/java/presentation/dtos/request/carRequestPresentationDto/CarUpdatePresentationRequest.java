package presentation.dtos.request.carRequestPresentationDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update a car")
public class CarUpdatePresentationRequest {

    @Positive(message = "Price must be positive")
    @Schema(description = "New car price", example = "2600000.0")
    private Double price;

    @Schema(description = "New car status", example = "AVAILABLE")
    private String status;

    @Schema(description = "New configuration ID", example = "cfg_456h789i")
    private String configurationId;

    @Size(max = 500, message = "Update reason cannot exceed 500 characters")
    @Schema(description = "Reason for the update", example = "Price adjustment for spring sale")
    private String updateReason;
}