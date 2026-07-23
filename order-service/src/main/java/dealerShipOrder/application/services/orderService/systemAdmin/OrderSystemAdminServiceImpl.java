package dealerShipOrder.application.services.orderService.systemAdmin;

import dealerShipOrder.application.dtos.request.orderRequest.UpdateOrderRequest;
import dealerShipOrder.application.dtos.response.orderResponse.OrderResponse;
import dealerShipOrder.application.mapper.OrderMapper;
import dealerShipOrder.application.services.orderService.BaseOrderService;
import dealerShipOrder.domain.repository.usersRepository.userRepository.UserRepository;
import domain.exception.DomainValidationException;
import dealerShipOrder.domain.models.order.Order;
import dealerShipOrder.domain.models.users.User;
import dealerShipOrder.domain.models.users.systemAdmin.SystemAdmin;
import dealerShipOrder.domain.models.users.systemAdmin.SystemPermission;
import domain.repository.carRepository.CarRepository;
import dealerShipOrder.domain.repository.orderRepository.OrderRepository;
import infrastructure.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class OrderSystemAdminServiceImpl extends BaseOrderService implements OrderSystemAdminService
{
    private final OrderMapper orderMapper;
    private final CarRepository carRepository;

    public OrderSystemAdminServiceImpl(
            OrderRepository orderRepository,
            UserRepository userRepository,
            OrderMapper orderMapper,
            CarRepository carRepository) {
        super(orderRepository, userRepository);
        this.orderMapper = orderMapper;
        this.carRepository = carRepository;
    }

    @Override
    public OrderResponse updateOrder(String orderId, UpdateOrderRequest request) {
        String adminId = SecurityUtils.getCurrentUserId();
        User user = findUserById(adminId);
        if (!(user instanceof SystemAdmin admin)) {
            throw new DomainValidationException("User is not a system admin");
        }

        admin.checkPermission(SystemPermission.UPDATE_ORDER);

        Order order = findOrderById(orderId);
        String oldStatus = order.getStatus().name();

        if (request.getStatus() != null) {
            orderMapper.updateOrderStatus(order, request.getStatus(), request.getCancelReason());
        }

        if (request.getNotes() != null) {
            order.setNotes(request.getNotes());
        }

        Order updated = saveOrder(order);

        admin.logAction("UPDATE_ORDER", "Updated order: " + orderId + " from " + oldStatus + " to " + updated.getStatus());
        userRepository.save(admin);

        return orderMapper.toResponse(updated);
    }

    @Override
    public void deleteOrder(String orderId, String reason) {
        String adminId = SecurityUtils.getCurrentUserId();
        User user = findUserById(adminId);
        if (!(user instanceof SystemAdmin admin)) {
            throw new DomainValidationException("User is not a system admin");
        }

        admin.checkPermission(SystemPermission.DELETE_USER);

        Order order = findOrderById(orderId);

        if (order.isActive()) {
            throw new DomainValidationException("Cannot delete active order");
        }

        orderRepository.delete(orderId);

        admin.logAction("DELETE_ORDER", "Deleted order: " + orderId + ". Reason: " + reason);
        userRepository.save(admin);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrdersForAdmin() {
        String adminId = SecurityUtils.getCurrentUserId();
        User user = findUserById(adminId);
        if (!(user instanceof SystemAdmin admin)) {
            throw new DomainValidationException("User is not a system admin");
        }

        admin.checkPermission(SystemPermission.VIEW_ORDERS);

        return orderMapper.toResponseList(orderRepository.findAll());
    }
}