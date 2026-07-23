package dealerShipOrder.infrastructure.adapters.userAdapters;

import dealerShipOrder.domain.models.users.*;
import dealerShipOrder.domain.models.users.manager.Position;
import dealerShipOrder.domain.models.users.systemAdmin.AdminLevel;
import dealerShipOrder.domain.models.users.warehouseAdmin.StockOperation;
import dealerShipOrder.domain.models.users.warehouseAdmin.WarehouseAdmin;
import dealerShipOrder.domain.repository.usersRepository.userRepository.UserRepository;
import dealerShipOrder.infrastructure.adapters.userAdapters.userReferencesAdapters.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserBaseRepositoryAdapter baseAdapter;
    private final UserAuthAdapter authAdapter;
    private final UserStatusAdapter statusAdapter;
    private final UserRoleAdapter roleAdapter;
    private final UserNameAdapter nameAdapter;
    private final UserDateAdapter dateAdapter;
    private final ClientSpecificAdapter clientSpecificAdapter;
    private final ManagerSpecificAdapter managerSpecificAdapter;
    private final AdminSpecificAdapter adminSpecificAdapter;
    private final WarehouseAdminSpecificAdapter warehouseAdminAdapter;
    private final StockOperationAdapter stockOperationAdapter;

    @Override
    public User save(User user) { return baseAdapter.save(user); }
    @Override
    public Optional<User> findById(String id) { return baseAdapter.findById(id); }
    @Override
    public List<User> findAll() { return baseAdapter.findAll(); }
    @Override
    public void delete(String id) { baseAdapter.delete(id); }
    @Override
    public boolean existsById(String id) { return baseAdapter.existsById(id); }

    @Override
    public Optional<User> findByEmail(String email) { return authAdapter.findByEmail(email); }
    @Override
    public boolean existsByEmail(String email) { return authAdapter.existsByEmail(email); }
    @Override
    public Optional<User> findByEmailAndPassword(String email, String passwordHash) {
        return authAdapter.findByEmailAndPassword(email, passwordHash);
    }

    @Override
    public List<User> findByStatus(UserStatus status) { return statusAdapter.findByStatus(status); }
    @Override
    public List<User> findActiveUsers() { return statusAdapter.findActiveUsers(); }
    @Override
    public List<User> findInactiveUsers() { return statusAdapter.findInactiveUsers(); }
    @Override
    public List<User> findBlockedUsers() { return statusAdapter.findBlockedUsers(); }
    @Override
    public long countByStatus(UserStatus status) { return statusAdapter.countByStatus(status); }

    @Override
    public <T extends User> List<T> findAllByRole(Class<T> roleClass) { return roleAdapter.findAllByRole(roleClass); }
    @Override
    public <T extends User> List<T> findByRoleAndStatus(Class<T> roleClass, UserStatus status) {
        return roleAdapter.findByRoleAndStatus(roleClass, status);
    }
    @Override
    public <T extends User> long countByRole(Class<T> roleClass) { return roleAdapter.countByRole(roleClass); }

    @Override
    public List<User> findByFirstName(String firstName) { return nameAdapter.findByFirstName(firstName); }
    @Override
    public List<User> findByLastName(String lastName) { return nameAdapter.findByLastName(lastName); }
    @Override
    public List<User> findByFullNameContaining(String query) { return nameAdapter.findByFullNameContaining(query); }

    @Override
    public List<User> findByRegisteredAtBetween(LocalDateTime start, LocalDateTime end) {
        return dateAdapter.findByRegisteredAtBetween(start, end);
    }
    @Override
    public List<User> findByLastActiveAtBefore(LocalDateTime date) { return dateAdapter.findByLastActiveAtBefore(date); }
    @Override
    public List<User> findInactiveSince(LocalDateTime date) { return dateAdapter.findInactiveSince(date); }

    @Override
    public List<User> findClientsWithOrders() { return clientSpecificAdapter.findClientsWithOrders(); }
    @Override
    public List<User> findClientsWithTestDrives() { return clientSpecificAdapter.findClientsWithTestDrives(); }
    @Override
    public long countClientsWithNewsletterSubscription() {
        return clientSpecificAdapter.countClientsWithNewsletterSubscription();
    }

    @Override
    public List<User> findAvailableManagers() { return managerSpecificAdapter.findAvailableManagers(); }
    @Override
    public List<User> findManagersByPosition(Position position) {
        return managerSpecificAdapter.findManagersByPosition(position);
    }
    @Override
    public List<User> findManagersWithActiveOrders() { return managerSpecificAdapter.findManagersWithActiveOrders(); }

    @Override
    public List<User> findAdminsByLevel(AdminLevel level) { return adminSpecificAdapter.findAdminsByLevel(level); }
    @Override
    public List<User> findSystemAdminsWithPermission(String permission) {
        return adminSpecificAdapter.findSystemAdminsWithPermission(permission);
    }
    @Override
    public List<User> findWarehouseAdminsBySection(String sectionId) {
        return adminSpecificAdapter.findWarehouseAdminsBySection(sectionId);
    }

    @Override
    public List<WarehouseAdmin> findAllWarehouseAdmins() {
        return warehouseAdminAdapter.findAll();
    }

    @Override
    public Optional<WarehouseAdmin> findWarehouseAdminById(String id) {
        return warehouseAdminAdapter.findById(id);
    }

    @Override
    public WarehouseAdmin saveWarehouseAdmin(WarehouseAdmin admin) {
        return warehouseAdminAdapter.save(admin);
    }

    @Override
    public void deleteWarehouseAdmin(String id) {
        warehouseAdminAdapter.delete(id);
    }

    @Override
    public List<WarehouseAdmin> findOnDutyWarehouseAdmins() {
        return warehouseAdminAdapter.findOnDutyAdmins();
    }

    @Override
    public StockOperation saveStockOperation(StockOperation operation) {
        return stockOperationAdapter.save(operation);
    }

    @Override
    public List<StockOperation> findStockOperationsByAdminId(String adminId) {
        return stockOperationAdapter.findByAdminId(adminId);
    }

    @Override
    public List<StockOperation> findStockOperationsByDateRange(LocalDateTime start, LocalDateTime end) {
        return stockOperationAdapter.findByDateRange(start, end);
    }

    @Override
    public List<StockOperation> findStockOperationsByItemId(String itemId) {
        return stockOperationAdapter.findByItemId(itemId);
    }
}