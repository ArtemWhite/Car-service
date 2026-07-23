package dealerShipOrder.application.services.orderService.manager;

import dealerShipOrder.application.dtos.response.orderResponse.OrderResponse;

import java.util.List;

public interface OrderManagerService {
    void assignManager(String orderId);
    List<OrderResponse> getMyOrders();
    List<OrderResponse> getPendingOrders();
    void confirmOrder(String orderId);
}