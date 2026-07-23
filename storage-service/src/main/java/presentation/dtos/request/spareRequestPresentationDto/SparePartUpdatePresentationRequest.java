package presentation.dtos.request.spareRequestPresentationDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update a spare part")
public class SparePartUpdatePresentationRequest {

    @Size(min = 2, max = 200, message = "Name must be between 2 and 200 characters")
    @Schema(description = "Spare part name", example = "Oil Filter Premium")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Schema(description = "Spare part description", example = "Premium oil filter for Toyota Camry")
    private String description;

    @Size(min = 2, max = 100, message = "Manufacturer name must be between 2 and 100 characters")
    @Schema(description = "Manufacturer", example = "Toyota")
    private String manufacturer;

    @Pattern(regexp = "^[A-Z0-9\\-]+$", message = "Part number can only contain uppercase letters, numbers and hyphens")
    @Schema(description = "Part number", example = "90915-YZZE2")
    private String partNumber;

    @Positive(message = "Price must be positive")
    @DecimalMin(value = "0.01", message = "Price must be at least 0.01")
    @Schema(description = "Price", example = "1600.0")
    private Double price;

    @Schema(description = "Spare part type", example = "ENGINE")
    private String spareType;

    @Schema(description = "Compatible car model IDs")
    private Set<String> compatibleModelIds;

    @Size(max = 500, message = "Update reason cannot exceed 500 characters")
    @Schema(description = "Reason for update", example = "Price adjustment")
    private String updateReason;
}