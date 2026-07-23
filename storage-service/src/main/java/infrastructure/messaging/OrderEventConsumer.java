package infrastructure.messaging;

import application.services.assemblyService.AssemblyOrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import events.OrderSentForApprovalEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final ObjectMapper objectMapper;
    private final AssemblyOrderService assemblyOrderService;

    @KafkaListener(
            topics = "${app.kafka.topic.order-events:order-events}",
            groupId = "${spring.kafka.consumer.group-id:storage-service-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeOrderSentForApproval(String message, Acknowledgment acknowledgment) {
        try {
            log.info("Received message from order-events topic: {}", message);

            OrderSentForApprovalEvent event = objectMapper.readValue(
                    message,
                    OrderSentForApprovalEvent.class
            );

            log.info("Processing OrderSentForApprovalEvent for orderId: {}, type: {}",
                    event.getOrderId(), event.getOrderType());

            assemblyOrderService.processOrderApproval(event);

            acknowledgment.acknowledge();

            log.info("Successfully processed OrderSentForApprovalEvent for orderId: {}", event.getOrderId());

        } catch (Exception e) {
            log.error("Failed to process OrderSentForApprovalEvent", e);
            throw new RuntimeException("Failed to process event", e);
        }
    }
}