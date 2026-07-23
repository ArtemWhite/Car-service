package dealerShipOrder.presentation.dtos.request.orderRequestPresentationDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new order")
public class OrderCreatePresentationRequest {

    @Schema(description = "Car ID (required for IN_STOCK)", example = "car_123e4567")
    private String carId;

    @Schema(description = "Configuration ID (required for CUSTOM)", example = "cfg_456h789i")
    private String configurationId;

    @Schema(description = "Car model ID (required for CUSTOM)", example = "model_789")
    private String carModelId;

    @NotBlank(message = "Order type is required")
    @Pattern(regexp = "IN_STOCK|CUSTOM", message = "Invalid order type. Allowed values: IN_STOCK, CUSTOM")
    @Schema(description = "Order type", example = "IN_STOCK", allowableValues = {"IN_STOCK", "CUSTOM"})
    private String orderType;

    @Schema(description = "Additional notes", example = "Please deliver after 6 PM")
    private String notes;
}