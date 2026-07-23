package dealerShipOrder.application.dtos.request.userRequest;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationHistoryRequest {
    private String id;
    private String operationType;
    private String operationTypeDisplayName;
    private String description;
    private LocalDateTime timestamp;
    private String adminId;
    private String adminName;
    private String itemId;
    private String itemType;
    private Integer quantity;
    private String fromSection;
    private String toSection;
    private String fromLocation;
    private String toLocation;
    private String documentNumber;
}