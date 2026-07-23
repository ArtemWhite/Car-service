package domain.models.assembly;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssemblyOrder {

    private String id;

    private String sourceOrderId;

    private String orderType;

    private String carId;

    private String configurationId;

    private String carModelId;

    private AssemblyOrderStatus status;

    private String responsibleWarehouseAdminId;

    private Instant createdAt;
    private Instant updatedAt;
    private boolean removed;

    public static AssemblyOrder create(String sourceOrderId, String orderType,
                                       String carId, String configurationId,
                                       String carModelId, String responsibleAdminId) {
        AssemblyOrder order = new AssemblyOrder();
        order.id = UUID.randomUUID().toString();
        order.sourceOrderId = sourceOrderId;
        order.orderType = orderType;
        order.carId = carId;
        order.configurationId = configurationId;
        order.carModelId = carModelId;
        order.status = AssemblyOrderStatus.CREATED;
        order.responsibleWarehouseAdminId = responsibleAdminId;
        order.createdAt = Instant.now();
        order.updatedAt = Instant.now();
        order.removed = false;
        return order;
    }

    public void assemble() {
        if (status != AssemblyOrderStatus.CREATED) {
            throw new IllegalStateException("Cannot assemble order in status: " + status);
        }
        this.status = AssemblyOrderStatus.ASSEMBLED;
        this.updatedAt = Instant.now();
    }

    public void fail() {
        if (status != AssemblyOrderStatus.CREATED) {
            throw new IllegalStateException("Cannot fail order in status: " + status);
        }
        this.status = AssemblyOrderStatus.FAIL;
        this.updatedAt = Instant.now();
    }

    public void softDelete() {
        this.removed = true;
        this.updatedAt = Instant.now();
    }
}