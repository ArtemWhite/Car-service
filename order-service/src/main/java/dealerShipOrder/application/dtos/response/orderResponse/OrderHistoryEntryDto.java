package dealerShipOrder.application.dtos.response.orderResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderHistoryEntryDto
{
    private String action;
    private String description;
    private LocalDateTime timestamp;
}
