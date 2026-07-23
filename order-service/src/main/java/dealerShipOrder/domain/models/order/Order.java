package dealerShipOrder.domain.models.order;

import dealerShipOrder.domain.models.expection.DomainValidationException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Getter
@AllArgsConstructor
public class Order {
    private String id;
    private final String clientId;
    private String managerId;
    private OrderType type;
    private OrderStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
    private List<OrderHistoryEntry> history;
    private String carId;
    private String configurationId;
    private String carModelId;

    private String notes;

    private Order(Builder builder) {
        this.id = builder.id;
        this.clientId = builder.clientId;
        this.managerId = builder.managerId;
        this.type = builder.type;
        this.status = OrderStatus.CREATED;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.history = new ArrayList<>();
        this.carId = builder.carId;
        this.configurationId = builder.configurationId;
        this.carModelId = builder.carModelId;
        this.notes = builder.notes;

        addHistoryEntry("ORDER_CREATED", "Заказ создан");
    }

    public static Order createInStockOrder(String id, String clientId, String carId) {
        return new Builder()
                .id(id)
                .clientId(clientId)
                .type(OrderType.IN_STOCK)
                .carId(carId)
                .build();
    }

    public static Order createCustomOrder(String id, String clientId, String configurationId, String carModelId) {
        return new Builder()
                .id(id)
                .clientId(clientId)
                .type(OrderType.CUSTOM)
                .configurationId(configurationId)
                .carModelId(carModelId)
                .build();
    }

    public void assignManager(String managerId) {
        if (this.managerId != null) {
            throw new DomainValidationException("Manager already assigned");
        }
        this.managerId = managerId;
        this.status = OrderStatus.MANAGER_APPROVED;
        this.updatedAt = LocalDateTime.now();
        addHistoryEntry("MANAGER_ASSIGNED", "Назначен менеджер: " + managerId);
    }

    public void confirmByStock() {
        if (type != OrderType.CUSTOM) {
            throw new DomainValidationException("Only custom orders need stock confirmation");
        }
        if (status != OrderStatus.CREATED && status != OrderStatus.MANAGER_APPROVED) {
            throw new DomainValidationException("Invalid status for stock confirmation");
        }
        this.status = OrderStatus.STOCK_CONFIRMED;
        this.updatedAt = LocalDateTime.now();
        addHistoryEntry("STOCK_CONFIRMED", "Заказ подтверждён складом");
    }

    public void awaitPayment() {
        if (status != OrderStatus.MANAGER_APPROVED && status != OrderStatus.STOCK_CONFIRMED) {
            throw new DomainValidationException("Cannot move to payment from current status");
        }
        this.status = OrderStatus.AWAITING_PAYMENT;
        this.updatedAt = LocalDateTime.now();
        addHistoryEntry("AWAITING_PAYMENT", "Ожидает оплаты");
    }

    public void markAsPaid() {
        if (status != OrderStatus.AWAITING_PAYMENT) {
            throw new DomainValidationException("Order is not awaiting payment");
        }
        this.status = OrderStatus.PAID;
        this.updatedAt = LocalDateTime.now();
        addHistoryEntry("PAID", "Заказ оплачен");
    }

    public void markAsReadyForPickup() {
        if (status != OrderStatus.PAID) {
            throw new DomainValidationException("Order must be paid first");
        }
        this.status = OrderStatus.READY_FOR_PICKUP;
        this.updatedAt = LocalDateTime.now();
        addHistoryEntry("READY_FOR_PICKUP", "Автомобиль готов к выдаче");
    }

    public void markAsCompleted() {
        if (status != OrderStatus.READY_FOR_PICKUP) {
            throw new DomainValidationException("Order is not ready for pickup");
        }
        this.status = OrderStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        addHistoryEntry("COMPLETED", "Заказ завершён");
    }

    public void cancel(String reason) {
        if (status == OrderStatus.COMPLETED) {
            throw new DomainValidationException("Cannot cancel completed order");
        }
        if (status == OrderStatus.CANCELLED) {
            throw new DomainValidationException("Order already cancelled");
        }
        this.status = OrderStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
        addHistoryEntry("CANCELLED", "Заказ отменён: " + reason);
    }

    public void waitForDelivery() {
        if (type != OrderType.CUSTOM) {
            throw new DomainValidationException("Only custom orders have delivery");
        }
        if (status != OrderStatus.PAID) {
            throw new DomainValidationException("Order must be paid first");
        }
        this.status = OrderStatus.AWAITING_DELIVERY;
        this.updatedAt = LocalDateTime.now();
        addHistoryEntry("AWAITING_DELIVERY", "Ожидает доставки автомобиля");
    }

    public void markAsDelivered() {
        if (status != OrderStatus.AWAITING_DELIVERY) {
            throw new DomainValidationException("Order is not awaiting delivery");
        }
        this.status = OrderStatus.READY_FOR_PICKUP;
        this.updatedAt = LocalDateTime.now();
        addHistoryEntry("DELIVERED", "Автомобиль доставлен");
    }

    private void addHistoryEntry(String action, String description) {
        this.history.add(new OrderHistoryEntry(this.id , action, description, LocalDateTime.now()));
    }

    public boolean isInStockOrder() {
        return type == OrderType.IN_STOCK;
    }

    public boolean isCustomOrder() {
        return type == OrderType.CUSTOM;
    }

    public boolean isActive() {
        return status != OrderStatus.COMPLETED &&
                status != OrderStatus.CANCELLED;
    }

    public List<OrderHistoryEntry> getHistory() {
        return Collections.unmodifiableList(history);
    }

    public void setNotes(String notes) {
        this.notes = notes;
        this.updatedAt = LocalDateTime.now();
    }

    public static class Builder {
        private String id;
        private String clientId;
        private String managerId;
        private OrderType type;
        private String carId;
        private String configurationId;
        private String carModelId;
        private String notes;

        public Builder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder managerId(String managerId) {
            this.managerId = managerId;
            return this;
        }

        public Builder type(OrderType type) {
            this.type = type;
            return this;
        }

        public Builder carId(String carId) {
            this.carId = carId;
            return this;
        }

        public Builder id(String id)
        {
            this.id = id;
            return this;
        }

        public Builder configurationId(String configurationId) {
            this.configurationId = configurationId;
            return this;
        }

        public Builder carModelId(String carModelId) {
            this.carModelId = carModelId;
            return this;
        }

        public Builder notes(String notes) {
            this.notes = notes;
            return this;
        }

        public Order build() {
            Objects.requireNonNull(clientId, "Client ID required");
            Objects.requireNonNull(type, "Order type required");

            if (type == OrderType.IN_STOCK) {
                Objects.requireNonNull(carId, "Car ID required for in-stock order");
            } else if (type == OrderType.CUSTOM) {
                Objects.requireNonNull(configurationId, "Configuration ID required for custom order");
                Objects.requireNonNull(carModelId, "Car model ID required for custom order");
            }

            return new Order(this);
        }
    }
}