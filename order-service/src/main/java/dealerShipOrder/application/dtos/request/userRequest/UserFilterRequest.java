package dealerShipOrder.application.dtos.request.userRequest;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    private String query;
    private Boolean available;
    private String sectionId;
    private Boolean newsletterSubscribed;
    private Integer page;
    private Integer size;
}