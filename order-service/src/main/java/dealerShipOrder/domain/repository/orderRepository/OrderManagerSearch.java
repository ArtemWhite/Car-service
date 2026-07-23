package dealerShipOrder.domain.repository.orderRepository;

import dealerShipOrder.domain.models.order.OrderStatus;
import dealerShipOrder.domain.models.order.Order;

import java.util.List;

public interface OrderManagerSearch {
    List<Order> findByManagerId(String managerId);
    List<Order> findByManagerIdAndStatus(String managerId, OrderStatus status);
    long countByManagerId(String managerId);
}
