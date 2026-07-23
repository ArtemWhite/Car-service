package dealerShipOrder.application.services.testDriveService;

import dealerShipOrder.application.dtos.request.testDriveRequest.TestDriveFilterRequest;
import dealerShipOrder.application.dtos.response.testDriveResponse.TestDriveListResponse;
import dealerShipOrder.application.dtos.response.testDriveResponse.TestDriveResponse;
import dealerShipOrder.application.mapper.TestDriveMapper;
import dealerShipOrder.domain.repository.testDriveRequestRepository.testDriveRequestRepository.TestDriveRequestRepository;
import dealerShipOrder.domain.repository.usersRepository.userRepository.UserRepository;
import domain.models.car.Car;
import dealerShipOrder.domain.models.testDriveRequest.TestDriveRequest;
import dealerShipOrder.domain.models.testDriveRequest.TestDriveStatus;
import dealerShipOrder.domain.models.users.client.Client;
import domain.repository.carRepository.CarRepository;
import dealerShipOrder.infrastructure.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class TestDriveServiceImpl extends BaseTestDriveService implements TestDriveService {

    public TestDriveServiceImpl(
            TestDriveRequestRepository testDriveRepository,
            UserRepository userRepository,
            CarRepository carRepository,
            TestDriveMapper testDriveMapper) {
        super(testDriveRepository, userRepository, carRepository, testDriveMapper);
    }

    @Override
    public TestDriveResponse getTestDriveById(String id) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        TestDriveRequest request = findRequestById(id);

        boolean isAdminOrManager = SecurityUtils.hasRole("SYSTEM_ADMIN") || SecurityUtils.hasRole("MANAGER");
        if (!isAdminOrManager && !request.getClientId().equals(currentUserId)) {
            throw new SecurityException("Access denied: you can only view your own test drives");
        }

        Car car = findCarById(request.getCarId());
        Client client = findClientById(request.getClientId());
        String managerName = request.getManagerId() != null ?
                getManagerName(request.getManagerId()) : null;

        return testDriveMapper.toResponse(
                request,
                client.getFullName(),
                car.getCarInfo(),
                managerName
        );
    }

    @Override
    public List<TestDriveResponse> getAllTestDrives() {
        String currentUserId = SecurityUtils.getCurrentUserId();
        boolean isAdminOrManager = SecurityUtils.hasRole("SYSTEM_ADMIN") || SecurityUtils.hasRole("MANAGER");

        List<TestDriveRequest> requests = testDriveRepository.findAll();

        if (!isAdminOrManager) {
            requests = requests.stream()
                    .filter(req -> req.getClientId().equals(currentUserId))
                    .collect(Collectors.toList());
        }

        return requests.stream()
                .map(req -> {
                    Car car = findCarById(req.getCarId());
                    Client client = findClientById(req.getClientId());
                    String managerName = req.getManagerId() != null ?
                            getManagerName(req.getManagerId()) : null;
                    return testDriveMapper.toResponse(
                            req,
                            client.getFullName(),
                            car.getCarInfo(),
                            managerName
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    public TestDriveListResponse getTestDrivesWithFilters(TestDriveFilterRequest filter) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        boolean isAdminOrManager = SecurityUtils.hasRole("SYSTEM_ADMIN") || SecurityUtils.hasRole("MANAGER");

        if (!isAdminOrManager) {
            filter.setClientId(currentUserId);
        }

        List<TestDriveRequest> allRequests = testDriveRepository.findAll();

        List<TestDriveRequest> filteredRequests = allRequests.stream()
                .filter(req -> filterByClientId(req, filter.getClientId()))
                .filter(req -> filterByCarId(req, filter.getCarId()))
                .filter(req -> filterByManagerId(req, filter.getManagerId()))
                .filter(req -> filterByStatus(req, filter.getStatus()))
                .filter(req -> filterByDateRange(req, filter.getDateFrom(), filter.getDateTo()))
                .filter(req -> filterByUpcoming(req, filter.getUpcoming()))
                .filter(req -> filterByPast(req, filter.getPast()))
                .collect(Collectors.toList());

        int totalUnfilteredCount = filteredRequests.size();

        String sortBy = filter.getSortBy() != null ? filter.getSortBy() : "requestedTime";
        String sortDirection = filter.getSortDirection() != null ? filter.getSortDirection() : "DESC";

        Comparator<TestDriveRequest> comparator = getComparator(sortBy, sortDirection);
        if (comparator != null) {
            filteredRequests.sort(comparator);
        }

        int page = filter.getPage() != null ? filter.getPage() : 0;
        int size = filter.getSize() != null ? filter.getSize() : 20;

        int start = page * size;
        int end = Math.min(start + size, filteredRequests.size());

        List<TestDriveRequest> pagedRequests = start < filteredRequests.size()
                ? filteredRequests.subList(start, end)
                : new ArrayList<>();

        List<TestDriveResponse> responses = pagedRequests.stream()
                .map(req -> {
                    Car car = findCarById(req.getCarId());
                    Client client = findClientById(req.getClientId());
                    String managerName = req.getManagerId() != null ?
                            getManagerName(req.getManagerId()) : null;
                    return testDriveMapper.toResponse(
                            req,
                            client.getFullName(),
                            car.getCarInfo(),
                            managerName
                    );
                })
                .collect(Collectors.toList());

        int pendingCount = (int) filteredRequests.stream()
                .filter(r -> r.getStatus() == TestDriveStatus.PENDING).count();
        int confirmedCount = (int) filteredRequests.stream()
                .filter(r -> r.getStatus() == TestDriveStatus.CONFIRMED).count();
        int completedCount = (int) filteredRequests.stream()
                .filter(r -> r.getStatus() == TestDriveStatus.COMPLETED).count();
        int cancelledCount = (int) filteredRequests.stream()
                .filter(r -> r.getStatus() == TestDriveStatus.CANCELLED).count();

        return new TestDriveListResponse(
                responses,
                totalUnfilteredCount,
                pendingCount,
                confirmedCount,
                completedCount,
                cancelledCount
        );
    }

    private Comparator<TestDriveRequest> getComparator(String sortBy, String sortDirection) {
        boolean isAsc = "ASC".equalsIgnoreCase(sortDirection);

        switch (sortBy.toLowerCase()) {
            case "status":
                if (isAsc) {
                    return Comparator.comparing((TestDriveRequest req) -> req.getStatus().name(),
                            Comparator.nullsLast(String::compareTo));
                } else {
                    return Comparator.comparing((TestDriveRequest req) -> req.getStatus().name(),
                            Comparator.nullsLast(String::compareTo)).reversed();
                }
            case "clientid":
                if (isAsc) {
                    return Comparator.comparing(TestDriveRequest::getClientId,
                            Comparator.nullsLast(String::compareTo));
                } else {
                    return Comparator.comparing(TestDriveRequest::getClientId,
                            Comparator.nullsLast(String::compareTo)).reversed();
                }
            case "carid":
                if (isAsc) {
                    return Comparator.comparing(TestDriveRequest::getCarId,
                            Comparator.nullsLast(String::compareTo));
                } else {
                    return Comparator.comparing(TestDriveRequest::getCarId,
                            Comparator.nullsLast(String::compareTo)).reversed();
                }
            case "createdat":
            case "createdAt":
                if (isAsc) {
                    return Comparator.comparing(TestDriveRequest::getId);
                } else {
                    return Comparator.comparing(TestDriveRequest::getId).reversed();
                }
            default:
                if (isAsc) {
                    return Comparator.comparing(TestDriveRequest::getRequestedTime,
                            Comparator.nullsLast(LocalDateTime::compareTo));
                } else {
                    return Comparator.comparing(TestDriveRequest::getRequestedTime,
                            Comparator.nullsLast(LocalDateTime::compareTo)).reversed();
                }
        }
    }

    private boolean filterByClientId(TestDriveRequest req, String clientId) {
        if (clientId == null || clientId.isBlank()) return true;
        return req.getClientId().equals(clientId);
    }

    private boolean filterByCarId(TestDriveRequest req, String carId) {
        if (carId == null || carId.isBlank()) return true;
        return req.getCarId().equals(carId);
    }

    private boolean filterByManagerId(TestDriveRequest req, String managerId) {
        if (managerId == null || managerId.isBlank()) return true;
        return managerId.equals(req.getManagerId());
    }

    private boolean filterByStatus(TestDriveRequest req, String status) {
        if (status == null || status.isBlank()) return true;
        try {
            return req.getStatus() == TestDriveStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private boolean filterByDateRange(TestDriveRequest req, LocalDateTime from, LocalDateTime to) {
        LocalDateTime time = req.getConfirmedTime() != null ? req.getConfirmedTime() : req.getRequestedTime();
        if (time == null) return true;
        if (from != null && time.isBefore(from)) return false;
        return to == null || !time.isAfter(to);
    }

    private boolean filterByUpcoming(TestDriveRequest req, Boolean upcoming) {
        if (upcoming == null) return true;
        return upcoming == req.isUpcoming();
    }

    private boolean filterByPast(TestDriveRequest req, Boolean past) {
        if (past == null) return true;
        return past == req.isPast();
    }
}