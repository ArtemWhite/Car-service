package dealerShipOrder.application.dtos.response.orderResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatisticsResponse {

    private int totalOrders;
    private Double totalAmount;
    private Double averageOrderValue;
    private Map<String, Integer> ordersByStatus;
    private Map<String, Integer> ordersByType;
    private Map<String, Integer> dailyOrders;
}