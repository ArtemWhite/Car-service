package dealerShipOrder.application.services.orderService.client;

import dealerShipOrder.application.dtos.request.orderRequest.CreateOrderRequest;
import dealerShipOrder.application.dtos.response.orderResponse.OrderResponse;
import dealerShipOrder.application.mapper.OrderMapper;
import dealerShipOrder.application.services.orderService.BaseOrderService;
import dealerShipOrder.domain.repository.usersRepository.userRepository.UserRepository;
import dealerShipOrder.domain.models.expection.DomainValidationException;
import dealerShipOrder.domain.models.expection.EntityNotFoundException;
import domain.models.car.Car;
import dealerShipOrder.domain.models.order.Order;
import dealerShipOrder.domain.models.users.User;
import dealerShipOrder.domain.models.users.client.Client;
import domain.repository.carRepository.CarRepository;
import dealerShipOrder.domain.repository.orderRepository.OrderRepository;
import dealerShipOrder.infrastructure.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class OrderClientServiceImpl extends BaseOrderService implements OrderClientService
{
    private final OrderMapper orderMapper;
    private final CarRepository carRepository;

    public OrderClientServiceImpl(
            OrderRepository orderRepository,
            UserRepository userRepository,
            OrderMapper orderMapper,
            CarRepository carRepository) {
        super(orderRepository, userRepository);
        this.orderMapper = orderMapper;
        this.carRepository = carRepository;
    }

    @Override
    public OrderResponse createOrder(CreateOrderRequest request) {
        String clientId = SecurityUtils.getCurrentUserId();
        User user = findUserById(clientId);
        if (!(user instanceof Client client)) {
            throw new DomainValidationException("User is not a client");
        }

        request.setClientId(clientId);

        if ("IN_STOCK".equals(request.getOrderType())) {
            if (request.getCarId() == null || request.getCarId().isBlank()) {
                throw new DomainValidationException("Car ID required for in-stock order");
            }
        }

        if ("CUSTOM".equals(request.getOrderType())) {
            if (request.getConfigurationId() == null || request.getConfigurationId().isBlank()) {
                throw new DomainValidationException("Configuration ID is required for CUSTOM order");
            }
            if (request.getCarModelId() == null || request.getCarModelId().isBlank()) {
                throw new DomainValidationException("Car model ID is required for CUSTOM order");
            }
        }

        if ("IN_STOCK".equals(request.getOrderType())) {
            Car car = carRepository.findById(request.getCarId())
                    .orElseThrow(() -> new EntityNotFoundException("Car not found: " + request.getCarId()));

            if (!car.isAvailableForPurchase()) {
                throw new DomainValidationException("Car is not available for purchase");
            }

            car.reserve();
            carRepository.save(car);
        }

        Order order = orderMapper.toDomain(request);
        Order saved = saveOrder(order);

        client.addOrder(saved.getId());
        userRepository.save(client);

        return orderMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders() {
        String clientId = SecurityUtils.getCurrentUserId();
        findUserById(clientId);
        return orderMapper.toResponseList(
                orderRepository.findByClientId(clientId)
        );
    }

    @Override
    public void cancelOrder(String orderId, String reason) {
        String clientId = SecurityUtils.getCurrentUserId();
        Order order = findOrderById(orderId);

        if (!order.getClientId().equals(clientId)) {
            throw new DomainValidationException("Order does not belong to this client");
        }

        if (order.isInStockOrder() && order.getCarId() != null) {
            Car car = carRepository.findById(order.getCarId())
                    .orElseThrow(() -> new EntityNotFoundException("Car not found: " + order.getCarId()));
            car.markAsAvailable();
            carRepository.save(car);
        }

        order.cancel(reason);
        saveOrder(order);
    }
}