package dealerShipOrder.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import dealerShipOrder.infrastructure.entities.OutboxEventEntity;
import dealerShipOrder.infrastructure.jpaRepository.OutboxJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class OutboxMessageRelay {

    private final OutboxJpaRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topic.order-events:order-events}")
    private String orderEventsTopic;

    @Value("${app.outbox.batch-size:100}")
    private int batchSize;

    @PostConstruct
    public void init() {
        log.info("OutboxMessageRelay started. Topic: {}", orderEventsTopic);
        long pendingCount = outboxRepository.countPendingEvents();
        long failedCount = outboxRepository.countFailedEvents();
        log.info("Outbox status: {} pending, {} failed events", pendingCount, failedCount);
    }

    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.SECONDS)
    public void relayPendingEvents() {
        List<OutboxEventEntity> pendingEvents = outboxRepository.findPendingEvents();

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.info("Found {} pending events in outbox", pendingEvents.size());

        int processed = 0;
        int failed = 0;

        for (OutboxEventEntity event : pendingEvents) {
            if (processed >= batchSize) {
                log.info("Reached batch size limit ({}), continuing next cycle", batchSize);
                break;
            }

            boolean success = sendToKafka(event);

            if (success) {
                outboxRepository.markAsProcessed(event.getId(), Instant.now());
                processed++;
                log.debug("Event {} sent to Kafka, eventId: {}, orderId: {}",
                        event.getEventType(), event.getEventId(), event.getAggregateId());
            } else {
                outboxRepository.markAsFailed(event.getId(), "Kafka send failed");
                failed++;
                log.warn("Event {} failed to send, eventId: {}", event.getEventType(), event.getEventId());
            }
        }

        log.info("Outbox relay cycle completed: {} processed, {} failed", processed, failed);
    }

    @Scheduled(fixedDelay = 60, timeUnit = TimeUnit.SECONDS)
    public void retryFailedEvents() {
        Instant maxAge = Instant.now().minusSeconds(300);
        List<OutboxEventEntity> oldPendingEvents = outboxRepository.findPendingEventsOlderThan(maxAge);

        if (oldPendingEvents.isEmpty()) {
            return;
        }

        log.info("Retrying {} old pending events", oldPendingEvents.size());

        for (OutboxEventEntity event : oldPendingEvents) {
            boolean success = sendToKafka(event);

            if (success) {
                outboxRepository.markAsProcessed(event.getId(), Instant.now());
                log.info("Successfully retried event {} after {} attempts",
                        event.getEventId(), event.getRetryCount() + 1);
            } else {
                outboxRepository.markAsFailed(event.getId(), "Retry failed");
                log.warn("Failed to retry event {} after {} attempts",
                        event.getEventId(), event.getRetryCount() + 1);
            }
        }
    }

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.HOURS)
    public void cleanupProcessedEvents() {
        Instant olderThan = Instant.now().minusSeconds(7 * 24 * 60 * 60); // 7 days
        int deleted = outboxRepository.deleteProcessedEventsOlderThan(olderThan);
        if (deleted > 0) {
            log.info("Cleaned up {} processed outbox events older than 7 days", deleted);
        }
    }

    private boolean sendToKafka(OutboxEventEntity event) {
        try {
            String key = event.getAggregateId();

            kafkaTemplate.send(orderEventsTopic, key, event.getPayload()).get(10, TimeUnit.SECONDS);
            return true;

        } catch (Exception e) {
            log.error("Failed to send event {} to Kafka: {}", event.getEventId(), e.getMessage());
            return false;
        }
    }
}