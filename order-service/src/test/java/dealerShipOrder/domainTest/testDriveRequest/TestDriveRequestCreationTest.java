package dealerShipOrder.domainTest.testDriveRequest;

import dealerShipOrder.domain.models.expection.DomainValidationException;
import dealerShipOrder.domain.models.testDriveRequest.TestDriveRequest;
import dealerShipOrder.domain.models.testDriveRequest.TestDriveStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.*;

class TestDriveRequestCreationTest {

    @Test
    @DisplayName("Should create test drive request with valid data")
    void shouldCreateWithValidData() {
        String id = "req123";
        String clientId = "client456";
        String carId = "car789";
        LocalDateTime futureTime = LocalDateTime.now().plusDays(2);

        TestDriveRequest request = new TestDriveRequest(id, clientId, carId, futureTime);

        assertNotNull(request);
        assertEquals(id, request.getId());
        assertEquals(clientId, request.getClientId());
        assertEquals(carId, request.getCarId());
        assertEquals(futureTime, request.getRequestedTime());
        assertNull(request.getConfirmedTime());
        assertNull(request.getManagerId());
        assertEquals(TestDriveStatus.PENDING, request.getStatus());
        assertNull(request.getNotes());
    }

    @Test
    @DisplayName("Should set status to PENDING by default")
    void shouldSetPendingStatus() {
        LocalDateTime futureTime = LocalDateTime.now().plusDays(1);

        TestDriveRequest request = new TestDriveRequest("id", "client", "car", futureTime);

        assertEquals(TestDriveStatus.PENDING, request.getStatus());
    }

    @Test
    @DisplayName("Should throw exception when creating with past time")
    void shouldThrowWhenTimeInPast() {
        LocalDateTime pastTime = LocalDateTime.now().minusDays(1);

        assertThrows(DomainValidationException.class, () -> {
            new TestDriveRequest("id", "client", "car", pastTime);
        });
    }

    @Test
    @DisplayName("Should create request with future time")
    void shouldCreateWithFutureTime() {
        LocalDateTime futureTime = LocalDateTime.now().plusDays(5);

        TestDriveRequest request = new TestDriveRequest("id", "client", "car", futureTime);

        assertEquals(futureTime, request.getRequestedTime());
    }

    @Test
    @DisplayName("Should create request with current time plus margin")
    void shouldCreateWithCurrentTimePlusMargin() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime futureTime = now.plusHours(1);

        TestDriveRequest request = new TestDriveRequest("id", "client", "car", futureTime);

        assertTrue(request.getRequestedTime().isAfter(now));
    }
}