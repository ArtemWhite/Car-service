package dealerShipOrder.domain.repository.usersRepository.userRepository;

import dealerShipOrder.domain.models.users.User;
import dealerShipOrder.domain.repository.BaseRepository;

public interface UserRepository extends
        BaseRepository<User>,
        UserAuthSearch,
        UserStatusSearch,
        UserRoleSearch,
        UserNameSearch,
        UserDateSearch,
        ClientSpecificSearch,
        ManagerSpecificSearch,
        AdminSpecificSearch,
        WarehouseAdminSpecific,
        StockOperationSpecific{
}
