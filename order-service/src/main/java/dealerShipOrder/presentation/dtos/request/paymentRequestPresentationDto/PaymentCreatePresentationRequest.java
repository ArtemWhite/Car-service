package dealerShipOrder.presentation.dtos.request.paymentRequestPresentationDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a payment")
public class PaymentCreatePresentationRequest {

    @NotBlank(message = "Order ID is required")
    @Schema(description = "Order ID", example = "ord_123e4567")
    private String orderId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    @Schema(description = "Payment amount", example = "2500000.0")
    private Double amount;

    @NotBlank(message = "Payment method is required")
    @Schema(description = "Payment method", example = "CARD", allowableValues = {"CARD", "CASH", "BANK_TRANSFER", "CREDIT"})
    private String method;

    @Pattern(regexp = "^[0-9]{16}$", message = "Card number must be 16 digits")
    @Schema(description = "Card number (for card payments)", example = "4111111111111111")
    private String cardNumber;

    @Size(min = 2, max = 100, message = "Card holder name must be between 2 and 100 characters")
    @Schema(description = "Card holder name", example = "JOHN DOE")
    private String cardHolderName;

    @Pattern(regexp = "^(0[1-9]|1[0-2])/[0-9]{2}$", message = "Expiry date must be in format MM/YY")
    @Schema(description = "Card expiry date (MM/YY)", example = "12/25")
    private String expiryDate;

    @Pattern(regexp = "^[0-9]{3,4}$", message = "CVV must be 3 or 4 digits")
    @Schema(description = "Card CVV", example = "123")
    private String cvv;
}