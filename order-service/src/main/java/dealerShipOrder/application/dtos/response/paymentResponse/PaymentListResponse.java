package dealerShipOrder.application.dtos.response.paymentResponse;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentListResponse {
    private List<PaymentResponse> payments;
    private int totalCount;
    private double totalAmount;
    private String totalAmountFormatted;
    private int completedCount;
    private int failedCount;
    private int pendingCount;
}