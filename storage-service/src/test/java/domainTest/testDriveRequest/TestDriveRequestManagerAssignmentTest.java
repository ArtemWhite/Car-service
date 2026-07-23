package domainTest.testDriveRequest;

import domain.exception.DomainValidationException;
import domain.models.testDriveRequest.TestDriveRequest;
import domain.models.testDriveRequest.TestDriveStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.*;

class TestDriveRequestManagerAssignmentTest {

    private TestDriveRequest request;
    private final String managerId = "manager123";

    @BeforeEach
    void setUp() {
        LocalDateTime futureTime = LocalDateTime.now().plusDays(2);
        request = new TestDriveRequest("req1", "client1", "car1", futureTime);
    }

    @Test
    @DisplayName("Should assign manager to pending request")
    void shouldAssignManager() {
        assertEquals(TestDriveStatus.PENDING, request.getStatus());
        assertNull(request.getManagerId());
        assertNull(request.getConfirmedTime());

        request.assignManager(managerId);

        assertEquals(managerId, request.getManagerId());
        assertEquals(TestDriveStatus.CONFIRMED, request.getStatus());
        assertEquals(request.getRequestedTime(), request.getConfirmedTime());
    }

    @Test
    @DisplayName("Should set confirmed time to requested time after assignment")
    void shouldSetConfirmedTime() {
        request.assignManager(managerId);

        assertNotNull(request.getConfirmedTime());
        assertEquals(request.getRequestedTime(), request.getConfirmedTime());
    }

    @Test
    @DisplayName("Should throw exception when assigning manager twice")
    void shouldThrowWhenAssigningTwice() {
        request.assignManager(managerId);

        assertThrows(DomainValidationException.class, () -> {
            request.assignManager("anotherManager");
        });
    }

    @Test
    @DisplayName("Should throw exception when assigning to non-pending request")
    void shouldThrowWhenAssigningToNonPending() {
        request.assignManager(managerId);

        assertThrows(DomainValidationException.class, () -> {
            request.assignManager("anotherManager");
        });
    }

    @Test
    @DisplayName("Should throw exception when assigning to cancelled request")
    void shouldThrowWhenAssigningToCancelled() {
        request.cancel();

        assertThrows(DomainValidationException.class, () -> {
            request.assignManager(managerId);
        });
    }
}
