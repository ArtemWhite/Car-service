package dealerShipOrder.domain.repository.orderRepository;

import dealerShipOrder.domain.models.order.Order;
import dealerShipOrder.domain.repository.BaseRepository;

public interface OrderRepository extends
        BaseRepository<Order>,
        OrderClientSearch,
        OrderManagerSearch,
        OrderStatusSearch,
        OrderDateSearch,
        OrderSpecificSearch,
        OrderFilterSearch{

}