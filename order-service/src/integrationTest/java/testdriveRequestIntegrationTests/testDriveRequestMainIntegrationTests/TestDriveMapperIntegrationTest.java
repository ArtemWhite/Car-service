package testdriveRequestIntegrationTests.testDriveRequestMainIntegrationTests;

import dealerShipOrder.application.dtos.request.testDriveRequest.CreateTestDriveRequest;
import dealerShipOrder.application.dtos.response.testDriveResponse.TestDriveResponse;
import dealerShipOrder.application.mapper.TestDriveMapper;
import dealerShipOrder.domain.models.testDriveRequest.TestDriveRequest;
import dealerShipOrder.domain.models.testDriveRequest.TestDriveStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TestDriveMapperIntegrationTest extends TestDriveBaseIntegrationTest {

    @Autowired
    private TestDriveMapper testDriveMapper;

    @Test
    void shouldMapCreateRequestToDomain() {
        CreateTestDriveRequest request = new CreateTestDriveRequest();
        request.setClientId(UUID.randomUUID().toString());
        request.setCarId(UUID.randomUUID().toString());
        request.setStartTime(LocalDateTime.now().plusHours(1));
        request.setNotes("Test notes");

        TestDriveRequest result = testDriveMapper.toDomain(request);

        assertThat(result).isNotNull();
        assertThat(result.getClientId()).isEqualTo(request.getClientId());
        assertThat(result.getCarId()).isEqualTo(request.getCarId());
        assertThat(result.getRequestedTime()).isEqualTo(request.getStartTime());
        assertThat(result.getNotes()).isEqualTo(request.getNotes());
        assertThat(result.getStatus()).isEqualTo(TestDriveStatus.PENDING);
    }

    @Test
    void shouldMapDomainToResponse() {
        String requestId = UUID.randomUUID().toString();
        String clientId = UUID.randomUUID().toString();
        String carId = UUID.randomUUID().toString();
        String managerId = UUID.randomUUID().toString();
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);

        TestDriveRequest request = new TestDriveRequest(requestId, clientId, carId, startTime);
        request.setNotes("Test notes");
        request.setManagerId(managerId);

        TestDriveResponse response = testDriveMapper.toResponse(
                request, "Client Name", "Car Info", "Manager Name");

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(requestId);
        assertThat(response.getClientId()).isEqualTo(clientId);
        assertThat(response.getCarId()).isEqualTo(carId);
        assertThat(response.getClientName()).isEqualTo("Client Name");
        assertThat(response.getCarInfo()).isEqualTo("Car Info");
        assertThat(response.getManagerId()).isEqualTo(managerId);
        assertThat(response.getManagerName()).isEqualTo("Manager Name");
        assertThat(response.getStatus()).isEqualTo("PENDING");
        assertThat(response.isUpcoming()).isTrue();
    }

    @Test
    void shouldMapConfirmedRequestToResponse() {
        String requestId = UUID.randomUUID().toString();
        String managerId = UUID.randomUUID().toString();
        LocalDateTime futureTime = LocalDateTime.now().plusHours(1);

        TestDriveRequest request = new TestDriveRequest(requestId, "client", "car", futureTime);
        request.setStatus(TestDriveStatus.CONFIRMED);
        request.setConfirmedTime(futureTime);
        request.setManagerId(managerId);

        TestDriveResponse response = testDriveMapper.toResponse(request, "Client", "Car", "Manager");

        assertThat(response.getStatus()).isEqualTo("CONFIRMED");
        assertThat(response.getManagerId()).isEqualTo(managerId);
        assertThat(response.getManagerName()).isEqualTo("Manager");
        assertThat(response.isCanCancel()).isTrue();
        assertThat(response.isCanReschedule()).isFalse();
    }

    @Test
    void shouldMapCompletedRequestToResponse() throws Exception {
        String requestId = UUID.randomUUID().toString();
        String clientId = "client";
        String carId = "car";
        LocalDateTime futureTime = LocalDateTime.now().plusHours(1);

        TestDriveRequest request = new TestDriveRequest(requestId, clientId, carId, futureTime);

        java.lang.reflect.Field requestedTimeField = TestDriveRequest.class.getDeclaredField("requestedTime");
        requestedTimeField.setAccessible(true);
        requestedTimeField.set(request, LocalDateTime.now().minusHours(1));

        java.lang.reflect.Field statusField = TestDriveRequest.class.getDeclaredField("status");
        statusField.setAccessible(true);
        statusField.set(request, TestDriveStatus.COMPLETED);

        java.lang.reflect.Field confirmedTimeField = TestDriveRequest.class.getDeclaredField("confirmedTime");
        confirmedTimeField.setAccessible(true);
        confirmedTimeField.set(request, LocalDateTime.now().minusHours(1));

        TestDriveResponse response = testDriveMapper.toResponse(request, "Client", "Car", null);

        assertThat(response.getStatus()).isEqualTo("COMPLETED");
        assertThat(response.isPast()).isTrue();
        assertThat(response.isUpcoming()).isFalse();
        assertThat(response.isCanCancel()).isFalse();
        assertThat(response.isCanReschedule()).isFalse();
    }

    @Test
    void shouldMapCancelledRequestToResponse() {
        String requestId = UUID.randomUUID().toString();
        TestDriveRequest request = new TestDriveRequest(requestId, "client", "car", LocalDateTime.now().plusHours(1));
        request.setStatus(TestDriveStatus.CANCELLED);

        TestDriveResponse response = testDriveMapper.toResponse(request, "Client", "Car", null);

        assertThat(response.getStatus()).isEqualTo("CANCELLED");
        assertThat(response.isCanCancel()).isFalse();
    }

    @Test
    void shouldMapNoShowRequestToResponse() {
        String requestId = UUID.randomUUID().toString();
        TestDriveRequest request = new TestDriveRequest(requestId, "client", "car", LocalDateTime.now().plusHours(1));
        request.setStatus(TestDriveStatus.NO_SHOW);

        TestDriveResponse response = testDriveMapper.toResponse(request, "Client", "Car", null);

        assertThat(response.getStatus()).isEqualTo("NO_SHOW");
        assertThat(response.isCanCancel()).isFalse();
        assertThat(response.isCanReschedule()).isFalse();
    }

    @Test
    void shouldUpdateDomainFromUpdateRequest() {
        TestDriveRequest request = new TestDriveRequest(
                UUID.randomUUID().toString(), "client", "car", LocalDateTime.now().plusHours(1));

        dealerShipOrder.application.dtos.request.testDriveRequest.UpdateTestDriveRequest updateRequest =
                new dealerShipOrder.application.dtos.request.testDriveRequest.UpdateTestDriveRequest();
        LocalDateTime newTime = LocalDateTime.now().plusHours(3);
        updateRequest.setStartTime(newTime);
        updateRequest.setNotes("Updated notes");

        testDriveMapper.updateDomain(request, updateRequest);

        assertThat(request.getRequestedTime()).isEqualTo(newTime);
        assertThat(request.getNotes()).isEqualTo("Updated notes");
        assertThat(request.getStatus()).isEqualTo(TestDriveStatus.PENDING);
    }

    @Test
    void shouldUpdateStatusToCancelled() {
        TestDriveRequest request = new TestDriveRequest(
                UUID.randomUUID().toString(), "client", "car", LocalDateTime.now().plusHours(1));

        dealerShipOrder.application.dtos.request.testDriveRequest.UpdateTestDriveRequest updateRequest =
                new dealerShipOrder.application.dtos.request.testDriveRequest.UpdateTestDriveRequest();
        updateRequest.setStatus("CANCELLED");

        testDriveMapper.updateDomain(request, updateRequest);

        assertThat(request.getStatus()).isEqualTo(TestDriveStatus.CANCELLED);
    }

    @Test
    void shouldUpdateStatusToConfirmed() {
        TestDriveRequest request = new TestDriveRequest(
                UUID.randomUUID().toString(), "client", "car", LocalDateTime.now().plusHours(1));

        dealerShipOrder.application.dtos.request.testDriveRequest.UpdateTestDriveRequest updateRequest =
                new dealerShipOrder.application.dtos.request.testDriveRequest.UpdateTestDriveRequest();
        updateRequest.setStatus("CONFIRMED");

        testDriveMapper.updateDomain(request, updateRequest);

        assertThat(request.getStatus()).isEqualTo(TestDriveStatus.CONFIRMED);
    }
}