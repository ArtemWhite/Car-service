package dealerShipOrder.application.dtos.response.userResponse.users;

import dealerShipOrder.application.dtos.response.userResponse.UserBaseResponse;
import lombok.*;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class SystemAdminResponse extends UserBaseResponse {
    private String adminLevel;
    private Integer permissionsCount;
}
