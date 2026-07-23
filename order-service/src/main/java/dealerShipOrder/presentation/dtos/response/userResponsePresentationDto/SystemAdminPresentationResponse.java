package dealerShipOrder.presentation.dtos.response.userResponsePresentationDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Schema(description = "System admin response")
public class SystemAdminPresentationResponse extends UserBasePresentationResponse {

    @Schema(description = "Admin level", example = "SUPER_ADMIN")
    private String adminLevel;

    @Schema(description = "Number of permissions", example = "25")
    private Integer permissionsCount;
}