package dealerShipOrder.application.dtos.request.paymentRequest;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessPaymentRequest {
    private String transactionId;
    private String paymentDetails;
    private boolean success;
}