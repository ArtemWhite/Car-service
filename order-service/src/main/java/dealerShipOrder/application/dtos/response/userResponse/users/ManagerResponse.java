package dealerShipOrder.application.dtos.response.userResponse.users;

import dealerShipOrder.application.dtos.response.userResponse.UserBaseResponse;
import lombok.*;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ManagerResponse extends UserBaseResponse {
    private String position;
    private String positionDisplayName;
    private Integer assignedOrdersCount;
    private Integer managedTestDrivesCount;
    private Boolean available;
}