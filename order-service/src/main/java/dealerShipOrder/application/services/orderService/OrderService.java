package dealerShipOrder.application.services.orderService;


import dealerShipOrder.application.dtos.request.orderRequest.OrderFilterRequest;
import dealerShipOrder.application.dtos.response.orderResponse.OrderResponse;

import java.util.List;

public interface OrderService
{
    OrderResponse getOrderById(String id);
    List<OrderResponse> getAllOrders();
    List<OrderResponse> getOrdersWithFilters(OrderFilterRequest filter);
}