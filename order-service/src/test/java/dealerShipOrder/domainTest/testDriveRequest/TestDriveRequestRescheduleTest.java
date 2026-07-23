package dealerShipOrder.domainTest.testDriveRequest;

import dealerShipOrder.domain.models.expection.DomainValidationException;
import dealerShipOrder.domain.models.testDriveRequest.TestDriveRequest;
import dealerShipOrder.domain.models.testDriveRequest.TestDriveStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.*;

class TestDriveRequestRescheduleTest {

    private TestDriveRequest request;
    private LocalDateTime originalTime;

    @BeforeEach
    void setUp() {
        originalTime = LocalDateTime.now().plusDays(2);
        request = new TestDriveRequest("req1", "client1", "car1", originalTime);
    }

    @Test
    @DisplayName("Should reschedule pending request to future time")
    void shouldReschedulePendingRequest() {
        LocalDateTime newTime = LocalDateTime.now().plusDays(3);

        request.reschedule(newTime);

        assertEquals(newTime, request.getRequestedTime());
        assertNull(request.getConfirmedTime());
        assertEquals(TestDriveStatus.PENDING, request.getStatus());
    }

    @Test
    @DisplayName("Should reschedule confirmed request to future time")
    void shouldRescheduleConfirmedRequest() {
        request.assignManager("manager1");
        assertEquals(TestDriveStatus.CONFIRMED, request.getStatus());
        LocalDateTime newTime = LocalDateTime.now().plusDays(4);

        request.reschedule(newTime);

        assertEquals(newTime, request.getRequestedTime());
        assertNull(request.getConfirmedTime());
        assertEquals(TestDriveStatus.PENDING, request.getStatus());
    }

    @Test
    @DisplayName("Should throw when rescheduling to past time")
    void shouldThrowWhenReschedulingToPast() {
        LocalDateTime pastTime = LocalDateTime.now().minusDays(1);

        assertThrows(DomainValidationException.class, () -> {
            request.reschedule(pastTime);
        });
    }

    @Test
    @DisplayName("Should throw when rescheduling completed request")
    void shouldThrowWhenReschedulingCompleted() {
        request.assignManager("manager1");

        try {
            java.lang.reflect.Field statusField = request.getClass().getDeclaredField("status");
            statusField.setAccessible(true);
            statusField.set(request, TestDriveStatus.COMPLETED);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        assertThrows(DomainValidationException.class, () -> {
            request.reschedule(LocalDateTime.now().plusDays(5));
        });
    }

    @Test
    @DisplayName("Should throw when rescheduling cancelled request")
    void shouldThrowWhenReschedulingCancelled() {
        request.cancel();

        assertThrows(DomainValidationException.class, () -> {
            request.reschedule(LocalDateTime.now().plusDays(5));
        });
    }

    @Test
    @DisplayName("Should clear confirmed time after reschedule")
    void shouldClearConfirmedTime() {
        request.assignManager("manager1");
        assertNotNull(request.getConfirmedTime());

        request.reschedule(LocalDateTime.now().plusDays(5));

        assertNull(request.getConfirmedTime());
    }
}