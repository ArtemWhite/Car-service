package presentation.dtos.response.spareResponsePresentationDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Spare part response")
public class SparePartPresentationResponse {

    @Schema(description = "Spare part ID", example = "sp_123e4567")
    private String id;

    @Schema(description = "Spare part type code", example = "ENGINE")
    private String spareType;

    @Schema(description = "Spare part type display name", example = "Engine Parts")
    private String spareTypeDisplayName;

    @Schema(description = "Spare part name", example = "Oil Filter")
    private String name;

    @Schema(description = "Description", example = "Original oil filter for Toyota Camry")
    private String description;

    @Schema(description = "Manufacturer", example = "Toyota")
    private String manufacturer;

    @Schema(description = "Part number", example = "90915-YZZE1")
    private String partNumber;

    @Schema(description = "Price", example = "1500.0")
    private Double price;

    @Schema(description = "Formatted price", example = "1,500 ₽")
    private String priceFormatted;

    @Schema(description = "Current quantity", example = "50")
    private Integer quantity;

    @Schema(description = "Stock status code", example = "IN_STOCK")
    private String status;

    @Schema(description = "Stock status display name", example = "In Stock")
    private String statusDisplayName;

    @Schema(description = "Number of compatible models", example = "5")
    private Integer compatibleModelsCount;

    @Schema(description = "Warehouse section ID", example = "sect_123")
    private String sectionId;

    @Schema(description = "Storage location", example = "A-12")
    private String location;

    @Schema(description = "Last updated date")
    private LocalDateTime lastUpdated;

    @Schema(description = "Whether in stock", example = "true")
    private boolean inStock;

    @Schema(description = "Whether low stock", example = "false")
    private boolean lowStock;

    @Schema(description = "Whether out of stock", example = "false")
    private boolean outOfStock;
}