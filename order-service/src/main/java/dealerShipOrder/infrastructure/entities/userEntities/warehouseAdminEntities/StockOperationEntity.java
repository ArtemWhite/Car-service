package dealerShipOrder.infrastructure.entities.userEntities.warehouseAdminEntities;

import javax.persistence.*;

import dealerShipOrder.infrastructure.entities.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "stock_operations")
@Getter
@Setter
public class StockOperationEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private WarehouseAdminEntity admin;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(name = "item_id")
    private String itemId;

    @Column(name = "item_type", length = 30)
    private String itemType;

    @Column(name = "from_section", length = 50)
    private String fromSection;

    @Column(name = "to_section", length = 50)
    private String toSection;

    @Column(name = "from_location", length = 100)
    private String fromLocation;

    @Column(name = "to_location", length = 100)
    private String toLocation;

    private int quantity = 1;

    @Column(length = 500)
    private String reason;

    @Column(name = "document_number", length = 50)
    private String documentNumber;

    @Column(name = "operation_timestamp", nullable = false)
    private Instant timestamp;
}