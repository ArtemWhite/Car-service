package presentation.dtos.response.carResponsePresentationDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "Car list response with statistics")
public class CarListPresentationResponse {

    @Schema(description = "List of cars")
    private List<CarPresentationResponse> cars;

    @Schema(description = "Total number of cars", example = "150")
    private Integer totalCount;

    @Schema(description = "Number of available cars", example = "45")
    private Integer availableCount;

    @Schema(description = "Number of cars available for test drive", example = "30")
    private Integer testDriveCount;

    @Schema(description = "Number of cars in stock", example = "60")
    private Integer inStockCount;

    @Schema(description = "Number of reserved cars", example = "25")
    private Integer reservedCount;

    @Schema(description = "Number of sold cars", example = "65")
    private Integer soldCount;

    @Schema(description = "Minimum price", example = "800000.0")
    private Double minPrice;

    @Schema(description = "Maximum price", example = "5000000.0")
    private Double maxPrice;

    @Schema(description = "Average price", example = "2200000.0")
    private Double avgPrice;
}