package dealerShipOrder.presentation.dtos.request.paymentRequestPresentationDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to refund a payment")
public class PaymentRefundPresentationRequest {

    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    @Schema(description = "Reason for refund", example = "Customer requested cancellation")
    private String reason;

    @Positive(message = "Amount must be positive")
    @Schema(description = "Amount to refund (if partial)", example = "1000000.0")
    private Double amount;
}