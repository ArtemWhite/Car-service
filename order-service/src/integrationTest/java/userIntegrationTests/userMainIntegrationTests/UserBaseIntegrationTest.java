package userIntegrationTests.userMainIntegrationTests;

import dealerShipOrder.BaseIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
public abstract class UserBaseIntegrationTest extends BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected EntityManager entityManager;

    protected String adminId;
    protected String managerId;
    protected String clientId;
    protected String warehouseAdminId;
    protected String blockedUserId;
    protected String inactiveUserId;

    @BeforeEach
    void initReferenceData() {
        ensureUserStatusesExist();
        ensureUserTypesExist();
        ensureManagerPositionsExist();
        ensureAdminLevelsExist();
        ensureWarehousePositionsExist();
        ensureOrderStatusesExist();
        ensureOrderTypesExist();
        ensurePaymentMethodsExist();
        ensurePaymentStatusesExist();
        ensureTestDriveStatusesExist();
        ensureOperationTypesExist();
        ensureItemTypesExist();
        ensureSystemPermissionsExist();
    }

    private void ensureUserStatusesExist() {
        String[][] statuses = {
                {"ACTIVE", "Активен", "true"},
                {"INACTIVE", "Неактивен", "false"},
                {"BLOCKED", "Заблокирован", "false"},
                {"PENDING_VERIFICATION", "Ожидает подтверждения", "false"},
                {"DELETED", "Удалён", "false"}
        };
        for (String[] status : statuses) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM user_statuses WHERE name = ?",
                    Integer.class, status[0]);
            if (count == 0) {
                jdbcTemplate.update(
                        "INSERT INTO user_statuses (id, name, display_name, can_authenticate, can_be_restored, created_at, updated_at, removed) " +
                                "VALUES (gen_random_uuid(), ?, ?, ?, true, NOW(), NOW(), false)",
                        status[0], status[1], Boolean.parseBoolean(status[2]));
            }
        }
    }

    private void ensureUserTypesExist() {
        String[][] types = {
                {"CLIENT", "Клиент"},
                {"MANAGER", "Менеджер"},
                {"SYSTEM_ADMIN", "Системный администратор"},
                {"WAREHOUSE_ADMIN", "Складской администратор"}
        };
        for (String[] type : types) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM user_types WHERE name = ?",
                    Integer.class, type[0]);
            if (count == 0) {
                jdbcTemplate.update(
                        "INSERT INTO user_types (id, name, display_name, created_at, updated_at, removed) " +
                                "VALUES (gen_random_uuid(), ?, ?, NOW(), NOW(), false)",
                        type[0], type[1]);
            }
        }
    }

    private void ensureManagerPositionsExist() {
        Object[][] positions = {
                {"SALES_MANAGER", "Менеджер по продажам", 10, 5},
                {"SENIOR_MANAGER", "Старший менеджер", 15, 10},
                {"LEAD_MANAGER", "Ведущий менеджер", 20, 15}
        };
        for (Object[] pos : positions) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM manager_positions WHERE name = ?",
                    Integer.class, pos[0]);
            if (count == 0) {
                jdbcTemplate.update(
                        "INSERT INTO manager_positions (id, name, display_name, max_concurrent_orders, max_concurrent_test_drives, created_at, updated_at, removed) " +
                                "VALUES (gen_random_uuid(), ?, ?, ?, ?, NOW(), NOW(), false)",
                        pos[0], pos[1], pos[2], pos[3]);
            }
        }
    }

    private void ensureAdminLevelsExist() {
        Object[][] levels = {
                {"SUPER_ADMIN", "Супер администратор", 100},
                {"ADMIN", "Администратор", 50},
                {"JUNIOR_ADMIN", "Младший администратор", 10},
                {"SUPPORT", "Поддержка", 5}
        };
        for (Object[] level : levels) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM admin_levels WHERE name = ?",
                    Integer.class, level[0]);
            if (count == 0) {
                jdbcTemplate.update(
                        "INSERT INTO admin_levels (id, name, display_name, level, created_at, updated_at, removed) " +
                                "VALUES (gen_random_uuid(), ?, ?, ?, NOW(), NOW(), false)",
                        level[0], level[1], level[2]);
            }
        }
    }

    private void ensureWarehousePositionsExist() {
        Object[][] positions = {
                {"WAREHOUSE_WORKER", "Кладовщик"},
                {"STOREKEEPER", "Кладовщик"},
                {"SENIOR_STOREKEEPER", "Старший кладовщик"},
                {"WAREHOUSE_MANAGER", "Заведующий складом"}
        };
        for (Object[] pos : positions) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM warehouse_positions WHERE name = ?",
                    Integer.class, pos[0]);
            if (count == 0) {
                jdbcTemplate.update(
                        "INSERT INTO warehouse_positions (id, name, display_name, created_at, updated_at, removed) " +
                                "VALUES (gen_random_uuid(), ?, ?, NOW(), NOW(), false)",
                        pos[0], pos[1]);
            }
        }
    }

    private void ensureCarReferenceDataExists() {
        String[] bodies = {"SEDAN", "HATCHBACK", "SUV", "COUPE", "CONVERTIBLE", "STATION_WAGON", "MINIVAN", "PICKUP"};
        for (String body : bodies) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM car_bodies WHERE name = ?", Integer.class, body);
            if (count == 0) {
                jdbcTemplate.update(
                        "INSERT INTO car_bodies (id, name, display_name, created_at, updated_at, removed) " +
                                "VALUES (gen_random_uuid(), ?, ?, NOW(), NOW(), false)", body, body);
            }
        }

        String[][] colors = {
                {"WHITE", "Белый"}, {"BLACK", "Чёрный"}, {"SILVER", "Серебристый"},
                {"RED", "Красный"}, {"BLUE", "Синий"}, {"GREY", "Серый"}
        };
        for (String[] color : colors) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM car_colors WHERE name = ?", Integer.class, color[0]);
            if (count == 0) {
                jdbcTemplate.update(
                        "INSERT INTO car_colors (id, name, display_name, is_default, color_price, created_at, updated_at, removed) " +
                                "VALUES (gen_random_uuid(), ?, ?, false, 0, NOW(), NOW(), false)", color[0], color[1]);
            }
        }

        Object[][] drives = {
                {"FRONT", "Передний", "FWD"}, {"REAR", "Задний", "RWD"}, {"FULL", "Полный", "AWD"}, {"PART_TIME", "Подключаемый", "4WD"}
        };
        for (Object[] drive : drives) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM drive_types WHERE name = ?", Integer.class, drive[0]);
            if (count == 0) {
                jdbcTemplate.update(
                        "INSERT INTO drive_types (id, name, display_name, code_name, created_at, updated_at, removed) " +
                                "VALUES (gen_random_uuid(), ?, ?, ?, NOW(), NOW(), false)", drive[0], drive[1], drive[2]);
            }
        }

        String[] fuels = {"PETROL", "DIESEL", "ELECTRIC", "HYBRID", "PLUGIN_HYBRID"};
        String[] fuelNames = {"Бензин", "Дизель", "Электро", "Гибрид", "Подзаряжаемый гибрид"};
        for (int i = 0; i < fuels.length; i++) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM engine_fuel_types WHERE name = ?", Integer.class, fuels[i]);
            if (count == 0) {
                jdbcTemplate.update(
                        "INSERT INTO engine_fuel_types (id, name, display_name, created_at, updated_at, removed) " +
                                "VALUES (gen_random_uuid(), ?, ?, NOW(), NOW(), false)", fuels[i], fuelNames[i]);
            }
        }

        String[][] transmissions = {
                {"MANUAL", "Механическая"}, {"AUTOMATIC", "Автоматическая"}, {"CVT", "Вариатор"}, {"ROBOT", "Роботизированная"}
        };
        for (String[] trans : transmissions) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM transmission_types WHERE name = ?", Integer.class, trans[0]);
            if (count == 0) {
                jdbcTemplate.update(
                        "INSERT INTO transmission_types (id, name, display_name, created_at, updated_at, removed) " +
                                "VALUES (gen_random_uuid(), ?, ?, NOW(), NOW(), false)", trans[0], trans[1]);
            }
        }

        String[][] carStatuses = {
                {"AVAILABLE", "В наличии"}, {"UNAVAILABLE", "Недоступен"}, {"SOLD", "Продан"},
                {"RESERVED", "Зарезервирован"}, {"ON_TEST_DRIVE", "На тест-драйве"},
                {"TEST_DRIVE_AVAILABLE", "Доступен для тест-драйва"}, {"IN_SERVICE", "На обслуживании"},
                {"BOOKED", "Забронирован"}
        };
        for (String[] status : carStatuses) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM car_statuses WHERE name = ?", Integer.class, status[0]);
            if (count == 0) {
                jdbcTemplate.update(
                        "INSERT INTO car_statuses (id, name, display_name, created_at, updated_at, removed) " +
                                "VALUES (gen_random_uuid(), ?, ?, NOW(), NOW(), false)", status[0], status[1]);
            }
        }
    }

    private void ensureSpareTypesExist() {
        String[][] spareTypes = {
                {"OIL_FILTER", "Масляный фильтр"}, {"AIR_FILTER", "Воздушный фильтр"},
                {"BRAKE_PADS", "Тормозные колодки"}, {"BRAKE_DISCS", "Тормозные диски"},
                {"SPARK_PLUG", "Свечи зажигания"}, {"BATTERY", "Аккумулятор"},
                {"TIMING_BELT", "Ремень ГРМ"}, {"ALTERNATOR", "Генератор"},
                {"STARTER", "Стартер"}, {"SHOCK_ABSORBER", "Амортизатор"},
                {"TIRE", "Шина"}, {"WHEEL", "Диск колесный"},
                {"HEADLIGHT", "Фара"}, {"RADIATOR", "Радиатор"},
                {"SENSOR", "Датчик"}, {"WIPER", "Щетки стеклоочистителя"},
                {"OIL", "Моторное масло"}, {"COOLANT", "Охлаждающая жидкость"}
        };
        for (String[] type : spareTypes) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM spare_types WHERE name = ?", Integer.class, type[0]);
            if (count == 0) {
                jdbcTemplate.update(
                        "INSERT INTO spare_types (id, name, display_name, created_at, updated_at, removed) " +
                                "VALUES (gen_random_uuid(), ?, ?, NOW(), NOW(), false)", type[0], type[1]);
            }
        }
    }

    private void ensureOrderStatusesExist() {
        String[][] statuses = {
                {"CREATED", "Оформлен"}, {"MANAGER_APPROVED", "Согласован менеджером"},
                {"AWAITING_PAYMENT", "Ожидает оплаты"}, {"PAID", "Оплачен"},
                {"READY_FOR_PICKUP", "Автомобиль готов к выдаче"}, {"COMPLETED", "Завершён"},
                {"CANCELLED", "Отменён"}, {"STOCK_CONFIRMED", "Согласован складом"},
                {"AWAITING_DELIVERY", "Ожидает доставки"}
        };
        for (String[] status : statuses) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM order_statuses WHERE name = ?", Integer.class, status[0]);
            if (count == 0) {
                jdbcTemplate.update(
                        "INSERT INTO order_statuses (id, name, display_name, created_at, updated_at, removed) " +
                                "VALUES (gen_random_uuid(), ?, ?, NOW(), NOW(), false)", status[0], status[1]);
            }
        }
    }

    private void ensureOrderTypesExist() {
        String[][] types = {
                {"IN_STOCK", "Заказ на автомобиль в наличии"},
                {"CUSTOM", "Заказ на автомобиль с конфигурацией"}
        };
        for (String[] type : types) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM order_types WHERE name = ?", Integer.class, type[0]);
            if (count == 0) {
                jdbcTemplate.update(
                        "INSERT INTO order_types (id, name, display_name, created_at, updated_at, removed) " +
                                "VALUES (gen_random_uuid(), ?, ?, NOW(), NOW(), false)", type[0], type[1]);
            }
        }
    }

    private void ensurePaymentMethodsExist() {
        String[][] methods = {
                {"CASH", "Наличные"}, {"CARD", "Банковская карта"},
                {"ONLINE", "Онлайн-оплата"}, {"INSTALLMENT", "Рассрочка"}
        };
        for (String[] method : methods) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM payment_methods WHERE name = ?", Integer.class, method[0]);
            if (count == 0) {
                jdbcTemplate.update(
                        "INSERT INTO payment_methods (id, name, display_name, created_at, updated_at, removed) " +
                                "VALUES (gen_random_uuid(), ?, ?, NOW(), NOW(), false)", method[0], method[1]);
            }
        }
    }

    private void ensurePaymentStatusesExist() {
        String[][] statuses = {
                {"PENDING", "Ожидает оплаты"}, {"PROCESSING", "Обрабатывается"},
                {"COMPLETED", "Оплачен"}, {"FAILED", "Ошибка оплаты"},
                {"REFUNDED", "Возврат"}
        };
        for (String[] status : statuses) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM payment_statuses WHERE name = ?", Integer.class, status[0]);
            if (count == 0) {
                jdbcTemplate.update(
                        "INSERT INTO payment_statuses (id, name, display_name, created_at, updated_at, removed) " +
                                "VALUES (gen_random_uuid(), ?, ?, NOW(), NOW(), false)", status[0], status[1]);
            }
        }
    }

    private void ensureTestDriveStatusesExist() {
        String[][] statuses = {
                {"PENDING", "Ожидает подтверждения"}, {"CONFIRMED", "Подтверждён"},
                {"COMPLETED", "Проведён"}, {"CANCELLED", "Отменён"},
                {"NO_SHOW", "Клиент не пришёл"}
        };
        for (String[] status : statuses) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM test_drive_statuses WHERE name = ?", Integer.class, status[0]);
            if (count == 0) {
                jdbcTemplate.update(
                        "INSERT INTO test_drive_statuses (id, name, display_name, created_at, updated_at, removed) " +
                                "VALUES (gen_random_uuid(), ?, ?, NOW(), NOW(), false)", status[0], status[1]);
            }
        }
    }

    private void ensureOperationTypesExist() {
        String[][] types = {
                {"ARRIVAL", "Поступление"}, {"REMOVAL", "Выбытие"},
                {"MOVE", "Перемещение"}, {"WRITE_OFF", "Списание"},
                {"INVENTORY_START", "Начало инвентаризации"}, {"INVENTORY_COMPLETE", "Завершение инвентаризации"},
                {"QUANTITY_CHANGE", "Изменение количества"}, {"SHIFT_START", "Начало смены"},
                {"SHIFT_END", "Конец смены"}, {"UPDATE", "Обновление"},
                {"DISCREPANCY", "Расхождение"}
        };
        for (String[] type : types) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM operation_types WHERE name = ?", Integer.class, type[0]);
            if (count == 0) {
                jdbcTemplate.update(
                        "INSERT INTO operation_types (id, name, display_name, created_at, updated_at, removed) " +
                                "VALUES (gen_random_uuid(), ?, ?, NOW(), NOW(), false)", type[0], type[1]);
            }
        }
    }

    private void ensureItemTypesExist() {
        String[][] types = {
                {"SPARE_PART", "Запчасть"}, {"CAR", "Автомобиль"},
                {"EQUIPMENT", "Оборудование"}
        };
        for (String[] type : types) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM item_types WHERE name = ?", Integer.class, type[0]);
            if (count == 0) {
                jdbcTemplate.update(
                        "INSERT INTO item_types (id, name, display_name, created_at, updated_at, removed) " +
                                "VALUES (gen_random_uuid(), ?, ?, NOW(), NOW(), false)", type[0], type[1]);
            }
        }
    }

    private void ensureSystemPermissionsExist() {
        String[][] permissions = {
                {"CREATE_USER", "Создание пользователей", "USER"},
                {"UPDATE_USER", "Редактирование пользователей", "USER"},
                {"DELETE_USER", "Удаление пользователей", "USER"},
                {"VIEW_USERS", "Просмотр пользователей", "USER"},
                {"BLOCK_USER", "Блокировка пользователей", "USER"},
                {"MANAGE_CARS", "Управление автомобилями", "CAR"},
                {"VIEW_CARS", "Просмотр автомобилей", "CAR"},
                {"MANAGE_SPARE_PARTS", "Управление запчастями", "SPARE_PARTS"},
                {"VIEW_SPARE_PARTS", "Просмотр запчастей", "SPARE_PARTS"},
                {"VIEW_ORDERS", "Просмотр заказов", "ORDER"},
                {"UPDATE_ORDER", "Обновление заказа", "ORDER"},
                {"CANCEL_ORDER", "Отмена заказа", "ORDER"},
                {"VIEW_TEST_DRIVES", "Просмотр тест-драйвов", "TEST_DRIVE"},
                {"UPDATE_TEST_DRIVE", "Обновление тест-драйва", "TEST_DRIVE"},
                {"MANAGE_PERMISSIONS", "Управление правами", "ADMIN"},
                {"VIEW_AUDIT_LOG", "Просмотр аудита", "AUDIT"},
                {"SYSTEM_CONFIG", "Настройка системы", "SYSTEM"}
        };
        for (String[] perm : permissions) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM system_permissions WHERE name = ?", Integer.class, perm[0]);
            if (count == 0) {
                jdbcTemplate.update(
                        "INSERT INTO system_permissions (id, name, display_name, category, created_at, updated_at, removed) " +
                                "VALUES (gen_random_uuid(), ?, ?, ?, NOW(), NOW(), false)", perm[0], perm[1], perm[2]);
            }
        }
    }

    protected void createTestUsers() {
        adminId = UUID.randomUUID().toString();
        createUser(adminId, "SYSTEM_ADMIN", "admin@test.com", "ACTIVE");
        createSystemAdmin(adminId, "SUPER_ADMIN");

        managerId = UUID.randomUUID().toString();
        createUser(managerId, "MANAGER", "manager@test.com", "ACTIVE");
        createManager(managerId, "SALES_MANAGER");

        clientId = UUID.randomUUID().toString();
        createUser(clientId, "CLIENT", "client@test.com", "ACTIVE");
        createClient(clientId);

        warehouseAdminId = UUID.randomUUID().toString();
        createUser(warehouseAdminId, "WAREHOUSE_ADMIN", "warehouse@test.com", "ACTIVE");
        createWarehouseAdmin(warehouseAdminId, "STOREKEEPER");

        blockedUserId = UUID.randomUUID().toString();
        createUser(blockedUserId, "CLIENT", "blocked@test.com", "BLOCKED");
        createClient(blockedUserId);

        inactiveUserId = UUID.randomUUID().toString();
        createUser(inactiveUserId, "CLIENT", "inactive@test.com", "INACTIVE");
        createClient(inactiveUserId);

        entityManager.flush();
        entityManager.clear();
    }

    protected void createUser(String id, String userType, String email, String status) {
        jdbcTemplate.update(
                "INSERT INTO users (id, first_name, last_name, middle_name, email, phone, password_hash, status_id, user_type_id, last_active_at, last_password_change_at, created_at, updated_at, removed) " +
                        "VALUES (?::uuid, ?, ?, ?, ?, ?, ?, (SELECT id FROM user_statuses WHERE name = ?), (SELECT id FROM user_types WHERE name = ?), NOW(), NOW(), NOW(), NOW(), false)",
                UUID.fromString(id), "Test", "User", "Testovich", email, "+71234567890",
                "hashed_" + id.substring(0, 8), status, userType
        );
    }

    protected void createClient(String id) {
        jdbcTemplate.update(
                "INSERT INTO clients (user_id, preferred_contact_method, newsletter_subscribed) VALUES (?::uuid, 'EMAIL', false)",
                UUID.fromString(id)
        );
    }

    protected void createManager(String id, String position) {
        jdbcTemplate.update(
                "INSERT INTO managers (user_id, position_id, max_concurrent_orders, max_concurrent_test_drives, available) " +
                        "VALUES (?::uuid, (SELECT id FROM manager_positions WHERE name = ?), 10, 5, true)",
                UUID.fromString(id), position
        );
    }

    protected void createSystemAdmin(String id, String level) {
        jdbcTemplate.update(
                "INSERT INTO system_admins (user_id, admin_level_id, last_login_at) " +
                        "VALUES (?::uuid, (SELECT id FROM admin_levels WHERE name = ?), NOW())",
                UUID.fromString(id), level
        );
    }

    protected void createWarehouseAdmin(String id, String position) {
        jdbcTemplate.update(
                "INSERT INTO warehouse_admins (user_id, warehouse_position_id, on_duty) " +
                        "VALUES (?::uuid, (SELECT id FROM warehouse_positions WHERE name = ?), false)",
                UUID.fromString(id), position
        );
    }

    protected void assignWarehouseAdminToSection(String adminId, String sectionId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM warehouse_admin_sections WHERE admin_id = ?::uuid AND section_id = ?",
                Integer.class, UUID.fromString(adminId), sectionId
        );
        if (count == 0) {
            jdbcTemplate.update(
                    "INSERT INTO warehouse_admin_sections (admin_id, section_id) VALUES (?::uuid, ?)",
                    UUID.fromString(adminId), sectionId
            );
        }
    }

    protected void addPermissionToAdmin(String adminId, String permission) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM admin_permissions WHERE admin_id = ?::uuid AND permission_id = (SELECT id FROM system_permissions WHERE name = ?)",
                Integer.class, UUID.fromString(adminId), permission
        );
        if (count == 0) {
            jdbcTemplate.update(
                    "INSERT INTO admin_permissions (admin_id, permission_id) VALUES (?::uuid, (SELECT id FROM system_permissions WHERE name = ?))",
                    UUID.fromString(adminId), permission
            );
        }
    }

    protected void createAuditLogEntry(String adminId, String action, String details) {
        jdbcTemplate.update(
                "INSERT INTO audit_log_entries (id, admin_id, action, details, log_timestamp, created_at, updated_at, removed) " +
                        "VALUES (?::uuid, ?::uuid, ?, ?, NOW(), NOW(), NOW(), false)",
                UUID.randomUUID(), UUID.fromString(adminId), action, details
        );
    }

    protected String getUserStatus(String userId) {
        return jdbcTemplate.queryForObject(
                "SELECT s.name FROM users u JOIN user_statuses s ON u.status_id = s.id WHERE u.id = ?::uuid AND u.removed = false",
                String.class, UUID.fromString(userId)
        );
    }

    protected String getUserType(String userId) {
        return jdbcTemplate.queryForObject(
                "SELECT t.name FROM users u JOIN user_types t ON u.user_type_id = t.id WHERE u.id = ?::uuid AND u.removed = false",
                String.class, UUID.fromString(userId)
        );
    }

    protected boolean existsByEmail(String email) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE email = ? AND removed = false",
                Integer.class, email
        );
        return count > 0;
    }

    protected boolean existsById(String userId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE id = ?::uuid AND removed = false",
                Integer.class, UUID.fromString(userId)
        );
        return count > 0;
    }

    protected void cleanUpUsers() {
        jdbcTemplate.execute("DELETE FROM admin_permissions");
        jdbcTemplate.execute("DELETE FROM audit_log_entries");
        jdbcTemplate.execute("DELETE FROM warehouse_admin_sections");
        jdbcTemplate.execute("DELETE FROM client_orders");
        jdbcTemplate.execute("DELETE FROM client_test_drives");
        jdbcTemplate.execute("DELETE FROM manager_orders");
        jdbcTemplate.execute("DELETE FROM manager_test_drives");
        jdbcTemplate.execute("DELETE FROM manager_test_drive_fleet");
        jdbcTemplate.execute("DELETE FROM clients");
        jdbcTemplate.execute("DELETE FROM managers");
        jdbcTemplate.execute("DELETE FROM system_admins");
        jdbcTemplate.execute("DELETE FROM warehouse_admins");
        jdbcTemplate.execute("DELETE FROM users WHERE email LIKE '%@test.com'");
    }

    protected String createTestSparePart() throws Exception {
        return UUID.randomUUID().toString();
    }

    protected String createTestCar() throws Exception {
        return UUID.randomUUID().toString();
    }
}