package dealerShipOrder.presentation.dtos.response.orderResponsePresentationDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Schema(description = "Order response")
public class OrderPresentationResponse {

    @Schema(description = "Order ID", example = "ord_123e4567")
    private String id;

    @Schema(description = "Client ID", example = "user_123")
    private String clientId;

    @Schema(description = "Client name", example = "John Doe")
    private String clientName;

    @Schema(description = "Manager ID", example = "manager_456")
    private String managerId;

    @Schema(description = "Manager name", example = "Jane Smith")
    private String managerName;

    @Schema(description = "Order type code", example = "IN_STOCK")
    private String orderType;

    @Schema(description = "Order type display name", example = "In Stock")
    private String orderTypeDisplayName;

    @Schema(description = "Order status code", example = "PAID")
    private String status;

    @Schema(description = "Order status display name", example = "Paid")
    private String statusDisplayName;

    @Schema(description = "Created date", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Updated date", example = "2024-01-16T14:20:00")
    private LocalDateTime updatedAt;

    @Schema(description = "Completed date", example = "2024-01-20T16:00:00")
    private LocalDateTime completedAt;

    @Schema(description = "Car ID", example = "car_789")
    private String carId;

    @Schema(description = "Car information", example = "Toyota Camry 2024, Black")
    private String carInfo;

    @Schema(description = "Configuration ID", example = "cfg_456")
    private String configurationId;

    @Schema(description = "Configuration name", example = "Luxury Package")
    private String configurationName;

    @Schema(description = "Car model ID", example = "model_123")
    private String carModelId;

    @Schema(description = "Car model name", example = "Camry XV70")
    private String carModelName;

    @Schema(description = "Order history")
    private List<OrderHistoryEntryPresentationResponse> history;

    @Schema(description = "Additional notes", example = "Please deliver after 6 PM")
    private String notes;

    @Schema(description = "Whether order is active", example = "true")
    private boolean active;
}