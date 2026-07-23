package dealerShipOrder.domain.repository.orderRepository;

import dealerShipOrder.domain.models.order.OrderStatus;
import dealerShipOrder.domain.models.order.Order;
import dealerShipOrder.domain.models.order.OrderType;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderFilterSearch {
    List<Order> findOrdersWithFilters(
            OrderStatus status,
            OrderType type,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            String clientId,
            String managerId
    );
}