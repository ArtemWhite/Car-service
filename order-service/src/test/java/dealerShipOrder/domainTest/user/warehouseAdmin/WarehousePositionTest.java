package dealerShipOrder.domainTest.user.warehouseAdmin;

import dealerShipOrder.domain.models.users.warehouseAdmin.WarehousePosition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("WarehousePosition Enum Tests")
class WarehousePositionTest {

    @Test
    @DisplayName("Should have all 5 warehouse positions")
    void shouldHaveAllPositions() {
        WarehousePosition[] positions = WarehousePosition.values();
        assertEquals(5, positions.length);
    }

    @Test
    @DisplayName("Should have correct display names")
    void shouldHaveCorrectDisplayNames() {
        assertEquals("Кладовщик", WarehousePosition.WAREHOUSE_WORKER.getDisplayName());
        assertEquals("Кладовщик", WarehousePosition.STOREKEEPER.getDisplayName());
        assertEquals("Старший кладовщик", WarehousePosition.SENIOR_WAREHOUSE_ADMIN.getDisplayName());
        assertEquals("Старший кладовщик", WarehousePosition.SENIOR_STOREKEEPER.getDisplayName());
        assertEquals("Заведующий складом", WarehousePosition.WAREHOUSE_MANAGER.getDisplayName());
    }

    @Test
    @DisplayName("Should maintain correct order")
    void shouldMaintainCorrectOrder() {
        assertEquals(0, WarehousePosition.WAREHOUSE_WORKER.ordinal());
        assertEquals(1, WarehousePosition.STOREKEEPER.ordinal());
        assertEquals(2, WarehousePosition.SENIOR_WAREHOUSE_ADMIN.ordinal());
        assertEquals(3, WarehousePosition.SENIOR_STOREKEEPER.ordinal());
        assertEquals(4, WarehousePosition.WAREHOUSE_MANAGER.ordinal());
    }
}