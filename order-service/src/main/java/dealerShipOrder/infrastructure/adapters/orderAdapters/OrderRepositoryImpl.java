package dealerShipOrder.infrastructure.adapters.orderAdapters;

import dealerShipOrder.domain.models.order.Order;
import dealerShipOrder.domain.models.order.OrderStatus;
import dealerShipOrder.domain.models.order.OrderType;
import dealerShipOrder.domain.repository.orderRepository.OrderRepository;
import dealerShipOrder.infrastructure.adapters.orderAdapters.orderReferencesAdapters.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderBaseRepositoryAdapter baseAdapter;
    private final OrderClientAdapter clientAdapter;
    private final OrderManagerAdapter managerAdapter;
    private final OrderStatusAdapter statusAdapter;
    private final OrderDateAdapter dateAdapter;
    private final OrderSpecificAdapter specificAdapter;
    private final OrderFilterAdapter filterAdapter;

    @Override
    public Order save(Order order) { return baseAdapter.save(order); }
    @Override
    public Optional<Order> findById(String id) { return baseAdapter.findById(id); }
    @Override
    public List<Order> findAll() { return baseAdapter.findAll(); }
    @Override
    public void delete(String id) { baseAdapter.delete(id); }
    @Override
    public boolean existsById(String id) { return baseAdapter.existsById(id); }

    @Override
    public List<Order> findByClientId(String clientId) { return clientAdapter.findByClientId(clientId); }
    @Override
    public List<Order> findByClientIdAndStatus(String clientId, OrderStatus status) {
        return clientAdapter.findByClientIdAndStatus(clientId, status);
    }
    @Override
    public long countByClientId(String clientId) { return clientAdapter.countByClientId(clientId); }

    @Override
    public List<Order> findByManagerId(String managerId) { return managerAdapter.findByManagerId(managerId); }
    @Override
    public List<Order> findByManagerIdAndStatus(String managerId, OrderStatus status) {
        return managerAdapter.findByManagerIdAndStatus(managerId, status);
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) { return statusAdapter.findByStatus(status); }
    @Override
    public List<Order> findByStatusIn(List<OrderStatus> statuses) { return statusAdapter.findByStatusIn(statuses); }
    @Override
    public long countByStatus(OrderStatus status) { return statusAdapter.countByStatus(status); }
    @Override
    public List<Order> findActiveOrders() { return statusAdapter.findActiveOrders(); }

    @Override
    public List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end) {
        return dateAdapter.findByCreatedAtBetween(start, end);
    }
    @Override
    public List<Order> findByCompletedAtBetween(LocalDateTime start, LocalDateTime end) {
        return dateAdapter.findByCompletedAtBetween(start, end);
    }

    @Override
    public Optional<Order> findByCarId(String carId) { return specificAdapter.findByCarId(carId); }
    @Override
    public List<Order> findByCarModelId(String carModelId) { return specificAdapter.findByCarModelId(carModelId); }
    @Override
    public List<Order> findByConfigurationId(String configurationId) { return specificAdapter.findByConfigurationId(configurationId); }
    @Override
    public boolean existsActiveOrderForCar(String carId) { return specificAdapter.existsActiveOrderForCar(carId); }
    @Override
    public List<Order> findOrdersWithFilters(OrderStatus status, OrderType type,
                                             LocalDateTime dateFrom, LocalDateTime dateTo,
                                             String clientId, String managerId) {
        return filterAdapter.findOrdersWithFilters(status, type, dateFrom, dateTo, clientId, managerId);
    }

    @Override
    public List<Order> findByOrderType(String orderType) {
        return specificAdapter.findByOrderType(orderType);
    }

    @Override
    public long countByManagerId(String managerId) {
        return managerAdapter.countByManagerId(managerId);
    }

    @Override
    public long countByOrderType(String orderType) {
        return statusAdapter.countByOrderType(orderType);
    }
}