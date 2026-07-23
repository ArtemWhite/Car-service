package dealerShipOrder.domain.models.users;

import lombok.Getter;

public enum UserStatus
{
    ACTIVE("Активен", true, true),
    INACTIVE("Неактивен", false, true),
    BLOCKED("Заблокирован", false, false),
    PENDING_VERIFICATION("Ожидает подтверждения", false, true),
    DELETED("Удалён", false, false);

    @Getter
    private final String displayName;
    private final boolean canAuthenticate;
    private final boolean canBeRestored;

    UserStatus(String displayName, boolean canAuthenticate, boolean canBeRestored) {
        this.displayName = displayName;
        this.canAuthenticate = canAuthenticate;
        this.canBeRestored = canBeRestored;
    }

    public boolean canAuthenticate() { return canAuthenticate; }
    public boolean canBeRestored() { return canBeRestored; }
}

