package dealerShipOrder.application.services.orderService.client;

import dealerShipOrder.application.dtos.request.orderRequest.CreateOrderRequest;
import dealerShipOrder.application.dtos.response.orderResponse.OrderResponse;

import java.util.List;

public interface OrderClientService {
    OrderResponse createOrder(CreateOrderRequest request);
    List<OrderResponse> getMyOrders();
    void cancelOrder(String orderId, String reason);
}