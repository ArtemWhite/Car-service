package dealerShipOrder.domain.models.users.manager;

import dealerShipOrder.domain.models.expection.DomainValidationException;
import dealerShipOrder.domain.models.users.User;
import dealerShipOrder.domain.models.users.UserStatus;
import dealerShipOrder.domain.models.users.UserType;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Manager extends User {
    @Getter
    private Position position;
    private final List<String> assignedOrderIds;
    private final List<String> managedTestDriveIds;
    private final List<String> testDriveFleetCarIds;
    @Getter
    private int maxConcurrentOrders;
    @Getter
    private int maxConcurrentTestDrives;
    private boolean available;

    public Manager(String firstName, String lastName, String middleName, String email, String phone,
                   String password, String employeeId) {
        super(employeeId, firstName, lastName, middleName, email, phone, password, UserType.MANAGER);
        this.position = Position.SALES_MANAGER;
        this.assignedOrderIds = new ArrayList<>();
        this.managedTestDriveIds = new ArrayList<>();
        this.testDriveFleetCarIds = new ArrayList<>();
        this.maxConcurrentOrders = 10;
        this.maxConcurrentTestDrives = 5;
        this.available = true;
    }

    public void assignOrder(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            throw new DomainValidationException("Order ID cannot be null or empty");
        }
        if (!available) {
            throw new DomainValidationException("Manager is not available");
        }
        if (assignedOrderIds.size() >= maxConcurrentOrders) {
            throw new DomainValidationException("Maximum concurrent orders reached");
        }
        this.assignedOrderIds.add(orderId);
        this.updateLastActive();
    }

    public void completeOrder(String orderId) {
        if (!assignedOrderIds.contains(orderId)) {
            throw new DomainValidationException("Order not assigned to this manager");
        }
        this.assignedOrderIds.remove(orderId);
        this.updateLastActive();
    }

    public void assignToTestDrive(String testDriveId) {
        if (testDriveId == null || testDriveId.isBlank()) {
            throw new DomainValidationException("Test drive ID cannot be null or empty");
        }
        if (!available) {
            throw new DomainValidationException("Manager is not available");
        }
        if (managedTestDriveIds.size() >= maxConcurrentTestDrives) {
            throw new DomainValidationException("Maximum concurrent test drives reached");
        }
        this.managedTestDriveIds.add(testDriveId);
        this.updateLastActive();
    }

    public void completeTestDrive(String testDriveId) {
        if (!managedTestDriveIds.contains(testDriveId)) {
            throw new DomainValidationException("Test drive not assigned to this manager");
        }
        this.managedTestDriveIds.remove(testDriveId);
        this.updateLastActive();
    }

    public void addCarToTestDriveFleet(String carId) {
        if (carId == null || carId.isBlank()) {
            throw new DomainValidationException("Car ID cannot be null or empty");
        }
        if (testDriveFleetCarIds.contains(carId)) {
            throw new DomainValidationException("Car already in test drive fleet");
        }
        this.testDriveFleetCarIds.add(carId);
        this.updateLastActive();
    }

    public void removeCarFromTestDriveFleet(String carId) {
        if (!testDriveFleetCarIds.contains(carId)) {
            throw new DomainValidationException("Car not in test drive fleet");
        }
        this.testDriveFleetCarIds.remove(carId);
        this.updateLastActive();
    }

    public void setAvailable(boolean available) {
        this.available = available;
        this.updateLastActive();
    }

    public void promote(Position newPosition) {
        this.position = newPosition;
        switch (newPosition) {
            case SENIOR_MANAGER:
                this.maxConcurrentOrders = 15;
                this.maxConcurrentTestDrives = 10;
                break;
            case LEAD_MANAGER:
                this.maxConcurrentOrders = 20;
                this.maxConcurrentTestDrives = 15;
                break;
            default:
                this.maxConcurrentOrders = 10;
                this.maxConcurrentTestDrives = 5;
        }
        this.updateLastActive();
    }

    public List<String> getAssignedOrders() {
        return Collections.unmodifiableList(assignedOrderIds);
    }

    public int getAssignedOrdersCount() {
        return assignedOrderIds.size();
    }

    public boolean canTakeMoreOrders() {
        return available && assignedOrderIds.size() < maxConcurrentOrders;
    }

    public List<String> getManagedTestDrives() {
        return Collections.unmodifiableList(managedTestDriveIds);
    }

    public List<String> getTestDriveFleet() {
        return Collections.unmodifiableList(testDriveFleetCarIds);
    }

    public boolean isAvailable() {
        return available && getStatus() == UserStatus.ACTIVE;
    }

    public boolean canManageTestDrive() {
        return isAvailable() && managedTestDriveIds.size() < maxConcurrentTestDrives;
    }

}