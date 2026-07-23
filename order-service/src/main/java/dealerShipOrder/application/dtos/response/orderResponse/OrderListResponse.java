package dealerShipOrder.application.dtos.response.orderResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderListResponse {

    private List<OrderResponse> orders;
    private int totalCount;
    private int pendingCount;
    private int paidCount;
    private int completedCount;
    private int cancelledCount;
    private Double totalAmount;
}