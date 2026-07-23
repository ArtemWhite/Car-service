package dealerShipOrder.application.dtos.request.orderRequest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderRequest
{
    private String clientId;

    private String carId;

    private String configurationId;
    private String carModelId;

    private String orderType;

    private String notes;
}
