package dealerShipOrder.domain.repository.orderRepository;

import dealerShipOrder.domain.models.order.Order;

import java.util.List;
import java.util.Optional;

public interface OrderSpecificSearch {
    Optional<Order> findByCarId(String carId);
    List<Order> findByCarModelId(String carModelId);
    List<Order> findByConfigurationId(String configurationId);
    boolean existsActiveOrderForCar(String carId);
    List<Order> findByOrderType(String orderType);
}
