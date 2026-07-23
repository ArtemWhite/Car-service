package dealerShipOrder.infrastructure.entities.orderEntities;

import javax.persistence.*;

import dealerShipOrder.infrastructure.entities.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "order_history_entries")
@Getter
@Setter
public class OrderHistoryEntryEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(length = 500)
    private String description;

    @Column(name = "entry_timestamp", nullable = false)
    private Instant timestamp;
}