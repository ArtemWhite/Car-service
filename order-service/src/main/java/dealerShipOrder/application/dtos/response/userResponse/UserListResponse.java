package dealerShipOrder.application.dtos.response.userResponse;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserListResponse {
    private List<UserBaseResponse> users;
    private int totalCount;
    private int activeCount;
    private int inactiveCount;
    private int blockedCount;
}