package orderIntegrationTests;

import dealerShipOrder.BaseIntegrationTest;
import dealerShipOrder.domain.models.order.Order;
import dealerShipOrder.domain.models.order.OrderStatus;
import dealerShipOrder.domain.models.order.OrderType;
import dealerShipOrder.domain.repository.orderRepository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class OrderRepositoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String clientId;
    private String carId;
    private String configurationId;
    private String carModelId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM orders");
        jdbcTemplate.execute("DELETE FROM order_history_entries");

        clientId = UUID.randomUUID().toString();
        carId = UUID.randomUUID().toString();
        configurationId = UUID.randomUUID().toString();
        carModelId = UUID.randomUUID().toString();
    }

    private Order createInStockOrder() {
        return Order.createInStockOrder(
                UUID.randomUUID().toString(),
                clientId,
                carId
        );
    }

    private Order createCustomOrder() {
        return Order.createCustomOrder(
                UUID.randomUUID().toString(),
                clientId,
                configurationId,
                carModelId
        );
    }

    @Test
    @Transactional
    @Rollback
    void shouldSaveAndFindInStockOrder() {
        Order order = createInStockOrder();
        Order saved = orderRepository.save(order);
        Order found = orderRepository.findById(saved.getId()).orElse(null);

        assertThat(found).isNotNull();
        assertThat(found.getType()).isEqualTo(OrderType.IN_STOCK);
        assertThat(found.getClientId()).isEqualTo(clientId);
        assertThat(found.getCarId()).isEqualTo(carId);
        assertThat(found.getStatus()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    @Transactional
    @Rollback
    void shouldSaveAndFindCustomOrder() {
        Order order = createCustomOrder();
        Order saved = orderRepository.save(order);
        Order found = orderRepository.findById(saved.getId()).orElse(null);

        assertThat(found).isNotNull();
        assertThat(found.getType()).isEqualTo(OrderType.CUSTOM);
        assertThat(found.getClientId()).isEqualTo(clientId);
        assertThat(found.getConfigurationId()).isEqualTo(configurationId);
        assertThat(found.getCarModelId()).isEqualTo(carModelId);
        assertThat(found.getStatus()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    @Transactional
    @Rollback
    void shouldDeleteOrder() {
        Order order = createInStockOrder();
        Order saved = orderRepository.save(order);
        orderRepository.delete(saved.getId());
        assertThat(orderRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    @Transactional
    @Rollback
    void shouldFindAllOrders() {
        orderRepository.save(createInStockOrder());
        orderRepository.save(createCustomOrder());
        var allOrders = orderRepository.findAll();
        assertThat(allOrders).hasSize(2);
    }

    @Test
    @Transactional
    @Rollback
    void shouldFindByClientId() {
        Order order = createInStockOrder();
        orderRepository.save(order);
        var orders = orderRepository.findByClientId(clientId);
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getClientId()).isEqualTo(clientId);
    }

    @Test
    @Transactional
    @Rollback
    void shouldCountByClientId() {
        orderRepository.save(createInStockOrder());
        orderRepository.save(createInStockOrder());
        long count = orderRepository.countByClientId(clientId);
        assertThat(count).isEqualTo(2);
    }

    @Test
    @Transactional
    @Rollback
    void shouldFindByClientIdAndStatus() {
        Order order = createInStockOrder();
        orderRepository.save(order);
        var orders = orderRepository.findByClientIdAndStatus(clientId, OrderStatus.CREATED);
        assertThat(orders).hasSize(1);
    }

    @Test
    @Transactional
    @Rollback
    void shouldFindByCarId() {
        Order order = createInStockOrder();
        orderRepository.save(order);
        var found = orderRepository.findByCarId(carId);
        assertThat(found).isPresent();
        assertThat(found.get().getCarId()).isEqualTo(carId);
    }

    @Test
    @Transactional
    @Rollback
    void shouldFindByConfigurationId() {
        Order order = createCustomOrder();
        orderRepository.save(order);
        var orders = orderRepository.findByConfigurationId(configurationId);
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getConfigurationId()).isEqualTo(configurationId);
    }

    @Test
    @Transactional
    @Rollback
    void shouldFindByCarModelId() {
        Order order = createCustomOrder();
        orderRepository.save(order);
        var orders = orderRepository.findByCarModelId(carModelId);
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getCarModelId()).isEqualTo(carModelId);
    }

    @Test
    @Transactional
    @Rollback
    void shouldCheckActiveOrderForCar() {
        Order order = createInStockOrder();
        orderRepository.save(order);
        boolean exists = orderRepository.existsActiveOrderForCar(carId);
        assertThat(exists).isTrue();
    }

    @Test
    @Transactional
    @Rollback
    void shouldCountByStatus() {
        Order order = createInStockOrder();
        orderRepository.save(order);
        long count = orderRepository.countByStatus(OrderStatus.CREATED);
        assertThat(count).isGreaterThan(0);
    }

    @Test
    @Transactional
    @Rollback
    void shouldReturnEmptyWhenOrderNotFound() {
        var found = orderRepository.findById(UUID.randomUUID().toString());
        assertThat(found).isEmpty();
    }
}