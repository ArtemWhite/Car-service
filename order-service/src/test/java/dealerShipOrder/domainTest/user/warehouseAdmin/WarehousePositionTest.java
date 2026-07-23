package dealerShipOrder.domainTest.user.warehouseAdmin;

import dealerShipOrder.domain.models.users.warehouseAdmin.WarehousePosition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("WarehousePosition Enum Tests")
class WarehousePositionTest {

    @Test
    @DisplayName("Should have all 3 warehouse positions")
    void shouldHaveAllPositions() {
        WarehousePosition[] positions = WarehousePosition.values();
        assertEquals(3, positions.length);
    }

    @Test
    @DisplayName("Should have correct display names")
    void shouldHaveCorrectDisplayNames() {
        assertEquals("Кладовщик", WarehousePosition.WAREHOUSE_WORKER.getDisplayName());
        assertEquals("Старший кладовщик", WarehousePosition.SENIOR_WAREHOUSE_ADMIN.getDisplayName());
        assertEquals("Заведующий складом", WarehousePosition.WAREHOUSE_MANAGER.getDisplayName());
    }

    @Test
    @DisplayName("Should maintain correct order")
    void shouldMaintainCorrectOrder() {
        assertEquals(0, WarehousePosition.WAREHOUSE_WORKER.ordinal());
        assertEquals(1, WarehousePosition.SENIOR_WAREHOUSE_ADMIN.ordinal());
        assertEquals(2, WarehousePosition.WAREHOUSE_MANAGER.ordinal());
    }
}