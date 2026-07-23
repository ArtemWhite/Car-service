package dealerShipOrder.infrastructure.entities.userEntities.systemAdminEntities;

import javax.persistence.*;

import dealerShipOrder.infrastructure.entities.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "audit_log_entries")
@Getter
@Setter
public class AuditLogEntryEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private SystemAdminEntity admin;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(length = 500)
    private String details;

    @Column(name = "log_timestamp", nullable = false)
    private Instant timestamp;
}