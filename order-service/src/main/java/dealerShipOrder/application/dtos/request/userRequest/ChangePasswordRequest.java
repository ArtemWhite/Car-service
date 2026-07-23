package dealerShipOrder.application.dtos.request.userRequest;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {
    private String userId;
    private String oldPassword;
    private String newPassword;
}