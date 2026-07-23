package dealerShipOrder.domainTest.user.manager;

import dealerShipOrder.domain.models.expection.DomainValidationException;
import dealerShipOrder.domain.models.users.manager.Manager;
import dealerShipOrder.domain.models.users.manager.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Manager Tests")
class ManagerTest {

    private Manager manager;

    @BeforeEach
    void setUp() {
        manager = new Manager("John", "Doe", "Michael", "john@email.com", "+1234567890", "password123", "emp123");
    }

    @Test
    @DisplayName("Should create manager with all fields")
    void shouldCreateManager() {
        assertNotNull(manager);
        assertEquals("emp123", manager.getId());
        assertEquals("John", manager.getFirstName());
        assertEquals("Doe", manager.getLastName());
        assertEquals(Position.SALES_MANAGER, manager.getPosition());
        assertEquals(10, manager.getMaxConcurrentOrders());
        assertTrue(manager.isAvailable());
        assertNotNull(manager.getAssignedOrders());
        assertNotNull(manager.getManagedTestDrives());
        assertNotNull(manager.getTestDriveFleet());
        assertEquals(0, manager.getAssignedOrdersCount());
    }

    @Test
    @DisplayName("Should assign order successfully")
    void shouldAssignOrder() {
        manager.assignOrder("order123");

        assertEquals(1, manager.getAssignedOrdersCount());
        assertTrue(manager.getAssignedOrders().contains("order123"));
        assertTrue(manager.canTakeMoreOrders());
    }

    @Test
    @DisplayName("Should throw when assigning order with null ID")
    void shouldThrowWhenAssigningOrderWithNullId() {
        assertThrows(DomainValidationException.class, () -> {
            manager.assignOrder(null);
        });
    }

    @Test
    @DisplayName("Should throw when assigning order with blank ID")
    void shouldThrowWhenAssigningOrderWithBlankId() {
        assertThrows(DomainValidationException.class, () -> {
            manager.assignOrder("");
        });
    }

    @Test
    @DisplayName("Should throw when assigning order when manager not available")
    void shouldThrowWhenAssigningOrderWhenNotAvailable() {
        manager.setAvailable(false);

        assertThrows(DomainValidationException.class, () -> {
            manager.assignOrder("order123");
        });
    }

    @Test
    @DisplayName("Should throw when assigning order at max concurrent orders limit")
    void shouldThrowWhenAssigningOrderAtLimit() {
        for (int i = 0; i < 10; i++) {
            manager.assignOrder("order" + i);
        }

        assertThrows(DomainValidationException.class, () -> {
            manager.assignOrder("order11");
        });
    }

    @Test
    @DisplayName("Should complete order successfully")
    void shouldCompleteOrder() {
        manager.assignOrder("order123");
        assertEquals(1, manager.getAssignedOrdersCount());

        manager.completeOrder("order123");

        assertEquals(0, manager.getAssignedOrdersCount());
        assertFalse(manager.getAssignedOrders().contains("order123"));
    }

    @Test
    @DisplayName("Should throw when completing non-assigned order")
    void shouldThrowWhenCompletingNonAssignedOrder() {
        assertThrows(DomainValidationException.class, () -> {
            manager.completeOrder("order999");
        });
    }

    @Test
    @DisplayName("Should assign to test drive successfully")
    void shouldAssignToTestDrive() {
        manager.assignToTestDrive("td123");

        assertTrue(manager.getManagedTestDrives().contains("td123"));
        assertEquals(1, manager.getManagedTestDrives().size());
    }

    @Test
    @DisplayName("Should complete test drive successfully")
    void shouldCompleteTestDrive() {
        manager.assignToTestDrive("td123");

        manager.completeTestDrive("td123");

        assertFalse(manager.getManagedTestDrives().contains("td123"));
        assertEquals(0, manager.getManagedTestDrives().size());
    }

    @Test
    @DisplayName("Should have correct test drive limit")
    void shouldHaveCorrectTestDriveLimit() {
        for (int i = 0; i < 4; i++) {
            manager.assignToTestDrive("td" + i);
            assertTrue(manager.canManageTestDrive());
        }

        manager.assignToTestDrive("td4");
        assertFalse(manager.canManageTestDrive());

        assertThrows(DomainValidationException.class, () -> {
            manager.assignToTestDrive("td5");
        });
    }

    @Test
    @DisplayName("Should add car to test drive fleet")
    void shouldAddCarToTestDriveFleet() {
        manager.addCarToTestDriveFleet("car123");

        assertTrue(manager.getTestDriveFleet().contains("car123"));
        assertEquals(1, manager.getTestDriveFleet().size());
    }

    @Test
    @DisplayName("Should throw when adding duplicate car to test drive fleet")
    void shouldThrowWhenAddingDuplicateCar() {
        manager.addCarToTestDriveFleet("car123");

        assertThrows(DomainValidationException.class, () -> {
            manager.addCarToTestDriveFleet("car123");
        });
    }

    @Test
    @DisplayName("Should remove car from test drive fleet")
    void shouldRemoveCarFromTestDriveFleet() {
        manager.addCarToTestDriveFleet("car123");

        manager.removeCarFromTestDriveFleet("car123");

        assertFalse(manager.getTestDriveFleet().contains("car123"));
        assertEquals(0, manager.getTestDriveFleet().size());
    }

    @Test
    @DisplayName("Should throw when removing non-existing car from fleet")
    void shouldThrowWhenRemovingNonExistingCar() {
        assertThrows(DomainValidationException.class, () -> {
            manager.removeCarFromTestDriveFleet("car999");
        });
    }

    @Test
    @DisplayName("Should set availability")
    void shouldSetAvailability() {
        manager.setAvailable(false);
        assertFalse(manager.isAvailable());

        manager.setAvailable(true);
        assertTrue(manager.isAvailable());
    }

    @Test
    @DisplayName("Should check canTakeMoreOrders")
    void shouldCheckCanTakeMoreOrders() {
        assertTrue(manager.canTakeMoreOrders());

        for (int i = 0; i < manager.getMaxConcurrentOrders() - 1; i++) {
            manager.assignOrder("order" + i);
            assertTrue(manager.canTakeMoreOrders());
        }

        manager.assignOrder("order" + (manager.getMaxConcurrentOrders() - 1));
        assertFalse(manager.canTakeMoreOrders());
    }

    @Test
    @DisplayName("Should check isAvailable with user status")
    void shouldCheckIsAvailable() {
        assertTrue(manager.isAvailable());

        manager.block();
        assertFalse(manager.isAvailable());

        manager.activate();
        assertTrue(manager.isAvailable());

        manager.setAvailable(false);
        assertFalse(manager.isAvailable());
    }

    @Test
    @DisplayName("Should promote to senior manager")
    void shouldPromoteToSeniorManager() {
        manager.promote(Position.SENIOR_MANAGER);

        assertEquals(Position.SENIOR_MANAGER, manager.getPosition());
        assertEquals(15, manager.getMaxConcurrentOrders());
    }

    @Test
    @DisplayName("Should promote to lead manager")
    void shouldPromoteToLeadManager() {
        manager.promote(Position.LEAD_MANAGER);

        assertEquals(Position.LEAD_MANAGER, manager.getPosition());
        assertEquals(20, manager.getMaxConcurrentOrders());
    }

    @Test
    @DisplayName("Should promote to sales manager")
    void shouldPromoteToSalesManager() {
        manager.promote(Position.SENIOR_MANAGER);
        assertEquals(15, manager.getMaxConcurrentOrders());

        manager.promote(Position.SALES_MANAGER);

        assertEquals(Position.SALES_MANAGER, manager.getPosition());
        assertEquals(10, manager.getMaxConcurrentOrders());
    }

    @Test
    @DisplayName("Should return unmodifiable assigned orders list")
    void shouldReturnUnmodifiableAssignedOrders() {
        manager.assignOrder("order1");

        assertThrows(UnsupportedOperationException.class, () -> {
            manager.getAssignedOrders().add("order2");
        });
    }

    @Test
    @DisplayName("Should return unmodifiable managed test drives list")
    void shouldReturnUnmodifiableManagedTestDrives() {
        manager.assignToTestDrive("td1");

        assertThrows(UnsupportedOperationException.class, () -> {
            manager.getManagedTestDrives().add("td2");
        });
    }

    @Test
    @DisplayName("Should return unmodifiable test drive fleet list")
    void shouldReturnUnmodifiableTestDriveFleet() {
        manager.addCarToTestDriveFleet("car1");

        assertThrows(UnsupportedOperationException.class, () -> {
            manager.getTestDriveFleet().add("car2");
        });
    }

    @Test
    @DisplayName("Should get assigned orders count correctly")
    void shouldGetAssignedOrdersCount() {
        assertEquals(0, manager.getAssignedOrdersCount());

        manager.assignOrder("order1");
        assertEquals(1, manager.getAssignedOrdersCount());

        manager.assignOrder("order2");
        assertEquals(2, manager.getAssignedOrdersCount());

        manager.completeOrder("order1");
        assertEquals(1, manager.getAssignedOrdersCount());
    }
}