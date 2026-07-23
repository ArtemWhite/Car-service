package dealerShipOrder.presentation.dtos.request.orderRequestPresentationDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to filter orders with pagination")
public class OrderFilterPresentationRequest {

    @Schema(description = "Client ID", example = "user_123")
    private String clientId;

    @Schema(description = "Manager ID", example = "manager_456")
    private String managerId;

    @Schema(description = "Order status", example = "PAID")
    private String status;

    @Schema(description = "Order type", example = "IN_STOCK")
    private String orderType;

    @Schema(description = "Date from", example = "2024-01-01T00:00:00")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime dateFrom;

    @Schema(description = "Date to", example = "2024-12-31T23:59:59")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime dateTo;

    @Min(value = 0, message = "Page number must be 0 or greater")
    @Schema(description = "Page number (0-based)", example = "0", defaultValue = "0")
    private Integer page = 0;

    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size cannot exceed 100")
    @Schema(description = "Page size", example = "20", defaultValue = "20")
    private Integer size = 20;

    @Schema(description = "Sort field", example = "createdAt", defaultValue = "createdAt")
    private String sortBy = "createdAt";

    @Schema(description = "Sort direction", example = "DESC", defaultValue = "DESC")
    private String sortDirection = "DESC";
}