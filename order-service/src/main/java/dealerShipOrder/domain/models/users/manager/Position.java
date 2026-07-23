package dealerShipOrder.domain.models.users.manager;

import lombok.Getter;

@Getter
public enum Position
{
    SALES_MANAGER("Менеджер по продажам"),
    SENIOR_MANAGER("Старший менеджер"),
    LEAD_MANAGER("Ведущий менеджер");

    private final String displayName;

    Position(String displayName) {
        this.displayName = displayName;
    }

}
