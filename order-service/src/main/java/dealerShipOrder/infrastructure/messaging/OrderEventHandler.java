package dealerShipOrder.infrastructure.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dealerShipOrder.domain.models.order.Order;
import dealerShipOrder.domain.repository.orderRepository.OrderRepository;
import events.OrderApprovedEvent;
import events.OrderRejectedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventHandler {

    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "storage-events",
            groupId = "order-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handleStorageEvent(String message, Acknowledgment acknowledgment) {
        try {
            log.info("Received message from storage-events: {}", message);

            JsonNode jsonNode = objectMapper.readTree(message);
            String eventType = jsonNode.has("eventType") ? jsonNode.get("eventType").asText() : null;

            if ("ORDER_APPROVED".equals(eventType)) {
                OrderApprovedEvent event = objectMapper.treeToValue(jsonNode, OrderApprovedEvent.class);
                handleOrderApproved(event);
            }
            else if ("ORDER_REJECTED".equals(eventType)) {
                OrderRejectedEvent event = objectMapper.treeToValue(jsonNode, OrderRejectedEvent.class);
                handleOrderRejected(event);
            }
            else {
                log.warn("Unknown event type '{}' in message", eventType);
            }

            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Failed to handle storage event", e);
        }
    }

    private void handleOrderApproved(OrderApprovedEvent event) {
        log.info("Order {} approved by storage. AssemblyOrderId: {}",
                event.getOrderId(), event.getAssemblyOrderId());

        Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found: " + event.getOrderId()));

        order.markAsReadyForPickup();
        orderRepository.save(order);

        log.info("Order {} status updated to READY_FOR_PICKUP", event.getOrderId());
    }

    private void handleOrderRejected(OrderRejectedEvent event) {
        log.warn("Order {} rejected by storage. Reason: {}",
                event.getOrderId(), event.getReason());

        Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found: " + event.getOrderId()));

        order.cancel(event.getReason());
        orderRepository.save(order);

        log.info("Order {} status updated to CANCELLED", event.getOrderId());
    }
}