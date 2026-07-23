package dealerShipOrder.domain.repository.orderRepository;

import dealerShipOrder.domain.models.order.Order;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderDateSearch {
    List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    List<Order> findByCompletedAtBetween(LocalDateTime start, LocalDateTime end);
}

