package dealerShipOrder.application.dtos.request.paymentRequest;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentRequest {

    private String orderId;

    private String clientId;

    private Double amount;

    private String method;

    private String cardNumber;
    private String cardHolderName;
    private String expiryDate;
    private String cvv;
}