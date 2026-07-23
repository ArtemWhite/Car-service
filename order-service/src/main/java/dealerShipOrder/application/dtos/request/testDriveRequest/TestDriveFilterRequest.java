package dealerShipOrder.application.dtos.request.testDriveRequest;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestDriveFilterRequest {
    private String clientId;
    private String carId;
    private String managerId;
    private String status;
    private LocalDateTime dateFrom;
    private LocalDateTime dateTo;
    private Boolean upcoming;
    private Boolean past;

    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "requestedTime";
    private String sortDirection = "DESC";
}