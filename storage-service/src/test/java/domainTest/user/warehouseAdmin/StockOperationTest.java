package domainTest.user.warehouseAdmin;

import domain.models.users.warehouseAdmin.ItemType;
import domain.models.users.warehouseAdmin.OperationType;
import domain.models.users.warehouseAdmin.StockOperation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StockOperation Tests")
class StockOperationTest {

    private final String adminId = "admin123";

    @Test
    @DisplayName("Should create arrival operation")
    void shouldCreateArrival() {
        StockOperation op = StockOperation.createArrival(adminId, "item1", ItemType.SPARE_PART, "sectionA", "shelf1", 10);

        assertEquals(adminId, op.getAdminId());
        assertEquals(OperationType.ARRIVAL, op.getType());
        assertEquals("item1", op.getItemId());
        assertEquals(ItemType.SPARE_PART, op.getItemType());
        assertEquals("sectionA", op.getToSection());
        assertEquals("shelf1", op.getToLocation());
        assertEquals(10, op.getQuantity());
        assertEquals("Поступление на склад", op.getReason());
        assertNotNull(op.getTimestamp());
        assertNotNull(op.getDocumentNumber());
    }

    @Test
    @DisplayName("Should create removal operation")
    void shouldCreateRemoval() {
        StockOperation op = StockOperation.createRemoval(adminId, "item1", ItemType.CAR, "sectionA", "spot1", 1, "Sold");

        assertEquals(adminId, op.getAdminId());
        assertEquals(OperationType.REMOVAL, op.getType());
        assertEquals("item1", op.getItemId());
        assertEquals(ItemType.CAR, op.getItemType());
        assertEquals("sectionA", op.getFromSection());
        assertEquals("spot1", op.getFromLocation());
        assertEquals(1, op.getQuantity());
        assertEquals("Sold", op.getReason());
    }

    @Test
    @DisplayName("Should create move operation")
    void shouldCreateMove() {
        StockOperation op = StockOperation.createMove(adminId, "item1", ItemType.SPARE_PART,
                "sectionA", "shelf1", "sectionB", "shelf2", "Reorganization");

        assertEquals(OperationType.MOVE, op.getType());
        assertEquals("sectionA", op.getFromSection());
        assertEquals("shelf1", op.getFromLocation());
        assertEquals("sectionB", op.getToSection());
        assertEquals("shelf2", op.getToLocation());
        assertEquals(1, op.getQuantity());
        assertEquals("Reorganization", op.getReason());
    }

    @Test
    @DisplayName("Should create write-off operation")
    void shouldCreateWriteOff() {
        StockOperation op = StockOperation.createWriteOff(adminId, "item1", ItemType.SPARE_PART,
                "sectionA", "shelf1", 5, "Damaged");

        assertEquals(OperationType.WRITE_OFF, op.getType());
        assertEquals(5, op.getQuantity());
        assertEquals("Списание: Damaged", op.getReason());
    }

    @Test
    @DisplayName("Should create inventory start operation")
    void shouldCreateInventoryStart() {
        StockOperation op = StockOperation.createInventoryStart(adminId, "sectionA");

        assertEquals(OperationType.INVENTORY_START, op.getType());
        assertEquals("sectionA", op.getFromSection());
        assertEquals("Начало инвентаризации", op.getReason());
    }

    @Test
    @DisplayName("Should create inventory complete operation")
    void shouldCreateInventoryComplete() {
        StockOperation op = StockOperation.createInventoryComplete(adminId, "sectionA", "All items accounted for");

        assertEquals(OperationType.INVENTORY_COMPLETE, op.getType());
        assertEquals("sectionA", op.getFromSection());
        assertEquals("Инвентаризация завершена: All items accounted for", op.getReason());
    }

    @Test
    @DisplayName("Should create discrepancy operation")
    void shouldCreateDiscrepancy() {
        StockOperation op = StockOperation.createDiscrepancy(adminId, "item1", ItemType.SPARE_PART,
                "sectionA", "shelf1", 10, 8, "Missing 2 units");

        assertEquals(OperationType.DISCREPANCY, op.getType());
        assertEquals("item1", op.getItemId());
        assertEquals(ItemType.SPARE_PART, op.getItemType());
        assertEquals("sectionA", op.getFromSection());
        assertEquals("shelf1", op.getFromLocation());
        assertEquals(-2, op.getQuantity());
        assertTrue(op.getReason().contains("Расхождение: ожидалось 10, фактически 8"));
    }

    @Test
    @DisplayName("Should create update operation")
    void shouldCreateUpdate() {
        StockOperation op = StockOperation.createUpdate(adminId, "item1", ItemType.SPARE_PART,
                "sectionA", "shelf1", "Updated price");

        assertEquals(OperationType.UPDATE, op.getType());
        assertEquals("Updated price", op.getReason());
    }

    @Test
    @DisplayName("Should create quantity change operation")
    void shouldCreateQuantityChange() {
        StockOperation op = StockOperation.createQuantityChange(adminId, "item1", ItemType.SPARE_PART,
                "sectionA", "shelf1", 15, "Stock adjustment");

        assertEquals(OperationType.QUANTITY_CHANGE, op.getType());
        assertEquals(15, op.getQuantity());
        assertEquals("Stock adjustment", op.getReason());
    }

    @Test
    @DisplayName("Should create shift start operation")
    void shouldCreateShiftStart() {
        StockOperation op = StockOperation.createShiftStart(adminId);

        assertEquals(OperationType.SHIFT_START, op.getType());
        assertEquals("Shift started", op.getReason());
    }

    @Test
    @DisplayName("Should create shift end operation")
    void shouldCreateShiftEnd() {
        StockOperation op = StockOperation.createShiftEnd(adminId);

        assertEquals(OperationType.SHIFT_END, op.getType());
        assertEquals("Shift ended", op.getReason());
    }

    @Test
    @DisplayName("Should build with all required fields")
    void shouldBuildWithRequiredFields() {
        StockOperation op = new StockOperation.Builder()
                .adminId(adminId)
                .type(OperationType.ARRIVAL)
                .itemId("item1")
                .itemType(ItemType.SPARE_PART)
                .toSection("sectionA")
                .quantity(5)
                .build();

        assertEquals(adminId, op.getAdminId());
        assertEquals(OperationType.ARRIVAL, op.getType());
        assertEquals("item1", op.getItemId());
        assertEquals(ItemType.SPARE_PART, op.getItemType());
        assertEquals("sectionA", op.getToSection());
        assertEquals(5, op.getQuantity());
    }

    @Test
    @DisplayName("Should throw when adminId is null")
    void shouldThrowWhenAdminIdNull() {
        assertThrows(NullPointerException.class, () -> {
            new StockOperation.Builder()
                    .type(OperationType.ARRIVAL)
                    .build();
        });
    }

    @Test
    @DisplayName("Should throw when type is null")
    void shouldThrowWhenTypeNull() {
        assertThrows(NullPointerException.class, () -> {
            new StockOperation.Builder()
                    .adminId(adminId)
                    .build();
        });
    }

    @Test
    @DisplayName("Should throw when itemId is null for non-inventory operations")
    void shouldThrowWhenItemIdNullForNonInventory() {
        assertThrows(NullPointerException.class, () -> {
            new StockOperation.Builder()
                    .adminId(adminId)
                    .type(OperationType.ARRIVAL)
                    .build();
        });
    }

    @Test
    @DisplayName("Should allow null itemId for inventory operations")
    void shouldAllowNullItemIdForInventory() {
        StockOperation op = new StockOperation.Builder()
                .adminId(adminId)
                .type(OperationType.INVENTORY_START)
                .build();

        assertNotNull(op);
        assertNull(op.getItemId());
    }

    @Test
    @DisplayName("Should generate correct document number for arrival")
    void shouldGenerateDocumentNumberForArrival() {
        StockOperation op = StockOperation.createArrival(adminId, "item1", ItemType.SPARE_PART, "secA", "loc1", 5);

        assertTrue(op.getDocumentNumber().startsWith("IN-"));
        assertEquals(18, op.getDocumentNumber().length());
    }

    @Test
    @DisplayName("Should generate correct document number for removal")
    void shouldGenerateDocumentNumberForRemoval() {
        StockOperation op = StockOperation.createRemoval(adminId, "item1", ItemType.CAR, "secA", "loc1", 1, "Sold");

        assertTrue(op.getDocumentNumber().startsWith("OUT-"));
    }

    @Test
    @DisplayName("Should generate correct document number for move")
    void shouldGenerateDocumentNumberForMove() {
        StockOperation op = StockOperation.createMove(adminId, "item1", ItemType.SPARE_PART,
                "secA", "loc1", "secB", "loc2", "Move");

        assertTrue(op.getDocumentNumber().startsWith("MOV-"));
    }

    @Test
    @DisplayName("Should get correct description for arrival")
    void shouldGetCorrectDescriptionForArrival() {
        StockOperation op = StockOperation.createArrival(adminId, "item1", ItemType.SPARE_PART, "sectionA", "shelf1", 10);

        assertTrue(op.getDescription().contains("Поступление: SPARE_PART 10 шт. в sectionA shelf1"));
    }

    @Test
    @DisplayName("Should get correct description for removal")
    void shouldGetCorrectDescriptionForRemoval() {
        StockOperation op = StockOperation.createRemoval(adminId, "item1", ItemType.CAR, "sectionA", "spot1", 1, "Sold");

        assertTrue(op.getDescription().contains("Списание: CAR 1 шт. из sectionA spot1. Причина: Sold"));
    }

    @Test
    @DisplayName("Should get correct description for move")
    void shouldGetCorrectDescriptionForMove() {
        StockOperation op = StockOperation.createMove(adminId, "item1", ItemType.SPARE_PART,
                "sectionA", "shelf1", "sectionB", "shelf2", "Reorg");

        assertTrue(op.getDescription().contains("Перемещение: SPARE_PART из sectionA shelf1 в sectionB shelf2"));
    }

    @Test
    @DisplayName("Should get correct description for inventory start")
    void shouldGetCorrectDescriptionForInventoryStart() {
        StockOperation op = StockOperation.createInventoryStart(adminId, "sectionA");

        assertEquals("Начало инвентаризации в секции sectionA", op.getDescription());
    }

    @Test
    @DisplayName("Should get correct description for shift start")
    void shouldGetCorrectDescriptionForShiftStart() {
        StockOperation op = StockOperation.createShiftStart(adminId);

        assertEquals("Начало смены (админ: " + adminId + ")", op.getDescription());
    }
}