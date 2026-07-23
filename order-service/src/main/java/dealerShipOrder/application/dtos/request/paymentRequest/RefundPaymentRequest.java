package dealerShipOrder.application.dtos.request.paymentRequest;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefundPaymentRequest {

    private String reason;

    private Double amount;
}