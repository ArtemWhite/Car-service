package dealerShipOrder.application.dtos.response.testDriveResponse;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestDriveResponse {
    private String id;
    private String clientId;
    private String clientName;
    private String carId;
    private String carInfo;
    private String managerId;
    private String managerName;
    private LocalDateTime requestedTime;
    private LocalDateTime confirmedTime;
    private String status;
    private String statusDisplayName;
    private String notes;
    private boolean upcoming;
    private boolean past;
    private boolean canCancel;
    private boolean canReschedule;
}