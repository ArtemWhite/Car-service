package dealerShipOrder.application.services.orderService;

import dealerShipOrder.application.dtos.request.orderRequest.OrderFilterRequest;
import dealerShipOrder.application.dtos.response.orderResponse.OrderResponse;
import dealerShipOrder.application.mapper.OrderMapper;
import dealerShipOrder.domain.models.order.Order;
import dealerShipOrder.domain.models.order.OrderStatus;
import dealerShipOrder.domain.models.order.OrderType;
import dealerShipOrder.domain.repository.orderRepository.OrderRepository;
import dealerShipOrder.domain.repository.usersRepository.userRepository.UserRepository;
import dealerShipOrder.infrastructure.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class OrderServiceImpl extends BaseOrderService implements OrderService {

    private final OrderMapper orderMapper;

    public OrderServiceImpl(OrderRepository orderRepository,
                            UserRepository userRepository,
                            OrderMapper orderMapper) {
        super(orderRepository, userRepository);
        this.orderMapper = orderMapper;
    }

    @Override
    public OrderResponse getOrderById(String id) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        Order order = findOrderById(id);

        boolean isAdminOrManager = SecurityUtils.hasRole("SYSTEM_ADMIN") || SecurityUtils.hasRole("MANAGER");
        if (!isAdminOrManager && !order.getClientId().equals(currentUserId)) {
            throw new SecurityException("Access denied: you can only view your own orders");
        }

        return orderMapper.toResponse(order);
    }

    @Override
    public List<OrderResponse> getAllOrders() {
        String currentUserId = SecurityUtils.getCurrentUserId();
        boolean isAdminOrManager = SecurityUtils.hasRole("SYSTEM_ADMIN") || SecurityUtils.hasRole("MANAGER");

        List<Order> orders = orderRepository.findAll();

        if (!isAdminOrManager) {
            orders = orders.stream()
                    .filter(order -> order.getClientId().equals(currentUserId))
                    .collect(Collectors.toList());
        }

        return orderMapper.toResponseList(orders);
    }

    @Override
    public List<OrderResponse> getOrdersWithFilters(OrderFilterRequest filter) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        boolean isAdminOrManager = SecurityUtils.hasRole("SYSTEM_ADMIN") || SecurityUtils.hasRole("MANAGER");

        if (!isAdminOrManager) {
            filter.setClientId(currentUserId);
        } else {
            filter.setClientId(null);
        }

        List<Order> orders = orderRepository.findAll();

        List<Order> filteredOrders = orders.stream()
                .filter(order -> filterByStatus(order, filter.getStatus()))
                .filter(order -> filterByType(order, filter.getOrderType()))
                .filter(order -> filterByDate(order, filter.getDateFrom(), filter.getDateTo()))
                .filter(order -> filterByClientId(order, filter.getClientId()))
                .filter(order -> filterByManagerId(order, filter.getManagerId()))
                .collect(Collectors.toList());

        filteredOrders = sortOrders(filteredOrders, filter.getSortBy(), filter.getSortDirection());

        int page = filter.getPage() != null ? filter.getPage() : 0;
        int size = filter.getSize() != null ? filter.getSize() : 20;

        int start = page * size;
        int end = Math.min(start + size, filteredOrders.size());

        if (start < filteredOrders.size()) {
            filteredOrders = filteredOrders.subList(start, end);
        } else {
            filteredOrders = List.of();
        }

        return orderMapper.toResponseList(filteredOrders);
    }

    private List<Order> sortOrders(List<Order> orders, String sortBy, String sortDirection) {
        if (sortBy == null || sortBy.isBlank()) {
            sortBy = "createdAt";
        }

        Comparator<Order> comparator = switch (sortBy) {
            case "updatedAt" -> Comparator.comparing(Order::getUpdatedAt,
                    Comparator.nullsLast(Comparator.naturalOrder()));
            case "completedAt" -> Comparator.comparing(Order::getCompletedAt,
                    Comparator.nullsLast(Comparator.naturalOrder()));
            case "clientId" -> Comparator.comparing(Order::getClientId,
                    Comparator.nullsLast(Comparator.naturalOrder()));
            default -> Comparator.comparing(Order::getCreatedAt,
                    Comparator.nullsLast(Comparator.naturalOrder()));
        };

        if ("DESC".equalsIgnoreCase(sortDirection)) {
            comparator = comparator.reversed();
        }

        return orders.stream().sorted(comparator).collect(Collectors.toList());
    }

    private boolean filterByStatus(Order order, String status) {
        if (status == null || status.isBlank()) {
            return true;
        }
        OrderStatus orderStatus = order.getStatus();
        if (orderStatus == null) {
            return false;
        }
        return orderStatus.name().equals(status);
    }

    private boolean filterByType(Order order, String type) {
        if (type == null || type.isBlank()) {
            return true;
        }
        if (order.getType() == null) {
            return false;
        }
        try {
            OrderType orderType = OrderType.valueOf(type);
            return order.getType() == orderType;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private boolean filterByDate(Order order, LocalDateTime fromDate, LocalDateTime toDate) {
        LocalDateTime createdAt = order.getCreatedAt();
        if (createdAt == null) {
            return false;
        }
        if (fromDate != null && createdAt.isBefore(fromDate)) {
            return false;
        }
        if (toDate != null && createdAt.isAfter(toDate)) {
            return false;
        }
        return true;
    }

    private boolean filterByClientId(Order order, String clientId) {
        if (clientId == null || clientId.isBlank()) {
            return true;
        }
        String orderClientId = order.getClientId();
        return orderClientId != null && orderClientId.equals(clientId);
    }

    private boolean filterByManagerId(Order order, String managerId) {
        if (managerId == null || managerId.isBlank()) {
            return true;
        }
        String orderManagerId = order.getManagerId();
        return orderManagerId != null && orderManagerId.equals(managerId);
    }
}