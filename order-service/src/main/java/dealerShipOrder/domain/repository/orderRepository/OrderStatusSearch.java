package dealerShipOrder.domain.repository.orderRepository;

import dealerShipOrder.domain.models.order.OrderStatus;
import dealerShipOrder.domain.models.order.Order;

import java.util.List;

public interface OrderStatusSearch {
    List<Order> findByStatus(OrderStatus status);
    List<Order> findByStatusIn(List<OrderStatus> statuses);
    long countByStatus(OrderStatus status);
    List<Order> findActiveOrders();
    long countByOrderType(String orderType);
}
