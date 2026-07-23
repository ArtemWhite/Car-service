package dealerShipOrder.application.mapper;

import dealerShipOrder.application.dtos.request.testDriveRequest.CreateTestDriveRequest;
import dealerShipOrder.application.dtos.request.testDriveRequest.UpdateTestDriveRequest;
import dealerShipOrder.application.dtos.response.testDriveResponse.TestDriveResponse;
import dealerShipOrder.domain.models.expection.DomainValidationException;
import dealerShipOrder.domain.models.testDriveRequest.TestDriveRequest;
import dealerShipOrder.domain.models.testDriveRequest.TestDriveStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class TestDriveMapper {

    public TestDriveRequest toDomain(CreateTestDriveRequest request) {
        TestDriveRequest testDrive = new TestDriveRequest(
                UUID.randomUUID().toString(),
                request.getClientId(),
                request.getCarId(),
                request.getStartTime()
        );

        if (request.getNotes() != null) {
            testDrive.setNotes(request.getNotes());
        }

        return testDrive;
    }

    public TestDriveResponse toResponse(TestDriveRequest request,
                                        String clientName,
                                        String carInfo,
                                        String managerName) {
        TestDriveResponse response = new TestDriveResponse();

        response.setId(request.getId());
        response.setClientId(request.getClientId());
        response.setClientName(clientName);
        response.setCarId(request.getCarId());
        response.setCarInfo(carInfo);

        if (request.getManagerId() != null) {
            response.setManagerId(request.getManagerId());
            response.setManagerName(managerName);
        }

        response.setRequestedTime(request.getRequestedTime());
        response.setConfirmedTime(request.getConfirmedTime());
        response.setStatus(request.getStatus().name());
        response.setStatusDisplayName(request.getStatus().getDisplayName());
        response.setNotes(request.getNotes());
        response.setUpcoming(request.isUpcoming());
        response.setPast(request.isPast());

        response.setCanCancel(
                request.getStatus() == TestDriveStatus.PENDING ||
                        request.getStatus() == TestDriveStatus.CONFIRMED
        );
        response.setCanReschedule(
                request.getStatus() == TestDriveStatus.PENDING
        );

        return response;
    }

    public List<TestDriveResponse> toResponseList(List<TestDriveRequest> requests) {
        return requests.stream()
                .map(req -> toResponse(req, null, null, null))
                .collect(Collectors.toList());
    }

    public void updateDomain(TestDriveRequest request, UpdateTestDriveRequest updateRequest) {
        if (updateRequest.getStartTime() != null) {
            request.reschedule(updateRequest.getStartTime());
        }

        if (updateRequest.getStatus() != null) {
            updateStatus(request, updateRequest.getStatus(), updateRequest.getCancelReason());
        }

        if (updateRequest.getNotes() != null) {
            request.setNotes(updateRequest.getNotes());
        }
    }

    private void updateStatus(TestDriveRequest request, String status, String reason) {
        switch (status) {
            case "CONFIRMED":
                if (request.getStatus() == TestDriveStatus.PENDING) {
                    request.confirmTime(request.getRequestedTime());
                }
                break;
            case "CANCELLED":
                request.cancel();
                break;
            case "COMPLETED":
                if (request.getStatus() == TestDriveStatus.CONFIRMED) {
                    request.complete();
                }
                break;
            case "NO_SHOW":
                if (request.getStatus() == TestDriveStatus.CONFIRMED) {
                    request.markNoShow();
                }
                break;
            default:
                throw new DomainValidationException("Invalid status: " + status);
        }
    }
}