package domainTest.testDriveRequest;

import domain.exception.DomainValidationException;
import domain.models.testDriveRequest.TestDriveRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.*;

class TestDriveRequestHelperTest {

    @Test
    @DisplayName("isUpcoming should return true for pending future request")
    void isUpcomingShouldReturnTrueForPendingFuture() {
        LocalDateTime futureTime = LocalDateTime.now().plusDays(2);
        TestDriveRequest request = new TestDriveRequest("id", "client", "car", futureTime);

        assertTrue(request.isUpcoming());
        assertFalse(request.isPast());
    }

    @Test
    @DisplayName("isUpcoming should return true for confirmed future request")
    void isUpcomingShouldReturnTrueForConfirmedFuture() {
        LocalDateTime futureTime = LocalDateTime.now().plusDays(2);
        TestDriveRequest request = new TestDriveRequest("id", "client", "car", futureTime);
        request.assignManager("manager1");

        assertTrue(request.isUpcoming());
    }

    @Test
    @DisplayName("isUpcoming should return false for past request")
    void isUpcomingShouldReturnFalseForPast() {
        LocalDateTime pastTime = LocalDateTime.now().minusDays(1);
        assertThrows(DomainValidationException.class, () -> new TestDriveRequest("id", "client", "car", pastTime));
    }

    @Test
    @DisplayName("should set and update notes")
    void shouldSetAndUpdateNotes() {
        LocalDateTime futureTime = LocalDateTime.now().plusDays(2);
        TestDriveRequest request = new TestDriveRequest("id", "client", "car", futureTime);

        request.setNotes("Client wants automatic transmission");

        assertEquals("Client wants automatic transmission", request.getNotes());

        request.setNotes("Updated: client prefers manual");

        assertEquals("Updated: client prefers manual", request.getNotes());
    }

    @Test
    @DisplayName("should handle null notes")
    void shouldHandleNullNotes() {
        LocalDateTime futureTime = LocalDateTime.now().plusDays(2);
        TestDriveRequest request = new TestDriveRequest("id", "client", "car", futureTime);

        assertNull(request.getNotes());

        request.setNotes(null);

        assertNull(request.getNotes());
    }
}

