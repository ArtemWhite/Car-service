package domainTest.user.warehouseAdmin;

import domain.models.users.warehouseAdmin.ItemType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ItemType Enum Tests")
class ItemTypeTest {

    @Test
    @DisplayName("Should have both item types")
    void shouldHaveBothTypes() {
        ItemType[] types = ItemType.values();
        assertEquals(2, types.length);
    }

    @Test
    @DisplayName("Should have correct display names")
    void shouldHaveCorrectDisplayNames() {
        assertEquals("Автомобиль", ItemType.CAR.getDisplayName());
        assertEquals("Запчасть", ItemType.SPARE_PART.getDisplayName());
    }

    @Test
    @DisplayName("Should maintain correct order")
    void shouldMaintainCorrectOrder() {
        assertEquals(0, ItemType.CAR.ordinal());
        assertEquals(1, ItemType.SPARE_PART.ordinal());
    }
}