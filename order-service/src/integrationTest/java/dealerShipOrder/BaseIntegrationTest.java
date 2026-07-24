package dealerShipOrder;

import domain.models.car.Car;
import domain.models.car.CarConfiguration;
import domain.models.car.Price;
import domain.models.car.types.CarStatus;
import domain.repository.carRepository.CarRepository;
import domain.repository.carRepository.ConfigurationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Testcontainers
@ExtendWith(SpringExtension.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = OrderServiceApplication.class
)
@Import(TestSecurityConfiguration.class)
public abstract class BaseIntegrationTest {

    @MockBean
    protected CarRepository carRepository;

    @MockBean
    protected ConfigurationRepository configurationRepository;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUpDefaultMocks() {
        Car mockCar = mock(Car.class);
        when(mockCar.isAvailableForPurchase()).thenReturn(true);
        when(mockCar.isAvailableForTestDrive()).thenReturn(true);
        when(mockCar.isSold()).thenReturn(false);
        when(mockCar.isReserved()).thenReturn(false);
        when(mockCar.getCarStatus()).thenReturn(CarStatus.AVAILABLE);
        when(mockCar.getPrice()).thenReturn(Price.of(2500000.0, "RUB"));

        when(carRepository.findById(anyString())).thenReturn(Optional.of(mockCar));
        when(carRepository.save(any(Car.class))).thenAnswer(inv -> inv.getArgument(0));

        ensureReferenceData();
    }

    private void ensureReferenceData() {
        ensureUserStatuses();
        ensureUserTypes();
        ensureOrderStatuses();
        ensureOrderTypes();
        ensurePaymentMethods();
        ensurePaymentStatuses();
        ensureTestDriveStatuses();
        ensureManagerPositions();
        ensureAdminLevels();
    }

    private void ensureUserStatuses() {
        String[][] statuses = {
                {"ACTIVE", "Активен", "true"},
                {"INACTIVE", "Неактивен", "false"},
                {"BLOCKED", "Заблокирован", "false"}
        };
        for (String[] s : statuses) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM user_statuses WHERE name = ?", Integer.class, s[0]);
            if (count != null && count == 0) {
                jdbcTemplate.update(
                        "INSERT INTO user_statuses (id, name, display_name, can_authenticate, created_at, updated_at, removed) " +
                                "VALUES (gen_random_uuid(), ?, ?, ?, NOW(), NOW(), false)", s[0], s[1], Boolean.parseBoolean(s[2]));
            }
        }
    }

    private void ensureUserTypes() {
        String[] types = {"CLIENT", "MANAGER", "SYSTEM_ADMIN", "WAREHOUSE_ADMIN"};
        for (String t : types) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM user_types WHERE name = ?", Integer.class, t);
            if (count != null && count == 0) {
                jdbcTemplate.update(
                        "INSERT INTO user_types (id, name, display_name, created_at, updated_at, removed) " +
                                "VALUES (gen_random_uuid(), ?, ?, NOW(), NOW(), false)", t, t);
            }
        }
    }

    private void ensureOrderStatuses() {
        String[][] statuses = {
                {"CREATED", "Оформлен"}, {"MANAGER_APPROVED", "Согласован менеджером"},
                {"AWAITING_PAYMENT", "Ожидает оплаты"}, {"PAID", "Оплачен"},
                {"READY_FOR_PICKUP", "Автомобиль готов к выдаче"}, {"COMPLETED", "Завершён"},
                {"CANCELLED", "Отменён"}, {"STOCK_CONFIRMED", "Согласован складом"},
                {"AWAITING_DELIVERY", "Ожидает доставки"}
        };
        for (String[] s : statuses) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM order_statuses WHERE name = ?", Integer.class, s[0]);
            if (count != null && count == 0) {
                jdbcTemplate.update(
                        "INSERT INTO order_statuses (id, name, display_name, created_at, updated_at, removed) " +
                                "VALUES (gen_random_uuid(), ?, ?, NOW(), NOW(), false)", s[0], s[1]);
            }
        }
    }

    private void ensureOrderTypes() {
        String[][] types = {
                {"IN_STOCK", "Заказ на автомобиль в наличии"},
                {"CUSTOM", "Заказ на автомобиль с конфигурацией"}
        };
        for (String[] t : types) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM order_types WHERE name = ?", Integer.class, t[0]);
            if (count != null && count == 0) {
                jdbcTemplate.update(
                        "INSERT INTO order_types (id, name, display_name, created_at, updated_at, removed) " +
                                "VALUES (gen_random_uuid(), ?, ?, NOW(), NOW(), false)", t[0], t[1]);
            }
        }
    }

    private void ensurePaymentMethods() {
        String[][] methods = {
                {"CASH", "Наличные"}, {"CARD", "Банковская карта"},
                {"ONLINE", "Онлайн-оплата"}, {"INSTALLMENT", "Рассрочка"}
        };
        for (String[] m : methods) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM payment_methods WHERE name = ?", Integer.class, m[0]);
            if (count != null && count == 0) {
                jdbcTemplate.update(
                        "INSERT INTO payment_methods (id, name, display_name, created_at, updated_at, removed) " +
                                "VALUES (gen_random_uuid(), ?, ?, NOW(), NOW(), false)", m[0], m[1]);
            }
        }
    }

    private void ensurePaymentStatuses() {
        String[][] statuses = {
                {"PENDING", "Ожидает оплаты"}, {"PROCESSING", "Обрабатывается"},
                {"COMPLETED", "Оплачен"}, {"FAILED", "Ошибка оплаты"},
                {"REFUNDED", "Возврат"}
        };
        for (String[] s : statuses) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM payment_statuses WHERE name = ?", Integer.class, s[0]);
            if (count != null && count == 0) {
                jdbcTemplate.update(
                        "INSERT INTO payment_statuses (id, name, display_name, created_at, updated_at, removed) " +
                                "VALUES (gen_random_uuid(), ?, ?, NOW(), NOW(), false)", s[0], s[1]);
            }
        }
    }

    private void ensureTestDriveStatuses() {
        String[][] statuses = {
                {"PENDING", "Ожидает подтверждения"}, {"CONFIRMED", "Подтверждён"},
                {"COMPLETED", "Проведён"}, {"CANCELLED", "Отменён"},
                {"NO_SHOW", "Клиент не пришёл"}
        };
        for (String[] s : statuses) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM test_drive_statuses WHERE name = ?", Integer.class, s[0]);
            if (count != null && count == 0) {
                jdbcTemplate.update(
                        "INSERT INTO test_drive_statuses (id, name, display_name, created_at, updated_at, removed) " +
                                "VALUES (gen_random_uuid(), ?, ?, NOW(), NOW(), false)", s[0], s[1]);
            }
        }
    }

    private void ensureManagerPositions() {
        Object[][] positions = {
                {"SALES_MANAGER", "Менеджер по продажам", 10, 5},
                {"SENIOR_MANAGER", "Старший менеджер", 15, 10}
        };
        for (Object[] p : positions) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM manager_positions WHERE name = ?", Integer.class, p[0]);
            if (count != null && count == 0) {
                jdbcTemplate.update(
                        "INSERT INTO manager_positions (id, name, display_name, max_concurrent_orders, max_concurrent_test_drives, created_at, updated_at, removed) " +
                                "VALUES (gen_random_uuid(), ?, ?, ?, ?, NOW(), NOW(), false)",
                        p[0], p[1], p[2], p[3]);
            }
        }
    }

    private void ensureAdminLevels() {
        Object[][] levels = {
                {"SUPER_ADMIN", "Супер администратор", 100},
                {"ADMIN", "Администратор", 50}
        };
        for (Object[] l : levels) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM admin_levels WHERE name = ?", Integer.class, l[0]);
            if (count != null && count == 0) {
                jdbcTemplate.update(
                        "INSERT INTO admin_levels (id, name, display_name, level, created_at, updated_at, removed) " +
                                "VALUES (gen_random_uuid(), ?, ?, ?, NOW(), NOW(), false)", l[0], l[1], l[2]);
            }
        }
    }

    private static final PostgreSQLContainer<?> postgres;

    static {
        postgres = new PostgreSQLContainer<>("postgres:15")
                .withDatabaseName("testdb_order")
                .withUsername("test")
                .withPassword("test");
        postgres.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.liquibase.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("server.port", () -> "0");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.datasource.hikari.maximumPoolSize", () -> "10");
        registry.add("spring.datasource.hikari.connectionTimeout", () -> "30000");
        registry.add("grpc.client.storage-service.address", () -> "static://localhost:9999");
        registry.add("spring.kafka.bootstrap-servers", () -> "localhost:9999");
    }
}
