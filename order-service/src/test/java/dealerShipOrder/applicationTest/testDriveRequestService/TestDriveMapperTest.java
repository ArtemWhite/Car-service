package dealerShipOrder.applicationTest.testDriveRequestService;

import dealerShipOrder.application.dtos.request.testDriveRequest.CreateTestDriveRequest;
import dealerShipOrder.application.dtos.request.testDriveRequest.UpdateTestDriveRequest;
import dealerShipOrder.application.dtos.response.testDriveResponse.TestDriveResponse;
import dealerShipOrder.application.mapper.TestDriveMapper;
import dealerShipOrder.domain.models.testDriveRequest.TestDriveRequest;
import dealerShipOrder.domain.models.testDriveRequest.TestDriveStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TestDriveMapper Tests")
class TestDriveMapperTest {

    private TestDriveMapper testDriveMapper;
    private TestDriveRequest testDriveRequest;
    private LocalDateTime futureTime;

    @BeforeEach
    void setUp() {
        testDriveMapper = new TestDriveMapper();
        futureTime = LocalDateTime.now().plusDays(2);
        testDriveRequest = new TestDriveRequest("req123", "test-user-id", "car123", futureTime);
    }

    @Test
    @DisplayName("Should convert CreateTestDriveRequest to TestDriveRequest")
    void shouldConvertCreateRequestToDomain() {
        CreateTestDriveRequest request = new CreateTestDriveRequest();
        request.setClientId("test-user-id");
        request.setCarId("car123");
        request.setStartTime(futureTime);
        request.setNotes("Test notes");

        TestDriveRequest result = testDriveMapper.toDomain(request);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("test-user-id", result.getClientId());
        assertEquals("car123", result.getCarId());
        assertEquals(futureTime, result.getRequestedTime());
        assertNull(result.getConfirmedTime());
        assertNull(result.getManagerId());
        assertEquals(TestDriveStatus.PENDING, result.getStatus());
        assertEquals("Test notes" ,result.getNotes());
    }

    @Test
    @DisplayName("Should convert TestDriveRequest to TestDriveResponse")
    void shouldConvertDomainToResponse() {
        TestDriveResponse response = testDriveMapper.toResponse(testDriveRequest, "John Doe", "BMW 320i", null);

        assertNotNull(response);
        assertEquals("req123", response.getId());
        assertEquals("test-user-id", response.getClientId());
        assertEquals("John Doe", response.getClientName());
        assertEquals("car123", response.getCarId());
        assertEquals("BMW 320i", response.getCarInfo());
        assertNull(response.getManagerId());
        assertNull(response.getManagerName());
        assertEquals(futureTime, response.getRequestedTime());
        assertNull(response.getConfirmedTime());
        assertEquals("PENDING", response.getStatus());
        assertEquals("Ожидает подтверждения", response.getStatusDisplayName());
        assertNull(response.getNotes());
        assertTrue(response.isUpcoming());
        assertFalse(response.isPast());
        assertTrue(response.isCanCancel());
        assertTrue(response.isCanReschedule());
    }

    @Test
    @DisplayName("Should set canCancel correctly for confirmed request")
    void shouldSetCanCancelForConfirmedRequest() {
        testDriveRequest.assignManager("test-user-id");

        TestDriveResponse response = testDriveMapper.toResponse(testDriveRequest, "John Doe", "BMW 320i", "Jane Smith");

        assertTrue(response.isCanCancel());
        assertFalse(response.isCanReschedule());
    }

    @Test
    @DisplayName("Should set canCancel correctly for completed request")
    void shouldSetCanCancelForCompletedRequest() throws Exception {
        LocalDateTime pastTime = LocalDateTime.now().minusHours(1);
        TestDriveRequest pastRequest = new TestDriveRequest("req123", "test-user-id", "car123", LocalDateTime.now().plusDays(1));

        java.lang.reflect.Field requestedTimeField = TestDriveRequest.class.getDeclaredField("requestedTime");
        requestedTimeField.setAccessible(true);
        requestedTimeField.set(pastRequest, pastTime);

        java.lang.reflect.Field confirmedTimeField = TestDriveRequest.class.getDeclaredField("confirmedTime");
        confirmedTimeField.setAccessible(true);
        confirmedTimeField.set(pastRequest, pastTime);

        java.lang.reflect.Field statusField = TestDriveRequest.class.getDeclaredField("status");
        statusField.setAccessible(true);
        statusField.set(pastRequest, TestDriveStatus.CONFIRMED);

        java.lang.reflect.Field managerIdField = TestDriveRequest.class.getDeclaredField("managerId");
        managerIdField.setAccessible(true);
        managerIdField.set(pastRequest, "test-user-id");

        pastRequest.complete();

        TestDriveResponse response = testDriveMapper.toResponse(pastRequest, "John Doe", "BMW 320i", "Jane Smith");

        assertFalse(response.isCanCancel());
        assertFalse(response.isCanReschedule());
        assertTrue(response.isPast());
        assertFalse(response.isUpcoming());
    }

    @Test
    @DisplayName("Should set upcoming/past correctly for future request")
    void shouldSetUpcomingAndPastForFutureRequest() {
        TestDriveResponse response = testDriveMapper.toResponse(testDriveRequest, "John Doe", "BMW 320i", null);

        assertTrue(response.isUpcoming());
        assertFalse(response.isPast());
    }

    @Test
    @DisplayName("Should set upcoming/past correctly for past request")
    void shouldSetUpcomingAndPastForPastRequest() throws Exception {
        LocalDateTime pastTime = LocalDateTime.now().minusHours(1);
        TestDriveRequest pastRequest = new TestDriveRequest("req123", "test-user-id", "car123", LocalDateTime.now().plusDays(1));

        java.lang.reflect.Field confirmedTimeField = TestDriveRequest.class.getDeclaredField("confirmedTime");
        confirmedTimeField.setAccessible(true);
        confirmedTimeField.set(pastRequest, pastTime);

        TestDriveResponse response = testDriveMapper.toResponse(pastRequest, "John Doe", "BMW 320i", null);

        assertFalse(response.isUpcoming());
        assertTrue(response.isPast());
    }

    @Test
    @DisplayName("Should set manager name when present")
    void shouldSetManagerNameWhenPresent() {
        testDriveRequest.assignManager("test-user-id");

        TestDriveResponse response = testDriveMapper.toResponse(testDriveRequest, "John Doe", "BMW 320i", "Jane Smith");

        assertEquals("test-user-id", response.getManagerId());
        assertEquals("Jane Smith", response.getManagerName());
    }

    @Test
    @DisplayName("Should set confirmed time when present")
    void shouldSetConfirmedTimeWhenPresent() {
        testDriveRequest.assignManager("test-user-id");
        LocalDateTime confirmTime = LocalDateTime.now().plusDays(3);
        testDriveRequest.confirmTime(confirmTime);

        TestDriveResponse response = testDriveMapper.toResponse(testDriveRequest, "John Doe", "BMW 320i", "Jane Smith");

        assertEquals(confirmTime, response.getConfirmedTime());
    }

    @Test
    @DisplayName("Should convert list of TestDriveRequests to list of Responses")
    void shouldConvertListOfDomainsToResponses() {
        List<TestDriveRequest> requests = List.of(testDriveRequest, testDriveRequest);

        List<TestDriveResponse> responses = testDriveMapper.toResponseList(requests);

        assertNotNull(responses);
        assertEquals(2, responses.size());
    }

    @Test
    @DisplayName("Should return empty list for empty input")
    void shouldReturnEmptyListForEmptyInput() {
        List<TestDriveRequest> requests = List.of();

        List<TestDriveResponse> responses = testDriveMapper.toResponseList(requests);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    @DisplayName("Should update request time")
    void shouldUpdateRequestTime() {
        UpdateTestDriveRequest updateRequest = new UpdateTestDriveRequest();
        LocalDateTime newTime = LocalDateTime.now().plusDays(5);
        updateRequest.setStartTime(newTime);

        testDriveMapper.updateDomain(testDriveRequest, updateRequest);

        assertEquals(newTime, testDriveRequest.getRequestedTime());
        assertNull(testDriveRequest.getConfirmedTime());
        assertEquals(TestDriveStatus.PENDING, testDriveRequest.getStatus());
    }

    @Test
    @DisplayName("Should update request status to CONFIRMED")
    void shouldUpdateRequestStatusToConfirmed() {
        testDriveRequest.assignManager("test-user-id");

        UpdateTestDriveRequest updateRequest = new UpdateTestDriveRequest();
        updateRequest.setStatus("CONFIRMED");

        testDriveMapper.updateDomain(testDriveRequest, updateRequest);

        assertEquals(TestDriveStatus.CONFIRMED, testDriveRequest.getStatus());
    }

    @Test
    @DisplayName("Should update request status to CANCELLED")
    void shouldUpdateRequestStatusToCancelled() {
        UpdateTestDriveRequest updateRequest = new UpdateTestDriveRequest();
        updateRequest.setStatus("CANCELLED");

        testDriveMapper.updateDomain(testDriveRequest, updateRequest);

        assertEquals(TestDriveStatus.CANCELLED, testDriveRequest.getStatus());
    }

    @Test
    @DisplayName("Should update request notes")
    void shouldUpdateRequestNotes() {
        UpdateTestDriveRequest updateRequest = new UpdateTestDriveRequest();
        updateRequest.setNotes("Updated notes");

        testDriveMapper.updateDomain(testDriveRequest, updateRequest);

        assertEquals("Updated notes", testDriveRequest.getNotes());
    }

    @Test
    @DisplayName("Should ignore null fields in update")
    void shouldIgnoreNullFieldsInUpdate() {
        UpdateTestDriveRequest updateRequest = new UpdateTestDriveRequest();
        updateRequest.setStartTime(null);
        updateRequest.setStatus(null);
        updateRequest.setNotes(null);

        testDriveMapper.updateDomain(testDriveRequest, updateRequest);

        assertEquals(futureTime, testDriveRequest.getRequestedTime());
        assertEquals(TestDriveStatus.PENDING, testDriveRequest.getStatus());
        assertNull(testDriveRequest.getNotes());
    }
}