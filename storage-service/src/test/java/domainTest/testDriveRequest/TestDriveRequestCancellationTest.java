package domainTest.testDriveRequest;

import domain.exception.DomainValidationException;
import domain.models.testDriveRequest.TestDriveRequest;
import domain.models.testDriveRequest.TestDriveStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

class TestDriveRequestCancellationTest {

    private TestDriveRequest request;

    @BeforeEach
    void setUp() {
        LocalDateTime futureTime = LocalDateTime.now().plusDays(2);
        request = new TestDriveRequest("req1", "client1", "car1", futureTime);
    }

    @Test
    @DisplayName("Should cancel pending request")
    void shouldCancelPending() {
        assertEquals(TestDriveStatus.PENDING, request.getStatus());

        request.cancel();

        assertEquals(TestDriveStatus.CANCELLED, request.getStatus());
    }

    @Test
    @DisplayName("Should cancel confirmed request")
    void shouldCancelConfirmed() {
        request.assignManager("manager1");
        assertEquals(TestDriveStatus.CONFIRMED, request.getStatus());

        request.cancel();

        assertEquals(TestDriveStatus.CANCELLED, request.getStatus());
    }

    @Test
    @DisplayName("Should throw when cancelling completed request")
    void shouldThrowWhenCancellingCompleted() {
        request.assignManager("manager1");
        try {
            java.lang.reflect.Field statusField = request.getClass().getDeclaredField("status");
            statusField.setAccessible(true);
            statusField.set(request, TestDriveStatus.COMPLETED);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        assertThrows(DomainValidationException.class, () -> {
            request.cancel();
        });
    }

    @Test
    @DisplayName("Should throw when cancelling already cancelled request")
    void shouldThrowWhenCancellingCancelled() {
        request.cancel();

        assertThrows(DomainValidationException.class, () -> {
            request.cancel();
        });
    }
}