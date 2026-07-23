package dealerShipOrder.presentation.mappers;

import dealerShipOrder.application.dtos.request.testDriveRequest.CreateTestDriveRequest;
import dealerShipOrder.application.dtos.request.testDriveRequest.TestDriveFilterRequest;
import dealerShipOrder.application.dtos.request.testDriveRequest.UpdateTestDriveRequest;
import dealerShipOrder.application.dtos.response.testDriveResponse.TestDriveListResponse;
import dealerShipOrder.application.dtos.response.testDriveResponse.TestDriveResponse;
import dealerShipOrder.presentation.dtos.request.testDriveRequestPresentationDto.TestDriveCreatePresentationRequest;
import dealerShipOrder.presentation.dtos.request.testDriveRequestPresentationDto.TestDriveFilterPresentationRequest;
import dealerShipOrder.presentation.dtos.request.testDriveRequestPresentationDto.TestDriveUpdatePresentationRequest;
import dealerShipOrder.presentation.dtos.response.testDriveResponsePresentationDto.TestDriveListPresentationResponse;
import dealerShipOrder.presentation.dtos.response.testDriveResponsePresentationDto.TestDrivePresentationResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TestDrivePresentationMapper {

    public CreateTestDriveRequest toApplication(TestDriveCreatePresentationRequest request, String clientId) {
        if (request == null) return null;
        CreateTestDriveRequest target = new CreateTestDriveRequest();
        target.setClientId(clientId);
        target.setCarId(request.getCarId());
        target.setStartTime(request.getStartTime());
        target.setNotes(request.getNotes());
        return target;
    }

    public CreateTestDriveRequest toApplicationWithoutClientId(TestDriveCreatePresentationRequest request) {
        if (request == null) return null;
        CreateTestDriveRequest target = new CreateTestDriveRequest();
        target.setCarId(request.getCarId());
        target.setStartTime(request.getStartTime());
        target.setNotes(request.getNotes());
        return target;
    }

    public UpdateTestDriveRequest toApplication(TestDriveUpdatePresentationRequest request) {
        if (request == null) return null;
        UpdateTestDriveRequest target = new UpdateTestDriveRequest();
        target.setStartTime(request.getStartTime());
        target.setStatus(request.getStatus());
        target.setManagerId(request.getManagerId());
        target.setNotes(request.getNotes());
        target.setCancelReason(request.getCancelReason());
        return target;
    }

    public TestDriveFilterRequest toApplication(TestDriveFilterPresentationRequest request) {
        if (request == null) return new TestDriveFilterRequest();
        TestDriveFilterRequest target = new TestDriveFilterRequest();
        target.setClientId(request.getClientId());
        target.setCarId(request.getCarId());
        target.setManagerId(request.getManagerId());
        target.setStatus(request.getStatus());
        target.setDateFrom(request.getDateFrom());
        target.setDateTo(request.getDateTo());
        target.setUpcoming(request.getUpcoming());
        target.setPast(request.getPast());
        target.setPage(request.getPage());
        target.setSize(request.getSize());
        target.setSortBy(request.getSortBy());
        target.setSortDirection(request.getSortDirection());
        return target;
    }

    public TestDrivePresentationResponse toPresentation(TestDriveResponse source) {
        if (source == null) return null;
        return TestDrivePresentationResponse.builder()
                .id(source.getId())
                .clientId(source.getClientId())
                .clientName(source.getClientName())
                .carId(source.getCarId())
                .carInfo(source.getCarInfo())
                .managerId(source.getManagerId())
                .managerName(source.getManagerName())
                .requestedTime(source.getRequestedTime())
                .confirmedTime(source.getConfirmedTime())
                .status(source.getStatus())
                .statusDisplayName(source.getStatusDisplayName())
                .notes(source.getNotes())
                .upcoming(source.isUpcoming())
                .past(source.isPast())
                .canCancel(source.isCanCancel())
                .canReschedule(source.isCanReschedule())
                .build();
    }

    public TestDriveListPresentationResponse toPresentation(TestDriveListResponse source) {
        if (source == null) return null;
        return TestDriveListPresentationResponse.builder()
                .testDrives(source.getTestDrives().stream()
                        .map(this::toPresentation)
                        .collect(Collectors.toList()))
                .totalCount(source.getTotalCount())
                .pendingCount(source.getPendingCount())
                .confirmedCount(source.getConfirmedCount())
                .completedCount(source.getCompletedCount())
                .cancelledCount(source.getCancelledCount())
                .build();
    }

    public TestDriveListPresentationResponse toListPresentation(List<TestDriveResponse> source) {
        if (source == null || source.isEmpty()) {
            return TestDriveListPresentationResponse.builder()
                    .testDrives(List.of())
                    .totalCount(0)
                    .pendingCount(0)
                    .confirmedCount(0)
                    .completedCount(0)
                    .cancelledCount(0)
                    .build();
        }

        long pendingCount = source.stream()
                .filter(r -> "PENDING".equals(r.getStatus()))
                .count();
        long confirmedCount = source.stream()
                .filter(r -> "CONFIRMED".equals(r.getStatus()))
                .count();
        long completedCount = source.stream()
                .filter(r -> "COMPLETED".equals(r.getStatus()))
                .count();
        long cancelledCount = source.stream()
                .filter(r -> "CANCELLED".equals(r.getStatus()))
                .count();

        return TestDriveListPresentationResponse.builder()
                .testDrives(source.stream()
                        .map(this::toPresentation)
                        .collect(Collectors.toList()))
                .totalCount(source.size())
                .pendingCount((int) pendingCount)
                .confirmedCount((int) confirmedCount)
                .completedCount((int) completedCount)
                .cancelledCount((int) cancelledCount)
                .build();
    }
}