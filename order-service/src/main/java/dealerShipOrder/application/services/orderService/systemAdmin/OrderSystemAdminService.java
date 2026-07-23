package dealerShipOrder.application.services.orderService.systemAdmin;

import dealerShipOrder.application.dtos.request.orderRequest.UpdateOrderRequest;
import dealerShipOrder.application.dtos.response.orderResponse.OrderResponse;

import java.util.List;

public interface OrderSystemAdminService {
    OrderResponse updateOrder(String orderId, UpdateOrderRequest request);
    void deleteOrder(String orderId, String reason);
    List<OrderResponse> getAllOrdersForAdmin();
}