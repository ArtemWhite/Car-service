package dealerShipOrder.application.services.orderService.manager;

import dealerShipOrder.application.dtos.response.orderResponse.OrderResponse;
import dealerShipOrder.application.mapper.OrderMapper;
import dealerShipOrder.application.services.orderService.BaseOrderService;
import dealerShipOrder.domain.repository.usersRepository.userRepository.UserRepository;
import domain.exception.DomainValidationException;
import dealerShipOrder.domain.models.order.Order;
import dealerShipOrder.domain.models.order.OrderStatus;
import dealerShipOrder.domain.models.users.User;
import dealerShipOrder.domain.models.users.manager.Manager;
import dealerShipOrder.domain.repository.orderRepository.OrderRepository;
import infrastructure.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderManagerServiceImpl extends BaseOrderService implements OrderManagerService
{
    private final OrderMapper orderMapper;

    public OrderManagerServiceImpl(
            OrderRepository orderRepository,
            UserRepository userRepository,
            OrderMapper orderMapper) {
        super(orderRepository, userRepository);
        this.orderMapper = orderMapper;
    }

    @Override
    @Transactional
    public void assignManager(String orderId) {
        String managerId = SecurityUtils.getCurrentUserId();
        User user = findUserById(managerId);
        if (!(user instanceof Manager manager)) {
            throw new DomainValidationException("User is not a manager");
        }

        Order order = findOrderById(orderId);
        order.assignManager(managerId);
        manager.assignOrder(orderId);

        saveOrder(order);
        userRepository.save(manager);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders() {
        String managerId = SecurityUtils.getCurrentUserId();
        return orderMapper.toResponseList(
                orderRepository.findByManagerId(managerId)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getPendingOrders() {
        return orderMapper.toResponseList(
                orderRepository.findByStatus(OrderStatus.CREATED)
        );
    }

    @Override
    @Transactional
    public void confirmOrder(String orderId) {
        String managerId = SecurityUtils.getCurrentUserId();
        Order order = findOrderById(orderId);

        if (order.getManagerId() == null) {
            order.assignManager(managerId);
        }

        if (order.isCustomOrder()) {
            order.confirmByStock();
        } else if (order.isInStockOrder()) {
            order.awaitPayment();
        }

        saveOrder(order);
    }
}