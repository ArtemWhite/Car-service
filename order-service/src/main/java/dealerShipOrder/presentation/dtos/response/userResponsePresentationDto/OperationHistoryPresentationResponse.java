package dealerShipOrder.presentation.dtos.response.userResponsePresentationDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Operation history response")
public class OperationHistoryPresentationResponse {

    @Schema(description = "Operation ID", example = "op_123")
    private String id;

    @Schema(description = "Operation type", example = "UPDATE_STOCK")
    private String operationType;

    @Schema(description = "Operation type display name", example = "Update Stock")
    private String operationTypeDisplayName;

    @Schema(description = "Operation description", example = "Updated stock from 10 to 20")
    private String description;

    @Schema(description = "Timestamp")
    private LocalDateTime timestamp;

    @Schema(description = "Admin ID", example = "admin_123")
    private String adminId;

    @Schema(description = "Admin name", example = "John Doe")
    private String adminName;

    @Schema(description = "Item ID", example = "sp_123")
    private String itemId;

    @Schema(description = "Item type", example = "SPARE_PART")
    private String itemType;

    @Schema(description = "Quantity", example = "10")
    private Integer quantity;

    @Schema(description = "From section", example = "A-01")
    private String fromSection;

    @Schema(description = "To section", example = "B-02")
    private String toSection;

    @Schema(description = "From location", example = "A-12")
    private String fromLocation;

    @Schema(description = "To location", example = "B-34")
    private String toLocation;

    @Schema(description = "Document number", example = "DOC-001")
    private String documentNumber;
}