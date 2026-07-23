package dealerShipOrder.presentation.dtos.response.paymentResponsePresentationDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "Payment list response with statistics")
public class PaymentListPresentationResponse {

    @Schema(description = "List of payments")
    private List<PaymentPresentationResponse> payments;

    @Schema(description = "Total number of payments", example = "150")
    private int totalCount;

    @Schema(description = "Total amount of all payments", example = "375000000.0")
    private double totalAmount;

    @Schema(description = "Formatted total amount", example = "375,000,000 ₽")
    private String totalAmountFormatted;

    @Schema(description = "Number of completed payments", example = "120")
    private int completedCount;

    @Schema(description = "Number of failed payments", example = "10")
    private int failedCount;

    @Schema(description = "Number of pending payments", example = "20")
    private int pendingCount;
}