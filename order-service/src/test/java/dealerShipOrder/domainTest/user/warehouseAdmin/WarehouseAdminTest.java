package dealerShipOrder.domainTest.user.warehouseAdmin;

import dealerShipOrder.domain.models.expection.DomainValidationException;
import dealerShipOrder.domain.models.users.warehouseAdmin.ItemType;
import dealerShipOrder.domain.models.users.warehouseAdmin.OperationType;
import dealerShipOrder.domain.models.users.warehouseAdmin.StockOperation;
import dealerShipOrder.domain.models.users.warehouseAdmin.WarehouseAdmin;
import dealerShipOrder.domain.models.users.warehouseAdmin.WarehousePosition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("WarehouseAdmin Tests")
class WarehouseAdminTest {

    private WarehouseAdmin admin;

    @BeforeEach
    void setUp() {
        admin = new WarehouseAdmin("John", "Doe", "Michael", "john@email.com", "+1234567890", "password123", "emp123");
    }

    @Test
    @DisplayName("Should create warehouse admin")
    void shouldCreateWarehouseAdmin() {
        assertNotNull(admin);
        assertEquals("emp123", admin.getId());
        assertEquals(WarehousePosition.WAREHOUSE_WORKER, admin.getPosition());
        assertFalse(admin.isOnDuty());
        assertNotNull(admin.getManagedSectionIds());
        assertNotNull(admin.getOperationHistory());
        assertTrue(admin.getManagedSectionIds().isEmpty());
        assertTrue(admin.getOperationHistory().isEmpty());
    }

    @Test
    @DisplayName("Should assign to section")
    void shouldAssignToSection() {
        admin.assignToSection("section1");

        assertTrue(admin.getManagedSectionIds().contains("section1"));
        assertEquals(1, admin.getManagedSectionIds().size());
    }

    @Test
    @DisplayName("Should throw when assigning with null section ID")
    void shouldThrowWhenAssigningWithNullSectionId() {
        assertThrows(DomainValidationException.class, () -> {
            admin.assignToSection(null);
        });
    }

    @Test
    @DisplayName("Should throw when assigning with blank section ID")
    void shouldThrowWhenAssigningWithBlankSectionId() {
        assertThrows(DomainValidationException.class, () -> {
            admin.assignToSection("");
        });

        assertThrows(DomainValidationException.class, () -> {
            admin.assignToSection("   ");
        });
    }

    @Test
    @DisplayName("Should remove from section")
    void shouldRemoveFromSection() {
        admin.assignToSection("section1");

        admin.removeFromSection("section1");

        assertFalse(admin.getManagedSectionIds().contains("section1"));
        assertTrue(admin.getManagedSectionIds().isEmpty());
    }

    @Test
    @DisplayName("Should throw when removing from non-assigned section")
    void shouldThrowWhenRemovingFromNonAssignedSection() {
        assertThrows(DomainValidationException.class, () -> {
            admin.removeFromSection("section1");
        });
    }

    @Test
    @DisplayName("Should check canManageSection for worker")
    void shouldCheckCanManageSectionForWorker() {
        admin.assignToSection("section1");
        assertEquals(WarehousePosition.WAREHOUSE_WORKER, admin.getPosition());

        assertTrue(admin.canManageSection("section1"));
        assertFalse(admin.canManageSection("section2"));
    }

    @Test
    @DisplayName("Should check canManageSection for manager")
    void shouldCheckCanManageSectionForManager() {
        admin.setPosition(WarehousePosition.WAREHOUSE_MANAGER);

        assertTrue(admin.canManageSection("anySection"));
    }

    @Test
    @DisplayName("Should start shift")
    void shouldStartShift() {
        assertFalse(admin.isOnDuty());

        admin.startShift();

        assertTrue(admin.isOnDuty());
    }

    @Test
    @DisplayName("Should end shift")
    void shouldEndShift() {
        admin.startShift();
        assertTrue(admin.isOnDuty());

        admin.endShift();

        assertFalse(admin.isOnDuty());
    }

    @Test
    @DisplayName("Should check isOnDuty with user status")
    void shouldCheckIsOnDutyWithUserStatus() {
        admin.startShift();
        assertTrue(admin.isOnDuty());

        admin.block();

        assertFalse(admin.isOnDuty());
    }

    @Test
    @DisplayName("Should add operation")
    void shouldAddOperation() {
        StockOperation operation = StockOperation.createUpdate(
                admin.getId(), "item1", ItemType.SPARE_PART, "section1", "location1", "Test operation"
        );

        admin.addOperation(operation);

        assertEquals(1, admin.getOperationHistory().size());
        assertEquals(operation, admin.getOperationHistory().get(0));
    }

    @Test
    @DisplayName("Should get operation history as unmodifiable")
    void shouldGetUnmodifiableOperationHistory() {
        admin.addOperation(StockOperation.createUpdate(admin.getId(), "item1", ItemType.SPARE_PART, "sec1", "loc1", "test"));

        assertThrows(UnsupportedOperationException.class, () -> {
            admin.getOperationHistory().add(null);
        });
    }

    @Test
    @DisplayName("Should filter operations by date")
    void shouldFilterOperationsByDate() {
        StockOperation op1 = StockOperation.createUpdate(admin.getId(), "item1", ItemType.SPARE_PART, "sec1", "loc1", "test1");
        LocalDateTime time1 = op1.getTimestamp();

        try { Thread.sleep(10); } catch (InterruptedException e) {}

        StockOperation op2 = StockOperation.createUpdate(admin.getId(), "item2", ItemType.SPARE_PART, "sec2", "loc2", "test2");
        LocalDateTime time2 = op2.getTimestamp();

        try { Thread.sleep(1000); } catch (InterruptedException e) {}

        StockOperation op3 = StockOperation.createUpdate(admin.getId(), "item3", ItemType.SPARE_PART, "sec3", "loc3", "test3");

        admin.addOperation(op1);
        admin.addOperation(op2);
        admin.addOperation(op3);

        List<StockOperation> filtered = admin.getOperationsByDate(time1, time2.plusSeconds(1));

        assertTrue(filtered.contains(op1));
        assertTrue(filtered.contains(op2));
        assertFalse(filtered.contains(op3));
    }

    @Test
    @DisplayName("Should filter operations by type")
    void shouldFilterOperationsByType() {
        StockOperation updateOp = StockOperation.createUpdate(admin.getId(), "item1", ItemType.SPARE_PART, "sec1", "loc1", "update");
        StockOperation arrivalOp = StockOperation.createArrival(admin.getId(), "item2", ItemType.SPARE_PART, "sec2", "loc2", 5);

        admin.addOperation(updateOp);
        admin.addOperation(arrivalOp);

        List<StockOperation> updates = admin.getOperationsByType(OperationType.UPDATE);
        List<StockOperation> arrivals = admin.getOperationsByType(OperationType.ARRIVAL);

        assertEquals(1, updates.size());
        assertEquals(updateOp, updates.get(0));
        assertEquals(1, arrivals.size());
        assertEquals(arrivalOp, arrivals.get(0));
    }

    @Test
    @DisplayName("Should set position")
    void shouldSetPosition() {
        assertEquals(WarehousePosition.WAREHOUSE_WORKER, admin.getPosition());

        admin.setPosition(WarehousePosition.SENIOR_WAREHOUSE_ADMIN);

        assertEquals(WarehousePosition.SENIOR_WAREHOUSE_ADMIN, admin.getPosition());
    }

    @Test
    @DisplayName("Should update lastActiveAt when setting position")
    void shouldUpdateLastActiveAtWhenSettingPosition() {
        LocalDateTime before = admin.getLastActiveAt();

        try { Thread.sleep(10); } catch (InterruptedException e) {}
        admin.setPosition(WarehousePosition.WAREHOUSE_MANAGER);

        assertTrue(admin.getLastActiveAt().isAfter(before));
    }
}