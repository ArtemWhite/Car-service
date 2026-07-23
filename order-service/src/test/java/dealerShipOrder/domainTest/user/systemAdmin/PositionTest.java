package dealerShipOrder.domainTest.user.systemAdmin;

import dealerShipOrder.domain.models.users.manager.Position;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Position Enum Tests")
class PositionTest {

    @Test
    @DisplayName("Should have all 3 positions")
    void shouldHaveAllPositions() {
        Position[] positions = Position.values();
        assertEquals(3, positions.length);
    }

    @Test
    @DisplayName("Should have correct display names")
    void shouldHaveCorrectDisplayNames() {
        assertEquals("Менеджер по продажам", Position.SALES_MANAGER.getDisplayName());
        assertEquals("Старший менеджер", Position.SENIOR_MANAGER.getDisplayName());
        assertEquals("Ведущий менеджер", Position.LEAD_MANAGER.getDisplayName());
    }
}