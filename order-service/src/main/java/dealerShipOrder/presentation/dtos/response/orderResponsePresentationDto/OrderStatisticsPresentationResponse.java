package dealerShipOrder.presentation.dtos.response.orderResponsePresentationDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
@Schema(description = "Order statistics response")
public class OrderStatisticsPresentationResponse {

    @Schema(description = "Total number of orders", example = "150")
    private int totalOrders;

    @Schema(description = "Total amount", example = "375000000.0")
    private Double totalAmount;

    @Schema(description = "Average order value", example = "2500000.0")
    private Double averageOrderValue;

    @Schema(description = "Orders count by status")
    private Map<String, Integer> ordersByStatus;

    @Schema(description = "Orders count by type")
    private Map<String, Integer> ordersByType;

    @Schema(description = "Daily orders for last 30 days")
    private Map<String, Integer> dailyOrders;
}