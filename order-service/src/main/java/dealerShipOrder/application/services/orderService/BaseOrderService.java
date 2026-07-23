package dealerShipOrder.application.services.orderService;

import dealerShipOrder.domain.repository.usersRepository.userRepository.UserRepository;
import dealerShipOrder.domain.models.expection.EntityNotFoundException;
import dealerShipOrder.domain.models.order.Order;
import dealerShipOrder.domain.models.users.User;
import dealerShipOrder.domain.repository.orderRepository.OrderRepository;

public abstract class BaseOrderService
{
    protected final OrderRepository orderRepository;
    protected final UserRepository userRepository;

    public BaseOrderService(OrderRepository orderRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    protected Order findOrderById(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));
    }

    protected User findUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
    }

    protected Order saveOrder(Order order) {
        return orderRepository.save(order);
    }
}