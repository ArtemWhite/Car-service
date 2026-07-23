package dealerShipOrder.presentation.dtos.request.paymentRequestPresentationDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to process a payment")
public class PaymentProcessPresentationRequest {

    @NotBlank(message = "Transaction ID is required")
    @Schema(description = "Transaction ID from payment gateway", example = "txn_123e4567")
    private String transactionId;

    @Schema(description = "Additional payment details", example = "Payment via Visa")
    private String paymentDetails;

    @NotNull(message = "Success status is required")
    @Schema(description = "Whether payment was successful", example = "true")
    private Boolean success;
}