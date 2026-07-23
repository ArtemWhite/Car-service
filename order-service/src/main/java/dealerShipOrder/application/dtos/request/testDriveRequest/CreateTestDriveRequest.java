package dealerShipOrder.application.dtos.request.testDriveRequest;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTestDriveRequest {

    private String clientId;

    private String carId;

    private LocalDateTime startTime;

    private String notes;
}