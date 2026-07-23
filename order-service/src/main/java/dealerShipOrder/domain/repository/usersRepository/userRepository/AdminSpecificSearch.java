package dealerShipOrder.domain.repository.usersRepository.userRepository;

import dealerShipOrder.domain.models.users.User;
import dealerShipOrder.domain.models.users.systemAdmin.AdminLevel;

import java.util.List;

public interface AdminSpecificSearch {
    List<User> findAdminsByLevel(AdminLevel level);
    List<User> findSystemAdminsWithPermission(String permission);
    List<User> findWarehouseAdminsBySection(String sectionId);
}
