package dealerShipOrder.domainTest.testDriveRequest;

import dealerShipOrder.domain.models.expection.DomainValidationException;
import dealerShipOrder.domain.models.testDriveRequest.TestDriveRequest;
import dealerShipOrder.domain.models.testDriveRequest.TestDriveStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class TestDriveRequestConfirmationTest {

    private TestDriveRequest request;
    private LocalDateTime confirmTime;

    @BeforeEach
    void setUp() {
        LocalDateTime futureTime = LocalDateTime.now().plusDays(2);
        request = new TestDriveRequest("req1", "client1", "car1", futureTime);
        confirmTime = futureTime;
    }

    @Test
    @DisplayName("Should confirm pending request with time")
    void shouldConfirmPendingRequest() {
        assertEquals(TestDriveStatus.PENDING, request.getStatus());

        request.confirmTime(confirmTime);

        assertEquals(confirmTime, request.getConfirmedTime());
        assertEquals(TestDriveStatus.CONFIRMED, request.getStatus());
    }

    @Test
    @DisplayName("Should confirm with different time than requested")
    void shouldConfirmWithDifferentTime() {
        LocalDateTime newTime = LocalDateTime.now().plusDays(3);

        request.confirmTime(newTime);

        assertEquals(newTime, request.getConfirmedTime());
        assertNotEquals(request.getRequestedTime(), request.getConfirmedTime());
    }

    @Test
    @DisplayName("Should throw when confirming cancelled request")
    void shouldThrowWhenConfirmingCancelled() {
        request.cancel();

        assertThrows(DomainValidationException.class, () -> {
            request.confirmTime(confirmTime);
        });
    }

    @Test
    @DisplayName("Should throw when confirming already confirmed request")
    void shouldThrowWhenConfirmingConfirmed() {
        request.confirmTime(confirmTime);

        assertDoesNotThrow(() -> {
            request.confirmTime(confirmTime);
        });
    }
}
