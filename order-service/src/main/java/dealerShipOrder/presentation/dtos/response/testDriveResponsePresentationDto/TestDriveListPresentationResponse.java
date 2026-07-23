package dealerShipOrder.presentation.dtos.response.testDriveResponsePresentationDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "Test drive list response with statistics")
public class TestDriveListPresentationResponse {

    @Schema(description = "List of test drives")
    private List<TestDrivePresentationResponse> testDrives;

    @Schema(description = "Total number of test drives", example = "150")
    private int totalCount;

    @Schema(description = "Number of pending test drives", example = "45")
    private int pendingCount;

    @Schema(description = "Number of confirmed test drives", example = "60")
    private int confirmedCount;

    @Schema(description = "Number of completed test drives", example = "30")
    private int completedCount;

    @Schema(description = "Number of cancelled test drives", example = "15")
    private int cancelledCount;
}