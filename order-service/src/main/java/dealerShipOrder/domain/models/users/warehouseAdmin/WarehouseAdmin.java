package dealerShipOrder.domain.models.users.warehouseAdmin;

import dealerShipOrder.domain.models.expection.DomainValidationException;
import dealerShipOrder.domain.models.users.UserStatus;
import dealerShipOrder.domain.models.users.UserType;
import dealerShipOrder.domain.models.users.User;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.*;

public class WarehouseAdmin extends User {
    private final Set<String> managedSectionIds;
    private final List<StockOperation> operationHistory;
    @Getter
    private WarehousePosition position;
    private boolean onDuty;

    public WarehouseAdmin(String firstName, String lastName, String middleName,
                          String email, String phone, String password, String employeeId) {
        super(employeeId, firstName, lastName, middleName, email, phone, password, UserType.WAREHOUSE_ADMIN);
        this.managedSectionIds = new HashSet<>();
        this.operationHistory = new ArrayList<>();
        this.position = WarehousePosition.WAREHOUSE_WORKER;
        this.onDuty = false;
    }

    public void assignToSection(String sectionId) {
        if (sectionId == null || sectionId.isBlank()) {
            throw new DomainValidationException("Section ID cannot be null or empty");
        }
        managedSectionIds.add(sectionId);
        updateLastActive();
    }

    public void removeFromSection(String sectionId) {
        if (!managedSectionIds.contains(sectionId)) {
            throw new DomainValidationException("Not assigned to this section");
        }
        managedSectionIds.remove(sectionId);
        updateLastActive();
    }

    public boolean canManageSection(String sectionId) {
        return managedSectionIds.contains(sectionId) || position == WarehousePosition.WAREHOUSE_MANAGER;
    }

    public void startShift() {
        this.onDuty = true;
        updateLastActive();
    }

    public void endShift() {
        this.onDuty = false;
        updateLastActive();
    }

    public boolean isOnDuty() {
        return onDuty && getStatus() == UserStatus.ACTIVE;
    }

    public void addOperation(String action, String details) {
        StockOperation operation = StockOperation.createUpdate(
                this.getId(),
                "SYSTEM",
                ItemType.SPARE_PART,
                null,
                null,
                action + ": " + details
        );
        this.operationHistory.add(operation);
        this.updateLastActive();
    }

    public void addOperation(StockOperation operation) {
        this.operationHistory.add(operation);
        this.updateLastActive();
    }

    public void setPosition(WarehousePosition position) {
        this.position = position;
        updateLastActive();
    }

    private void checkOnDuty() {
        if (!isOnDuty()) {
            throw new DomainValidationException("Warehouse admin is not on duty");
        }
    }

    public Set<String> getManagedSectionIds() {
        return Collections.unmodifiableSet(managedSectionIds);
    }

    public List<StockOperation> getOperationHistory() {
        return Collections.unmodifiableList(operationHistory);
    }

    public List<StockOperation> getOperationsByDate(LocalDateTime from, LocalDateTime to) {
        return operationHistory.stream()
                .filter(op -> !op.getTimestamp().isBefore(from) && !op.getTimestamp().isAfter(to))
                .toList();
    }

    public List<StockOperation> getOperationsByType(OperationType type) {
        return operationHistory.stream()
                .filter(op -> op.getType() == type)
                .toList();
    }
}