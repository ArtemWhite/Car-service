package dealerShipOrder.presentation.mappers;


import dealerShipOrder.application.dtos.request.orderRequest.CreateOrderRequest;
import dealerShipOrder.application.dtos.request.orderRequest.OrderFilterRequest;
import dealerShipOrder.application.dtos.request.orderRequest.UpdateOrderRequest;
import dealerShipOrder.application.dtos.response.orderResponse.OrderHistoryEntryDto;
import dealerShipOrder.application.dtos.response.orderResponse.OrderResponse;
import dealerShipOrder.presentation.dtos.request.orderRequestPresentationDto.OrderCreatePresentationRequest;
import dealerShipOrder.presentation.dtos.request.orderRequestPresentationDto.OrderFilterPresentationRequest;
import dealerShipOrder.presentation.dtos.request.orderRequestPresentationDto.OrderUpdatePresentationRequest;
import dealerShipOrder.presentation.dtos.response.orderResponsePresentationDto.OrderHistoryEntryPresentationResponse;
import dealerShipOrder.presentation.dtos.response.orderResponsePresentationDto.OrderListPresentationResponse;
import dealerShipOrder.presentation.dtos.response.orderResponsePresentationDto.OrderPresentationResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class OrderPresentationMapper {

    public CreateOrderRequest toApplicationWithoutClientId(OrderCreatePresentationRequest request) {
        if (request == null) return null;
        CreateOrderRequest target = new CreateOrderRequest();

        target.setCarId(request.getCarId());
        target.setConfigurationId(request.getConfigurationId());
        target.setCarModelId(request.getCarModelId());
        target.setOrderType(request.getOrderType());
        target.setNotes(request.getNotes());
        return target;
    }

    public UpdateOrderRequest toApplicationWithoutUserId(OrderUpdatePresentationRequest request) {
        if (request == null) return null;
        UpdateOrderRequest target = new UpdateOrderRequest();

        target.setStatus(request.getStatus());
        target.setNotes(request.getNotes());
        target.setCancelReason(request.getCancelReason());
        return target;
    }

    public OrderFilterRequest toApplication(OrderFilterPresentationRequest request) {
        System.out.println("=== toApplication called with request: " + request);
        if (request == null) {
            System.out.println("Request is NULL, creating new OrderFilterRequest");
            return new OrderFilterRequest();
        }

        System.out.println("Request clientId: " + request.getClientId());
        System.out.println("Request status: " + request.getStatus());
        System.out.println("Request orderType: " + request.getOrderType());

        OrderFilterRequest target = new OrderFilterRequest();
        target.setClientId(request.getClientId());
        target.setManagerId(request.getManagerId());
        target.setStatus(request.getStatus());
        target.setOrderType(request.getOrderType());
        target.setDateFrom(request.getDateFrom());
        target.setDateTo(request.getDateTo());
        target.setPage(request.getPage());
        target.setSize(request.getSize());
        target.setSortBy(request.getSortBy());
        target.setSortDirection(request.getSortDirection());

        System.out.println("Target clientId: " + target.getClientId());
        return target;
    }

    public OrderPresentationResponse toPresentation(OrderResponse source) {
        if (source == null) return null;

        return OrderPresentationResponse.builder()
                .id(source.getId())
                .clientId(source.getClientId())
                .clientName(source.getClientName())
                .managerId(source.getManagerId())
                .managerName(source.getManagerName())
                .orderType(source.getOrderType())
                .orderTypeDisplayName(source.getOrderTypeDisplayName())
                .status(source.getStatus())
                .statusDisplayName(source.getStatusDisplayName())
                .createdAt(source.getCreatedAt())
                .updatedAt(source.getUpdatedAt())
                .completedAt(source.getCompletedAt())
                .carId(source.getCarId())
                .carInfo(source.getCarInfo())
                .configurationId(source.getConfigurationId())
                .configurationName(source.getConfigurationName())
                .carModelId(source.getCarModelId())
                .carModelName(source.getCarModelName())
                .history(source.getHistory() == null ? null :
                        source.getHistory().stream()
                                .map(this::toPresentation)
                                .collect(Collectors.toList()))
                .notes(source.getNotes())
                .active(source.isActive())
                .build();
    }

    public OrderHistoryEntryPresentationResponse toPresentation(OrderHistoryEntryDto source) {
        if (source == null) return null;

        return OrderHistoryEntryPresentationResponse.builder()
                .action(source.getAction())
                .description(source.getDescription())
                .timestamp(source.getTimestamp())
                .build();
    }

    public OrderListPresentationResponse toListPresentation(List<OrderResponse> source) {
        if (source == null || source.isEmpty()) {
            return OrderListPresentationResponse.builder()
                    .orders(List.of())
                    .totalCount(0)
                    .pendingCount(0)
                    .paidCount(0)
                    .completedCount(0)
                    .cancelledCount(0)
                    .totalAmount(0.0)
                    .build();
        }

        long pendingCount = source.stream()
                .filter(o -> "PENDING".equals(o.getStatus()) || "CREATED".equals(o.getStatus()))
                .count();
        long paidCount = source.stream()
                .filter(o -> "PAID".equals(o.getStatus()) || "AWAITING_PAYMENT".equals(o.getStatus()))
                .count();
        long completedCount = source.stream()
                .filter(o -> "COMPLETED".equals(o.getStatus()))
                .count();
        long cancelledCount = source.stream()
                .filter(o -> "CANCELLED".equals(o.getStatus()))
                .count();

        Double totalAmount = source.stream()
                .map(OrderResponse::getTotalAmount)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum();

        return OrderListPresentationResponse.builder()
                .orders(source.stream()
                        .map(this::toPresentation)
                        .collect(Collectors.toList()))
                .totalCount(source.size())
                .pendingCount((int) pendingCount)
                .paidCount((int) paidCount)
                .completedCount((int) completedCount)
                .cancelledCount((int) cancelledCount)
                .totalAmount(totalAmount)
                .build();
    }
}