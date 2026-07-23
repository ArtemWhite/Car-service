package dealerShipOrder.presentation.dtos.response.userResponsePresentationDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Client response")
public class ClientPresentationResponse extends UserBasePresentationResponse {

    @Schema(description = "Preferred contact method", example = "EMAIL")
    private String preferredContactMethod;

    @Schema(description = "Subscribed to newsletter", example = "true")
    private Boolean newsletterSubscribed;

    @Schema(description = "Number of orders", example = "5")
    private Integer orderCount;

    @Schema(description = "Number of test drives", example = "2")
    private Integer testDriveCount;
}