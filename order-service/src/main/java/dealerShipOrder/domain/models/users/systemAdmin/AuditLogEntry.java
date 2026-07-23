package dealerShipOrder.domain.models.users.systemAdmin;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AuditLogEntry
{
    private final String adminId;
    private final String action;
    private final String details;
    private final LocalDateTime timestamp;

    public AuditLogEntry(String adminId, String action, String details, LocalDateTime timestamp) {
        this.adminId = adminId;
        this.action = action;
        this.details = details;
        this.timestamp = timestamp;
    }

}