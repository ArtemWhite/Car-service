package dealerShipOrder.application.mapper;

import dealerShipOrder.application.dtos.request.orderRequest.CreateOrderRequest;
import dealerShipOrder.application.dtos.response.orderResponse.OrderHistoryEntryDto;
import dealerShipOrder.application.dtos.response.orderResponse.OrderResponse;
import dealerShipOrder.domain.models.order.Order;
import dealerShipOrder.domain.models.order.OrderHistoryEntry;
import domain.exception.EntityNotFoundException;
import domain.models.car.Car;
import domain.repository.carRepository.CarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderMapper {

    private final CarRepository carRepository;
    public Order toDomain(CreateOrderRequest request) {
        if (request.getOrderType().equals("IN_STOCK")) {
            return Order.createInStockOrder(
                    UUID.randomUUID().toString(),
                    request.getClientId(),
                    request.getCarId()
            );
        } else {
            return Order.createCustomOrder(
                    UUID.randomUUID().toString(),
                    request.getClientId(),
                    request.getConfigurationId(),
                    request.getCarModelId()
            );
        }
    }

    public OrderResponse toResponse(Order order) {
        OrderResponse response = new OrderResponse();

        response.setId(order.getId());
        response.setClientId(order.getClientId());

        if (order.getType() != null) {
            response.setOrderType(order.getType().name());
            response.setOrderTypeDisplayName(order.getType().getDisplayName());
        }

        if (order.getStatus() != null) {
            response.setStatus(order.getStatus().name());
            response.setStatusDisplayName(order.getStatus().getDisplayName());
        }

        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        response.setCompletedAt(order.getCompletedAt());
        response.setNotes(order.getNotes());
        response.setActive(order.isActive());

        if (order.getManagerId() != null) {
            response.setManagerId(order.getManagerId());
        }

        if (order.isInStockOrder() && order.getCarId() != null) {
            response.setCarId(order.getCarId());
            response.setCarInfo("Автомобиль в наличии");
        } else if (order.isCustomOrder()) {
            response.setConfigurationId(order.getConfigurationId());
            response.setCarModelId(order.getCarModelId());
            response.setCarModelName("Модель с конфигурацией");
        }

        response.setHistory(toHistoryDtoList(order.getHistory()));

        return response;
    }

    public List<OrderResponse> toResponseList(List<Order> orders) {
        if (orders == null) {
            return List.of();
        }
        return orders.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public OrderHistoryEntryDto toHistoryDto(OrderHistoryEntry entry) {
        if (entry == null) return null;
        OrderHistoryEntryDto dto = new OrderHistoryEntryDto();
        dto.setAction(entry.getAction());
        dto.setDescription(entry.getDescription());
        dto.setTimestamp(entry.getTimestamp());
        return dto;
    }

    public List<OrderHistoryEntryDto> toHistoryDtoList(List<OrderHistoryEntry> history) {
        if (history == null) return List.of();
        return history.stream()
                .map(this::toHistoryDto)
                .collect(Collectors.toList());
    }

    public void updateOrderStatus(Order order, String status, String reason) {
        System.out.println("=== MAPPER CALLED ===");
        System.out.println("Received status: '" + status + "'");
        switch (status) {
            case "AWAITING_PAYMENT":
                order.awaitPayment();
                break;
            case "PAID":
                order.markAsPaid();
                break;
            case "READY_FOR_PICKUP":
                order.markAsReadyForPickup();
                break;
            case "COMPLETED":
                order.markAsCompleted();
                if (order.isInStockOrder() && order.getCarId() != null) {
                    Car car = carRepository.findById(order.getCarId())
                            .orElseThrow(() -> new EntityNotFoundException("Car not found: " + order.getCarId()));
                    System.out.println("=== CAR STATUS BEFORE MARK_AS_SOLD: " + car.getCarStatus());
                    car.markAsSold();
                    carRepository.save(car);
                }
                break;
            case "CANCELLED":
                order.cancel(reason);
                break;
            case "AWAITING_DELIVERY":
                order.waitForDelivery();
                break;
            case "STOCK_CONFIRMED":
                order.confirmByStock();
                break;
            case "DELIVERED":
                order.markAsDelivered();
                break;
            default:
                break;
        }
    }
}