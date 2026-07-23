package dealerShipOrder.application.dtos.response.userResponse.users;

import dealerShipOrder.application.dtos.response.userResponse.UserBaseResponse;
import lombok.*;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ClientResponse extends UserBaseResponse {
    private String preferredContactMethod;
    private Boolean newsletterSubscribed;
    private Integer orderCount;
    private Integer testDriveCount;
}