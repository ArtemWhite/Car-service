package dealerShipOrder.application.dtos.request.orderRequest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderFilterRequest
{
    private String clientId;
    private String managerId;
    private String status;
    private String orderType;
    private LocalDateTime dateFrom;
    private LocalDateTime dateTo;
    
    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}