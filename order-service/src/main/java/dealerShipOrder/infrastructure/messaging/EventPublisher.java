package dealerShipOrder.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dealerShipOrder.domain.models.order.Order;
import dealerShipOrder.infrastructure.entities.OutboxEventEntity;
import dealerShipOrder.infrastructure.jpaRepository.OutboxJpaRepository;
import events.OrderSentForApprovalEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisher {

    private final OutboxJpaRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void publishOrderSentForApproval(Order order, Double amount) {
        try {
            String traceId = UUID.randomUUID().toString();

            OrderSentForApprovalEvent event = new OrderSentForApprovalEvent(
                    order.getId(),
                    traceId,
                    order.getType().name(),
                    order.getCarId(),
                    order.getConfigurationId(),
                    order.getCarModelId(),
                    order.getClientId(),
                    amount
            );

            String payload = objectMapper.writeValueAsString(event);

            OutboxEventEntity outboxEvent = new OutboxEventEntity();
            outboxEvent.setEventId(event.getEventId());
            outboxEvent.setEventType(OrderSentForApprovalEvent.TYPE);
            outboxEvent.setAggregateId(order.getId());
            outboxEvent.setPayload(payload);
            outboxEvent.setTraceId(traceId);
            outboxEvent.setCreatedAt(Instant.now());
            outboxEvent.setStatus(OutboxEventEntity.OutboxStatus.PENDING);
            outboxEvent.setRetryCount(0);

            outboxRepository.save(outboxEvent);

            log.info("OrderSentForApprovalEvent saved to outbox for order: {}, eventId: {}",
                    order.getId(), event.getEventId());

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize OrderSentForApprovalEvent for order: {}", order.getId(), e);
            throw new RuntimeException("Failed to create outbox event", e);
        }
    }
}