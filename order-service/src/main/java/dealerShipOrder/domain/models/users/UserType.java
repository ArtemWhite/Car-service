package dealerShipOrder.domain.models.users;

public enum UserType {
    CLIENT("Клиент"),
    MANAGER("Менеджер"),
    SYSTEM_ADMIN("Системный администратор"),
    WAREHOUSE_ADMIN("Складской администратор");

    private final String displayName;

    UserType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}