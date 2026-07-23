package dealerShipOrder.presentation.dtos.request.orderRequestPresentationDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update an order")
public class OrderUpdatePresentationRequest {

    @Schema(description = "New order status", example = "PAID")
    private String status;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    @Schema(description = "Additional notes", example = "Customer requested white color")
    private String notes;

    @Size(max = 500, message = "Cancel reason cannot exceed 500 characters")
    @Schema(description = "Reason for cancellation", example = "Customer changed mind")
    private String cancelReason;
}