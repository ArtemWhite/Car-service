package dealerShipOrder.application.dtos.response.userResponse.users;

import dealerShipOrder.application.dtos.response.userResponse.UserBaseResponse;
import lombok.*;

import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseAdminResponse extends UserBaseResponse {
    private String warehousePosition;
    private Set<String> managedSectionIds;
    private Boolean onDuty;
}
