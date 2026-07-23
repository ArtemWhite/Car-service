package dealerShipOrder.presentation.dtos.response.orderResponsePresentationDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "Order list response with statistics")
public class OrderListPresentationResponse {

    @Schema(description = "List of orders")
    private List<OrderPresentationResponse> orders;

    @Schema(description = "Total number of orders", example = "150")
    private int totalCount;

    @Schema(description = "Number of pending orders", example = "45")
    private int pendingCount;

    @Schema(description = "Number of paid orders", example = "60")
    private int paidCount;

    @Schema(description = "Number of completed orders", example = "30")
    private int completedCount;

    @Schema(description = "Number of cancelled orders", example = "15")
    private int cancelledCount;

    @Schema(description = "Total amount of all orders", example = "375000000.0")
    private Double totalAmount;
}