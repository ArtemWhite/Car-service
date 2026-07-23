package presentation.dtos.request.spareRequestPresentationDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to filter spare parts")
public class SparePartFilterPresentationRequest {

    @Schema(description = "Spare part type", example = "ENGINE")
    private String spareType;

    @Schema(description = "Manufacturer", example = "Toyota")
    private String manufacturer;

    @Schema(description = "Minimum price", example = "100.0")
    private Double minPrice;

    @Schema(description = "Maximum price", example = "10000.0")
    private Double maxPrice;

    @Schema(description = "Compatible car model ID", example = "model_123")
    private String compatibleModelId;

    @Schema(description = "Only in stock items", example = "true")
    private Boolean inStock;

    @Schema(description = "Only low stock items", example = "false")
    private Boolean lowStock;

    @Schema(description = "Search query (name, part number)", example = "oil filter")
    private String searchQuery;

    @Min(value = 0, message = "Page number must be 0 or greater")
    @Schema(description = "Page number (0-based)", example = "0", defaultValue = "0")
    private Integer page = 0;

    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size cannot exceed 100")
    @Schema(description = "Page size", example = "20", defaultValue = "20")
    private Integer size = 20;

    @Schema(description = "Sort field", example = "name", defaultValue = "createdAt")
    private String sortBy = "createdAt";

    @Schema(description = "Sort direction", example = "ASC", defaultValue = "DESC")
    private String sortDirection = "DESC";
}