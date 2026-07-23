package dealerShipOrder.application.dtos.response.testDriveResponse;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestDriveListResponse {
    private List<TestDriveResponse> testDrives;
    private int totalCount;
    private int pendingCount;
    private int confirmedCount;
    private int completedCount;
    private int cancelledCount;
}