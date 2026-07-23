package domainTest.user.warehouseAdmin;

import domain.models.users.warehouseAdmin.OperationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("OperationType Enum Tests")
class OperationTypeTest {

    @Test
    @DisplayName("Should have all 11 operation types")
    void shouldHaveAllTypes() {
        OperationType[] types = OperationType.values();
        assertEquals(11, types.length);
    }

    @Test
    @DisplayName("Should have correct display names")
    void shouldHaveCorrectDisplayNames() {
        assertEquals("Поступление", OperationType.ARRIVAL.getDisplayName());
        assertEquals("Списание", OperationType.REMOVAL.getDisplayName());
        assertEquals("Перемещение", OperationType.MOVE.getDisplayName());
        assertEquals("Списание (брак/утилизация)", OperationType.WRITE_OFF.getDisplayName());
        assertEquals("Начало инвентаризации", OperationType.INVENTORY_START.getDisplayName());
        assertEquals("Завершение инвентаризации", OperationType.INVENTORY_COMPLETE.getDisplayName());
        assertEquals("Расхождение", OperationType.DISCREPANCY.getDisplayName());
        assertEquals("Обновлён", OperationType.UPDATE.getDisplayName());
        assertEquals("Количество изменилось", OperationType.QUANTITY_CHANGE.getDisplayName());
        assertEquals("Смена началась", OperationType.SHIFT_START.getDisplayName());
        assertEquals("Смена закончилась", OperationType.SHIFT_END.getDisplayName());
    }
}