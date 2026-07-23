package dealerShipOrder.domain.repository.orderRepository;

import dealerShipOrder.domain.models.order.OrderStatus;
import dealerShipOrder.domain.models.order.Order;

import java.util.List;

public interface OrderClientSearch {
    List<Order> findByClientId(String clientId);
    List<Order> findByClientIdAndStatus(String clientId, OrderStatus status);
    long countByClientId(String clientId);
}