package infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import events.OrderApprovedEvent;
import events.OrderRejectedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@Component
@RequiredArgsConstructor
public class StorageEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topic.storage-events:storage-events}")
    private String storageEventsTopic;

    @PostConstruct
    public void init() {
        log.info("StorageEventPublisher initialized. Topic: {}", storageEventsTopic);
    }

    public void publishOrderApproved(OrderApprovedEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(storageEventsTopic, event.getOrderId(), payload);
            log.info("Published OrderApprovedEvent for orderId: {}, assemblyOrderId: {}",
                    event.getOrderId(), event.getAssemblyOrderId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize OrderApprovedEvent for orderId: {}", event.getOrderId(), e);
            throw new RuntimeException("Failed to publish OrderApprovedEvent", e);
        }
    }

    public void publishOrderRejected(OrderRejectedEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(storageEventsTopic, event.getOrderId(), payload);
            log.info("Published OrderRejectedEvent for orderId: {}, reason: {}",
                    event.getOrderId(), event.getReason());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize OrderRejectedEvent for orderId: {}", event.getOrderId(), e);
            throw new RuntimeException("Failed to publish OrderRejectedEvent", e);
        }
    }
}