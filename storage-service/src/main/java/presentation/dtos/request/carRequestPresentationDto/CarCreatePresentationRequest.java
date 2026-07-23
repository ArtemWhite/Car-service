package presentation.dtos.request.carRequestPresentationDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new car")
public class CarCreatePresentationRequest {

    @NotBlank(message = "Brand is required")
    @Size(min = 2, max = 50, message = "Brand name must be between 2 and 50 characters")
    @Schema(description = "Car brand", example = "Toyota")
    private String brand;

    @NotBlank(message = "Model is required")
    @Size(min = 2, max = 100, message = "Model name must be between 2 and 100 characters")
    @Schema(description = "Car model", example = "Camry")
    private String model;

    @NotBlank(message = "Body type is required")
    @Schema(description = "Body type", example = "SEDAN")
    private String bodyType;

    @NotBlank(message = "Color is required")
    @Schema(description = "Color", example = "BLACK")
    private String color;

    @NotBlank(message = "Drive type is required")
    @Schema(description = "Drive type", example = "FWD")
    private String driveType;

    @NotBlank(message = "Engine fuel type is required")
    @Schema(description = "Engine fuel type", example = "PETROL")
    private String engineFuelType;

    @NotNull(message = "Engine power is required")
    @Positive(message = "Engine power must be positive")
    @DecimalMin(value = "0.1", message = "Engine power must be at least 0.1 HP")
    @Schema(description = "Engine power (HP)", example = "249.0")
    private Double enginePower;

    @NotNull(message = "Engine displacement is required")
    @Positive(message = "Engine displacement must be positive")
    @DecimalMin(value = "0.1", message = "Engine displacement must be at least 0.1 L")
    @Schema(description = "Engine displacement (L)", example = "2.5")
    private Double engineDisplacement;

    @NotNull(message = "Transmission gears count is required")
    @Min(value = 1, message = "Transmission must have at least 1 gear")
    @Max(value = 12, message = "Transmission cannot have more than 12 gears")
    @Schema(description = "Number of transmission gears", example = "8")
    private Integer transmissionGears;

    @NotBlank(message = "Transmission type is required")
    @Schema(description = "Transmission type", example = "AUTOMATIC")
    private String transmissionType;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    @DecimalMin(value = "0.01", message = "Price must be at least 0.01")
    @Schema(description = "Car price", example = "2500000.0")
    private Double price;

    @Schema(description = "Configuration ID (optional)", example = "cfg_123e4567")
    private String configurationId;
}