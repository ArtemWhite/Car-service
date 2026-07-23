package presentation.dtos.request.spareRequestPresentationDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update spare part stock")
public class SparePartStockUpdatePresentationRequest {

    @NotBlank(message = "Spare part ID is required")
    @Schema(description = "Spare part ID", example = "sp_123e4567")
    private String sparePartId;

    @NotNull(message = "New quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    @Schema(description = "New quantity", example = "100")
    private Integer newQuantity;

    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    @Schema(description = "Reason for stock update", example = "New shipment received")
    private String reason;

    @Schema(description = "Warehouse section ID", example = "sect_123")
    private String sectionId;

    @Schema(description = "Storage location", example = "A-12")
    private String location;
}