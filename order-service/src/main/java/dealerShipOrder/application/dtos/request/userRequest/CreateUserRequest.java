package dealerShipOrder.application.dtos.request.userRequest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

    private String firstName;

    private String lastName;

    private String middleName;

    private String email;

    private String phone;

    private String password;

    private String userType;

    private String employeeId;

    private String adminLevel;

    private String position;
    private Set<String> managedSectionIds;
}