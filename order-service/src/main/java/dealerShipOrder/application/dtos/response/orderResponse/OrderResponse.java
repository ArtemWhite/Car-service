package dealerShipOrder.application.dtos.response.orderResponse;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse
{
    private String id;
    private String clientId;
    private String clientName;
    private String managerId;
    private String managerName;
    private String orderType;
    private String orderTypeDisplayName;
    private String status;
    private String statusDisplayName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;

    private String carId;
    private String carInfo;

    private String configurationId;
    private String configurationName;
    private String carModelId;
    private String carModelName;

    private List<OrderHistoryEntryDto> history;
    private String notes;
    private boolean active;

    private Double totalAmount;
    private String totalAmountFormatted;
}
