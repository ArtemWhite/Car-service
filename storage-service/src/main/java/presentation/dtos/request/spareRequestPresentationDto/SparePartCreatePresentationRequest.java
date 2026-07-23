package presentation.dtos.request.spareRequestPresentationDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.*;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new spare part")
public class SparePartCreatePresentationRequest {

    @NotBlank(message = "Spare part type is required")
    @Schema(description = "Spare part type", example = "ENGINE")
    private String spareType;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 200, message = "Name must be between 2 and 200 characters")
    @Schema(description = "Spare part name", example = "Oil Filter")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Schema(description = "Spare part description", example = "Original oil filter for Toyota Camry")
    private String description;

    @NotBlank(message = "Manufacturer is required")
    @Size(min = 2, max = 100, message = "Manufacturer name must be between 2 and 100 characters")
    @Schema(description = "Manufacturer", example = "Toyota")
    private String manufacturer;

    @NotBlank(message = "Part number is required")
    @Pattern(regexp = "^[A-Z0-9\\-]+$", message = "Part number can only contain uppercase letters, numbers and hyphens")
    @Schema(description = "Part number", example = "90915-YZZE1")
    private String partNumber;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    @DecimalMin(value = "0.01", message = "Price must be at least 0.01")
    @Schema(description = "Price", example = "1500.0")
    private Double price;

    @Schema(description = "Currency", example = "RUB", defaultValue = "RUB")
    private String currency = "RUB";

    @Min(value = 0, message = "Quantity cannot be negative")
    @Schema(description = "Initial quantity", example = "50", defaultValue = "0")
    private Integer quantity = 0;

    @Schema(description = "Compatible car model IDs")
    private Set<String> compatibleModelIds;

    @Schema(description = "Warehouse section ID", example = "sect_123")
    private String sectionId;

    @Schema(description = "Storage location", example = "A-12")
    private String location;
}