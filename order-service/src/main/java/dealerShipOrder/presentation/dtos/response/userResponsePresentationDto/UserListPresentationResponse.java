package dealerShipOrder.presentation.dtos.response.userResponsePresentationDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "User list response with statistics")
public class UserListPresentationResponse {

    @Schema(description = "List of users")
    private List<UserBasePresentationResponse> users;

    @Schema(description = "Total number of users", example = "250")
    private int totalCount;

    @Schema(description = "Number of active users", example = "180")
    private int activeCount;

    @Schema(description = "Number of inactive users", example = "50")
    private int inactiveCount;

    @Schema(description = "Number of blocked users", example = "20")
    private int blockedCount;
}