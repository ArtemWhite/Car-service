package userIntegrationTests.userMainIntegrationTests;

import dealerShipOrder.domain.models.users.User;
import dealerShipOrder.domain.models.users.UserStatus;
import dealerShipOrder.domain.models.users.client.Client;
import dealerShipOrder.domain.models.users.manager.Manager;
import dealerShipOrder.domain.models.users.manager.Position;
import dealerShipOrder.domain.models.users.systemAdmin.AdminLevel;
import dealerShipOrder.domain.models.users.systemAdmin.SystemAdmin;
import dealerShipOrder.domain.models.users.warehouseAdmin.WarehouseAdmin;
import dealerShipOrder.domain.repository.usersRepository.userRepository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserRepositoryIntegrationTest extends UserBaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        cleanUpUsers();
    }

    @Test
    void shouldSaveClientUser() {
        Client client = new Client(
                UUID.randomUUID().toString(),
                "John", "Doe", "Michael",
                "john@test.com", "+71234567890", "password123"
        );

        User saved = userRepository.save(client);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("john@test.com");
        assertThat(saved.getUserType().name()).isEqualTo("CLIENT");
        assertThat(saved.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void shouldSaveManager() {
        Manager manager = new Manager(
                "John", "Doe", "Michael",
                "manager@test.com", "+71234567890", "password123",
                UUID.randomUUID().toString()
        );

        User saved = userRepository.save(manager);

        assertThat(saved).isNotNull();
        assertThat(saved.getUserType().name()).isEqualTo("MANAGER");
        assertThat(((Manager) saved).getPosition()).isEqualTo(Position.SALES_MANAGER);
        assertThat(((Manager) saved).isAvailable()).isTrue();
    }

    @Test
    void shouldSaveSystemAdmin() {
        SystemAdmin admin = new SystemAdmin(
                "John", "Doe", "Michael",
                "admin@test.com", "+71234567890", "password123",
                UUID.randomUUID().toString(),
                AdminLevel.ADMIN
        );

        User saved = userRepository.save(admin);

        assertThat(saved).isNotNull();
        assertThat(saved.getUserType().name()).isEqualTo("SYSTEM_ADMIN");
        assertThat(((SystemAdmin) saved).getLevel()).isEqualTo(AdminLevel.ADMIN);
    }

    @Test
    void shouldSaveWarehouseAdmin() {
        WarehouseAdmin warehouseAdmin = new WarehouseAdmin(
                "John", "Doe", "Michael",
                "warehouse@test.com", "+71234567890", "password123",
                UUID.randomUUID().toString()
        );

        User saved = userRepository.save(warehouseAdmin);

        assertThat(saved).isNotNull();
        assertThat(saved.getUserType().name()).isEqualTo("WAREHOUSE_ADMIN");
    }

    @Test
    void shouldFindUserById() {
        String userId = UUID.randomUUID().toString();
        jdbcTemplate.update(
                "INSERT INTO users (id, first_name, last_name, email, phone, password_hash, status_id, user_type_id, created_at, updated_at, removed) " +
                        "VALUES (?::uuid, 'Test', 'User', 'find@test.com', '123', 'hash', " +
                        "(SELECT id FROM user_statuses WHERE name = 'ACTIVE'), " +
                        "(SELECT id FROM user_types WHERE name = 'CLIENT'), NOW(), NOW(), false)",
                UUID.fromString(userId)
        );

        var found = userRepository.findById(userId);

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(userId);
        assertThat(found.get().getEmail()).isEqualTo("find@test.com");
    }

    @Test
    void shouldFindByEmail() {
        String email = "unique@test.com";
        jdbcTemplate.update(
                "INSERT INTO users (id, first_name, last_name, email, phone, password_hash, status_id, user_type_id, created_at, updated_at, removed) " +
                        "VALUES (?::uuid, 'Test', 'User', ?, '123', 'hash', " +
                        "(SELECT id FROM user_statuses WHERE name = 'ACTIVE'), " +
                        "(SELECT id FROM user_types WHERE name = 'CLIENT'), NOW(), NOW(), false)",
                UUID.randomUUID(), email
        );

        var found = userRepository.findByEmail(email);

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo(email);
    }

    @Test
    void shouldFindByEmailAndPassword() {
        String email = "auth@test.com";
        String passwordHash = "hashed_password_123";
        jdbcTemplate.update(
                "INSERT INTO users (id, first_name, last_name, email, phone, password_hash, status_id, user_type_id, created_at, updated_at, removed) " +
                        "VALUES (?::uuid, 'Test', 'User', ?, '123', ?, " +
                        "(SELECT id FROM user_statuses WHERE name = 'ACTIVE'), " +
                        "(SELECT id FROM user_types WHERE name = 'CLIENT'), NOW(), NOW(), false)",
                UUID.randomUUID(), email, passwordHash
        );

        var found = userRepository.findByEmailAndPassword(email, passwordHash);

        assertThat(found).isPresent();
    }

    @Test
    void shouldDeleteUserById() {
        String userId = UUID.randomUUID().toString();
        jdbcTemplate.update(
                "INSERT INTO users (id, first_name, last_name, email, phone, password_hash, status_id, user_type_id, created_at, updated_at, removed) " +
                        "VALUES (?::uuid, 'Test', 'User', 'delete@test.com', '123', 'hash', " +
                        "(SELECT id FROM user_statuses WHERE name = 'ACTIVE'), " +
                        "(SELECT id FROM user_types WHERE name = 'CLIENT'), NOW(), NOW(), false)",
                UUID.fromString(userId)
        );

        userRepository.delete(userId);

        var found = userRepository.findById(userId);
        assertThat(found).isEmpty();
    }

    @Test
    void shouldNotFindDeletedUser() {
        String userId = UUID.randomUUID().toString();
        jdbcTemplate.update(
                "INSERT INTO users (id, first_name, last_name, email, phone, password_hash, status_id, user_type_id, created_at, updated_at, removed) " +
                        "VALUES (?::uuid, 'Test', 'User', 'softdelete@test.com', '123', 'hash', " +
                        "(SELECT id FROM user_statuses WHERE name = 'ACTIVE'), " +
                        "(SELECT id FROM user_types WHERE name = 'CLIENT'), NOW(), NOW(), true)",
                UUID.fromString(userId)
        );

        var found = userRepository.findById(userId);
        assertThat(found).isEmpty();
    }

    @Test
    void shouldFindAllUsers() {
        for (int i = 0; i < 5; i++) {
            jdbcTemplate.update(
                    "INSERT INTO users (id, first_name, last_name, email, phone, password_hash, status_id, user_type_id, created_at, updated_at, removed) " +
                            "VALUES (?::uuid, 'Test', 'User', ?, '123', 'hash', " +
                            "(SELECT id FROM user_statuses WHERE name = 'ACTIVE'), " +
                            "(SELECT id FROM user_types WHERE name = 'CLIENT'), NOW(), NOW(), false)",
                    UUID.randomUUID(), "user" + i + "@test.com"
            );
        }

        List<User> allUsers = userRepository.findAll();

        assertThat(allUsers.size()).isGreaterThanOrEqualTo(5);
    }

    @Test
    void shouldFindByStatus() {
        createTestUsers();

        List<User> activeUsers = userRepository.findByStatus(UserStatus.ACTIVE);
        List<User> blockedUsers = userRepository.findByStatus(UserStatus.BLOCKED);
        List<User> inactiveUsers = userRepository.findByStatus(UserStatus.INACTIVE);

        assertThat(activeUsers).isNotEmpty();
        assertThat(blockedUsers).isNotEmpty();
        assertThat(inactiveUsers).isNotEmpty();
    }

    @Test
    void shouldFindActiveUsers() {
        createTestUsers();

        List<User> activeUsers = userRepository.findActiveUsers();

        assertThat(activeUsers).allMatch(u -> u.getStatus() == UserStatus.ACTIVE);
    }

    @Test
    void shouldFindInactiveUsers() {
        createTestUsers();

        List<User> inactiveUsers = userRepository.findInactiveUsers();

        assertThat(inactiveUsers).allMatch(u -> u.getStatus() == UserStatus.INACTIVE);
    }

    @Test
    void shouldFindBlockedUsers() {
        createTestUsers();

        List<User> blockedUsers = userRepository.findBlockedUsers();

        assertThat(blockedUsers).allMatch(u -> u.getStatus() == UserStatus.BLOCKED);
    }

    @Test
    void shouldCountByStatus() {
        createTestUsers();

        long activeCount = userRepository.countByStatus(UserStatus.ACTIVE);
        long blockedCount = userRepository.countByStatus(UserStatus.BLOCKED);

        assertThat(activeCount).isGreaterThan(0);
        assertThat(blockedCount).isGreaterThan(0);
    }

    @Test
    void shouldFindAllByRole() {
        createTestUsers();

        List<Client> clients = userRepository.findAllByRole(Client.class);
        List<Manager> managers = userRepository.findAllByRole(Manager.class);
        List<SystemAdmin> admins = userRepository.findAllByRole(SystemAdmin.class);
        List<WarehouseAdmin> warehouseAdmins = userRepository.findAllByRole(WarehouseAdmin.class);

        assertThat(clients).isNotEmpty();
        assertThat(managers).isNotEmpty();
        assertThat(admins).isNotEmpty();
        assertThat(warehouseAdmins).isNotEmpty();
    }

    @Test
    void shouldFindByRoleAndStatus() {
        createTestUsers();

        List<Client> activeClients = userRepository.findByRoleAndStatus(Client.class, UserStatus.ACTIVE);

        assertThat(activeClients).isNotEmpty();
        assertThat(activeClients).allMatch(c -> c.getStatus() == UserStatus.ACTIVE);
    }

    @Test
    void shouldCountByRole() {
        createTestUsers();

        long clientCount = userRepository.countByRole(Client.class);
        long managerCount = userRepository.countByRole(Manager.class);

        assertThat(clientCount).isGreaterThan(0);
        assertThat(managerCount).isGreaterThan(0);
    }

    @Test
    void shouldFindByFirstName() {
        String firstName = "UniqueFirstName_" + UUID.randomUUID().toString().substring(0, 8);
        jdbcTemplate.update(
                "INSERT INTO users (id, first_name, last_name, email, phone, password_hash, status_id, user_type_id, created_at, updated_at, removed) " +
                        "VALUES (?::uuid, ?, 'User', 'name@test.com', '123', 'hash', " +
                        "(SELECT id FROM user_statuses WHERE name = 'ACTIVE'), " +
                        "(SELECT id FROM user_types WHERE name = 'CLIENT'), NOW(), NOW(), false)",
                UUID.randomUUID(), firstName
        );

        List<User> users = userRepository.findByFirstName(firstName);

        assertThat(users).isNotEmpty();
        assertThat(users.get(0).getFirstName()).isEqualTo(firstName);
    }

    @Test
    void shouldFindByLastName() {
        String lastName = "UniqueLastName_" + UUID.randomUUID().toString().substring(0, 8);
        jdbcTemplate.update(
                "INSERT INTO users (id, first_name, last_name, email, phone, password_hash, status_id, user_type_id, created_at, updated_at, removed) " +
                        "VALUES (?::uuid, 'Test', ?, 'lastname@test.com', '123', 'hash', " +
                        "(SELECT id FROM user_statuses WHERE name = 'ACTIVE'), " +
                        "(SELECT id FROM user_types WHERE name = 'CLIENT'), NOW(), NOW(), false)",
                UUID.randomUUID(), lastName
        );

        List<User> users = userRepository.findByLastName(lastName);

        assertThat(users).isNotEmpty();
        assertThat(users.get(0).getLastName()).isEqualTo(lastName);
    }

    @Test
    void shouldFindByFullNameContaining() {
        jdbcTemplate.update(
                "INSERT INTO users (id, first_name, last_name, email, phone, password_hash, status_id, user_type_id, created_at, updated_at, removed) " +
                        "VALUES (?::uuid, 'John', 'Doe', 'fullname@test.com', '123', 'hash', " +
                        "(SELECT id FROM user_statuses WHERE name = 'ACTIVE'), " +
                        "(SELECT id FROM user_types WHERE name = 'CLIENT'), NOW(), NOW(), false)",
                UUID.randomUUID()
        );

        List<User> users = userRepository.findByFullNameContaining("John Doe");

        assertThat(users).isNotEmpty();
    }

    @Test
    void shouldFindByRegisteredAtBetween() {
        createTestUsers();

        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        List<User> users = userRepository.findByRegisteredAtBetween(start, end);

        assertThat(users).isNotEmpty();
    }

    @Test
    void shouldFindByLastActiveAtBefore() {
        String userId = UUID.randomUUID().toString();
        jdbcTemplate.update(
                "INSERT INTO users (id, first_name, last_name, email, phone, password_hash, status_id, user_type_id, last_active_at, created_at, updated_at, removed) " +
                        "VALUES (?::uuid, 'Test', 'User', 'old@test.com', '123', 'hash', " +
                        "(SELECT id FROM user_statuses WHERE name = 'ACTIVE'), " +
                        "(SELECT id FROM user_types WHERE name = 'CLIENT'), '2020-01-01 00:00:00', NOW(), NOW(), false)",
                UUID.fromString(userId)
        );

        List<User> users = userRepository.findByLastActiveAtBefore(LocalDateTime.now().minusDays(30));

        assertThat(users).isNotEmpty();
    }

    @Test
    void shouldFindInactiveSince() {
        String userId = UUID.randomUUID().toString();
        jdbcTemplate.update(
                "INSERT INTO users (id, first_name, last_name, email, phone, password_hash, status_id, user_type_id, last_active_at, created_at, updated_at, removed) " +
                        "VALUES (?::uuid, 'Test', 'User', 'inactive@test.com', '123', 'hash', " +
                        "(SELECT id FROM user_statuses WHERE name = 'INACTIVE'), " +
                        "(SELECT id FROM user_types WHERE name = 'CLIENT'), '2020-01-01 00:00:00', NOW(), NOW(), false)",
                UUID.fromString(userId)
        );

        List<User> users = userRepository.findInactiveSince(LocalDateTime.now().minusDays(30));

        assertThat(users).isNotEmpty();
    }

    @Test
    void shouldFindAvailableManagers() {
        createTestUsers();

        List<User> availableManagers = userRepository.findAvailableManagers();

        assertThat(availableManagers).isNotEmpty();
        assertThat(availableManagers.get(0)).isInstanceOf(Manager.class);
        assertThat(((Manager) availableManagers.get(0)).isAvailable()).isTrue();
    }

    @Test
    void shouldFindManagersByPosition() {
        createTestUsers();

        List<User> salesManagers = userRepository.findManagersByPosition(Position.SALES_MANAGER);

        assertThat(salesManagers).isNotEmpty();
    }

    @Test
    void shouldFindManagersWithActiveOrders() {
        createTestUsers();

        String orderId = UUID.randomUUID().toString();
        jdbcTemplate.update(
                "INSERT INTO manager_orders (manager_id, order_id) VALUES (?::uuid, ?)",
                UUID.fromString(managerId), orderId
        );

        List<User> managersWithOrders = userRepository.findManagersWithActiveOrders();

        assertThat(managersWithOrders).isNotEmpty();
    }

    @Test
    void shouldFindAdminsByLevel() {
        createTestUsers();

        List<User> superAdmins = userRepository.findAdminsByLevel(AdminLevel.SUPER_ADMIN);

        assertThat(superAdmins).isNotEmpty();
    }

    @Test
    void shouldFindSystemAdminsWithPermission() {
        createTestUsers();
        addPermissionToAdmin(adminId, "CREATE_USER");

        List<User> adminsWithPermission = userRepository.findSystemAdminsWithPermission("CREATE_USER");

        assertThat(adminsWithPermission).isNotEmpty();
    }

    @Test
    void shouldFindWarehouseAdminsBySection() {
        createTestUsers();
        String sectionId = "SEC-001";
        assignWarehouseAdminToSection(warehouseAdminId, sectionId);

        List<User> admins = userRepository.findWarehouseAdminsBySection(sectionId);

        assertThat(admins).isNotEmpty();
        assertThat(admins.get(0).getId()).isEqualTo(warehouseAdminId);
    }

    @Test
    void shouldFindClientsWithOrders() {
        createTestUsers();

        String orderId = UUID.randomUUID().toString();
        jdbcTemplate.update(
                "INSERT INTO client_orders (client_id, order_id) VALUES (?::uuid, ?)",
                UUID.fromString(clientId), orderId
        );

        List<User> clientsWithOrders = userRepository.findClientsWithOrders();

        assertThat(clientsWithOrders).isNotEmpty();
    }

    @Test
    void shouldFindClientsWithTestDrives() {
        createTestUsers();

        String testDriveId = UUID.randomUUID().toString();
        jdbcTemplate.update(
                "INSERT INTO client_test_drives (client_id, test_drive_id) VALUES (?::uuid, ?)",
                UUID.fromString(clientId), testDriveId
        );

        List<User> clientsWithTestDrives = userRepository.findClientsWithTestDrives();

        assertThat(clientsWithTestDrives).isNotEmpty();
    }

    @Test
    void shouldCountClientsWithNewsletterSubscription() {
        createTestUsers();

        jdbcTemplate.update(
                "UPDATE clients SET newsletter_subscribed = true WHERE user_id = ?::uuid",
                UUID.fromString(clientId)
        );

        long count = userRepository.countClientsWithNewsletterSubscription();

        assertThat(count).isGreaterThan(0);
    }

    @Test
    void shouldCheckExistsById() {
        String userId = UUID.randomUUID().toString();
        jdbcTemplate.update(
                "INSERT INTO users (id, first_name, last_name, email, phone, password_hash, status_id, user_type_id, created_at, updated_at, removed) " +
                        "VALUES (?::uuid, 'Test', 'User', 'exists@test.com', '123', 'hash', " +
                        "(SELECT id FROM user_statuses WHERE name = 'ACTIVE'), " +
                        "(SELECT id FROM user_types WHERE name = 'CLIENT'), NOW(), NOW(), false)",
                UUID.fromString(userId)
        );

        boolean exists = userRepository.existsById(userId);
        boolean notExists = userRepository.existsById(UUID.randomUUID().toString());

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    void shouldCheckExistsByEmail() {
        String email = "uniqueexists@test.com";
        jdbcTemplate.update(
                "INSERT INTO users (id, first_name, last_name, email, phone, password_hash, status_id, user_type_id, created_at, updated_at, removed) " +
                        "VALUES (?::uuid, 'Test', 'User', ?, '123', 'hash', " +
                        "(SELECT id FROM user_statuses WHERE name = 'ACTIVE'), " +
                        "(SELECT id FROM user_types WHERE name = 'CLIENT'), NOW(), NOW(), false)",
                UUID.randomUUID(), email
        );

        boolean exists = userRepository.existsByEmail(email);
        boolean notExists = userRepository.existsByEmail("nonexistent@test.com");

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }
}