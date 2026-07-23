package domainTest.testDriveRequest;

import domain.exception.DomainValidationException;
import domain.models.testDriveRequest.TestDriveRequest;
import domain.models.testDriveRequest.TestDriveStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

class TestDriveRequestCompletionTest {

    @Test
    @DisplayName("Should throw when completing non-confirmed request")
    void shouldThrowWhenCompletingNonConfirmed() {
        LocalDateTime futureTime = LocalDateTime.now().plusDays(1);
        TestDriveRequest request = new TestDriveRequest("req1", "client1", "car1", futureTime);

        assertEquals(TestDriveStatus.PENDING, request.getStatus());
        assertThrows(DomainValidationException.class, request::complete);
    }

    @Test
    @DisplayName("Should throw when completing future test drive")
    void shouldThrowWhenCompletingFuture() {
        LocalDateTime futureTime = LocalDateTime.now().plusDays(2);
        TestDriveRequest request = new TestDriveRequest("req2", "client2", "car2", futureTime);

        request.assignManager("manager1");
        request.confirmTime(futureTime);

        assertThrows(DomainValidationException.class, request::complete);
    }

}