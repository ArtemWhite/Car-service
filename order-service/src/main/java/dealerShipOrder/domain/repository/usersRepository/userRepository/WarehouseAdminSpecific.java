package dealerShipOrder.domain.repository.usersRepository.userRepository;

import dealerShipOrder.domain.models.users.warehouseAdmin.WarehouseAdmin;

import java.util.List;
import java.util.Optional;

public interface WarehouseAdminSpecific {
    List<WarehouseAdmin> findAllWarehouseAdmins();
    Optional<WarehouseAdmin> findWarehouseAdminById(String id);
    WarehouseAdmin saveWarehouseAdmin(WarehouseAdmin admin);
    void deleteWarehouseAdmin(String id);
    List<WarehouseAdmin> findOnDutyWarehouseAdmins();
}
