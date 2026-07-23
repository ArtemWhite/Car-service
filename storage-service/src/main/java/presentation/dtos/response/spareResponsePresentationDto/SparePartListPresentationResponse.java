package presentation.dtos.response.spareResponsePresentationDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
@Schema(description = "Spare part list response with statistics")
public class SparePartListPresentationResponse {

    @Schema(description = "List of spare parts")
    private List<SparePartPresentationResponse> spareParts;

    @Schema(description = "Total number of spare parts", example = "250")
    private int totalCount;

    @Schema(description = "Number of in stock items", example = "150")
    private int inStockCount;

    @Schema(description = "Number of low stock items", example = "30")
    private int lowStockCount;

    @Schema(description = "Number of out of stock items", example = "70")
    private int outOfStockCount;

    @Schema(description = "Count by spare part type")
    private Map<String, Integer> countByType;
}