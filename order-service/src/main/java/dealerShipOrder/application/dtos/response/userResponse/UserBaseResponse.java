package dealerShipOrder.application.dtos.response.userResponse;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBaseResponse {
    private String id;
    private String firstName;
    private String lastName;
    private String middleName;
    private String fullName;
    private String email;
    private String phone;
    private String userType;
    private String status;
    private String statusDisplayName;
    private LocalDateTime registeredAt;
    private LocalDateTime lastActiveAt;
    private String employeeId;
}
