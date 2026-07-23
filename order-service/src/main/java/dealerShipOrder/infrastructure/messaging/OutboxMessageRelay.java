package dealerShipOrder.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import dealerShipOrder.infrastructure.entities.OutboxEventEntity;
import dealerShipOrder.infrastructure.jpaRepository.OutboxJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFutureCallback;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.List;

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

    @Value("${app.outbox.max-retries:5}")
    private int maxRetries;

    @Value("${app.outbox.relay-delay-ms:5000}")
    private long relayDelayMs;

    @Value("${app.outbox.retry-delay-ms:60000}")
    private long retryDelayMs;

    @Value("${app.outbox.cleanup-delay-ms:3600000}")
    private long cleanupDelayMs;

    @PostConstruct
    public void init() {
        log.info("OutboxMessageRelay started. Topic: {}, relayDelay: {}ms, retryDelay: {}ms",
                orderEventsTopic, relayDelayMs, retryDelayMs);
        long pendingCount = outboxRepository.countPendingEvents();
        long failedCount = outboxRepository.countFailedEvents();
        log.info("Outbox status: {} pending, {} failed events", pendingCount, failedCount);
    }

    @Scheduled(fixedDelayString = "${app.outbox.relay-delay-ms:5000}")
    public void relayPendingEvents() {
        List<OutboxEventEntity> pendingEvents = outboxRepository.findPendingEvents();

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.info("Found {} pending events in outbox", pendingEvents.size());

        int count = 0;
        for (OutboxEventEntity event : pendingEvents) {
            if (count >= batchSize) {
                log.info("Reached batch size limit ({}), continuing next cycle", batchSize);
                break;
            }
            count++;
            sendEventAsync(event);
        }
    }

    @Scheduled(fixedDelayString = "${app.outbox.retry-delay-ms:60000}")
    public void retryFailedEvents() {
        Instant maxAge = Instant.now().minusSeconds(300);
        List<OutboxEventEntity> oldPendingEvents = outboxRepository.findPendingEventsOlderThan(maxAge);

        if (!oldPendingEvents.isEmpty()) {
            log.info("Retrying {} old pending events", oldPendingEvents.size());
            for (OutboxEventEntity event : oldPendingEvents) {
                sendEventAsync(event);
            }
        }

        List<OutboxEventEntity> failedEvents = outboxRepository.findFailedEventsForRetry(maxRetries);

        if (!failedEvents.isEmpty()) {
            log.info("Retrying {} failed events (retryCount < {})", failedEvents.size(), maxRetries);
            for (OutboxEventEntity event : failedEvents) {
                sendEventAsync(event);
            }
        }
    }

    @Scheduled(fixedDelayString = "${app.outbox.cleanup-delay-ms:3600000}")
    public void cleanupProcessedEvents() {
        Instant olderThan = Instant.now().minusSeconds(7 * 24 * 60 * 60);
        int deleted = outboxRepository.deleteProcessedEventsOlderThan(olderThan);
        if (deleted > 0) {
            log.info("Cleaned up {} processed outbox events older than 7 days", deleted);
        }
    }

    private void sendEventAsync(OutboxEventEntity event) {
        String key = event.getAggregateId();

        kafkaTemplate.send(orderEventsTopic, key, event.getPayload())
                .addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
                    @Override
                    public void onSuccess(SendResult<String, String> result) {
                        outboxRepository.markAsProcessed(event.getId(), Instant.now());
                        log.debug("Event {} sent to Kafka, eventId: {}, offset: {}",
                                event.getEventType(), event.getEventId(),
                                result.getRecordMetadata() != null ? result.getRecordMetadata().offset() : "unknown");
                    }

                    @Override
                    public void onFailure(Throwable ex) {
                        outboxRepository.markAsFailed(event.getId(), ex.getMessage());
                        log.warn("Failed to send event {} (eventId: {}): {}",
                                event.getEventType(), event.getEventId(), ex.getMessage());
                    }
                });
    }
}
