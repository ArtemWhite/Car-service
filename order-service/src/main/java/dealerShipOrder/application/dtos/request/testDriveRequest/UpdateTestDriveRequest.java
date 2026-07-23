package dealerShipOrder.application.dtos.request.testDriveRequest;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTestDriveRequest {

    private LocalDateTime startTime;
    private String status;
    private String managerId;
    private String notes;
    private String cancelReason;
}