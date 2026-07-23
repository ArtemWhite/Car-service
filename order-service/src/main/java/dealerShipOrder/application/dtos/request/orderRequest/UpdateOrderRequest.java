package dealerShipOrder.application.dtos.request.orderRequest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateOrderRequest
{
    private String userId;
    private String status;
    private String notes;
    private String cancelReason;
}
