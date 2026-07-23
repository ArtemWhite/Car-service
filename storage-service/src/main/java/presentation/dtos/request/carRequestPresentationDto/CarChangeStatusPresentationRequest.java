package presentation.dtos.request.carRequestPresentationDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to change car status")
public class CarChangeStatusPresentationRequest {

    @NotBlank(message = "Status is required")
    @Schema(description = "New car status", example = "AVAILABLE")
    private String status;
}