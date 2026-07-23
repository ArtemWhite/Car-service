package dealerShipOrder.application.dtos.request.userRequest;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFilterRequest {
    private String userType;
    private String status;
    private String email;
    private String phone;
    private String firstName;
    private String lastName;
    private Boolean active;
    private String managerPosition;
    private String adminLevel;
}