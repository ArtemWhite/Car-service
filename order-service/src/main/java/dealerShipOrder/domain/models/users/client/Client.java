package dealerShipOrder.domain.models.users.client;

import dealerShipOrder.domain.models.expection.DomainValidationException;
import dealerShipOrder.domain.models.users.User;
import dealerShipOrder.domain.models.users.UserType;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Client extends User {
    private final List<String> orderIds;
    private final List<String> testDriveRequestIds;
    @Getter
    private String preferredContactMethod;
    @Getter
    private boolean newsletterSubscribed;

    public Client(String employeeId, String firstName, String lastName, String middleName,
                  String email, String phone, String password) {
        super(employeeId, firstName, lastName, middleName, email, phone, password, UserType.CLIENT);
        this.orderIds = new ArrayList<>();
        this.testDriveRequestIds = new ArrayList<>();
        this.preferredContactMethod = "email";
        this.newsletterSubscribed = false;
    }

    public void addOrder(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            throw new DomainValidationException("Order ID cannot be null or empty");
        }
        this.orderIds.add(orderId);
        this.updateLastActive();
    }

    public void addTestDriveRequest(String requestId) {
        if (requestId == null || requestId.isBlank()) {
            throw new DomainValidationException("Test drive request ID cannot be null or empty");
        }
        this.testDriveRequestIds.add(requestId);
        this.updateLastActive();
    }

    public void setPreferredContactMethod(String method) {
        if (method == null) {
            throw new DomainValidationException("Contact method cannot be null");
        }
        if (!method.equals("email") && !method.equals("phone"))
        {
            throw new DomainValidationException("Contact method must be 'email' or 'phone'");
        }
        this.preferredContactMethod = method;
    }

    public void subscribeToNewsletter() {
        this.newsletterSubscribed = true;
        this.updateLastActive();
    }

    public void unsubscribeFromNewsletter() {
        this.newsletterSubscribed = false;
        this.updateLastActive();
    }

    public List<String> getOrderHistory() {
        return Collections.unmodifiableList(orderIds);
    }

    public int getOrderCount() {
        return orderIds.size();
    }

    public List<String> getTestDriveRequests() {
        return Collections.unmodifiableList(testDriveRequestIds);
    }

    public boolean hasActiveTestDrive() {
        return !testDriveRequestIds.isEmpty();
    }
}