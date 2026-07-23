package dealerShipOrder.application.dtos.request.userRequest;

import lombok.*;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    private String userId;
    private String firstName;
    private String lastName;
    private String middleName;

    private String email;

    private String phone;

    private String status;

    private String position;
    private Boolean available;

    private String warehousePosition;
    private Set<String> managedSectionIds;

    private String preferredContactMethod;
    private Boolean newsletterSubscribed;
}