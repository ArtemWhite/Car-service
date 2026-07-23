package events;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
public abstract class BaseEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private String eventId = UUID.randomUUID().toString();

    private String eventType;

    private String orderId;

    private String traceId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    @Builder.Default
    private Instant timestamp = Instant.now();

    @Builder.Default
    private Integer version = 1;

    public BaseEvent(String eventId, String eventType, String orderId, String traceId, Instant timestamp, Integer version) {
        this.eventId = eventId != null ? eventId : UUID.randomUUID().toString();
        this.eventType = eventType;
        this.orderId = orderId;
        this.traceId = traceId;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
        this.version = version != null ? version : 1;
    }
}