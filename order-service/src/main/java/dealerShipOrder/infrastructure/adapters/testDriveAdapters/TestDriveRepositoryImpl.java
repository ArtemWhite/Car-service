package dealerShipOrder.infrastructure.adapters.testDriveAdapters;

import dealerShipOrder.domain.models.testDriveRequest.*;
import dealerShipOrder.domain.repository.testDriveRequestRepository.testDriveRequestRepository.TestDriveRequestRepository;
import dealerShipOrder.infrastructure.adapters.testDriveAdapters.testDriveReferencesAdapters.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TestDriveRepositoryImpl implements TestDriveRequestRepository {

    private final TestDriveBaseRepositoryAdapter baseAdapter;
    private final TestDriveClientAdapter clientAdapter;
    private final TestDriveCarAdapter carAdapter;
    private final TestDriveManagerAdapter managerAdapter;
    private final TestDriveStatusAdapter statusAdapter;
    private final TestDriveDateAdapter dateAdapter;
    private final TestDriveConflictAdapter conflictAdapter;

    @Override
    public TestDriveRequest save(TestDriveRequest request) { return baseAdapter.save(request); }
    @Override
    public Optional<TestDriveRequest> findById(String id) { return baseAdapter.findById(id); }
    @Override
    public List<TestDriveRequest> findAll() { return baseAdapter.findAll(); }
    @Override
    public void delete(String id) { baseAdapter.delete(id); }
    @Override
    public boolean existsById(String id) { return baseAdapter.existsById(id); }

    @Override
    public List<TestDriveRequest> findByClientId(String clientId) { return clientAdapter.findByClientId(clientId); }
    @Override
    public List<TestDriveRequest> findByClientIdAndStatus(String clientId, TestDriveStatus status) {
        return clientAdapter.findByClientIdAndStatus(clientId, status);
    }
    @Override
    public List<TestDriveRequest> findUpcomingByClientId(String clientId) {
        return clientAdapter.findUpcomingByClientId(clientId);
    }
    @Override
    public boolean hasActiveRequestForClient(String clientId) {
        return clientAdapter.hasActiveRequestForClient(clientId);
    }

    @Override
    public List<TestDriveRequest> findByCarId(String carId) { return carAdapter.findByCarId(carId); }
    @Override
    public List<TestDriveRequest> findByCarIdAndStatus(String carId, TestDriveStatus status) {
        return carAdapter.findByCarIdAndStatus(carId, status);
    }
    @Override
    public List<TestDriveRequest> findUpcomingByCarId(String carId) {
        return carAdapter.findUpcomingByCarId(carId);
    }
    @Override
    public boolean isCarBookedForTestDrive(String carId, LocalDateTime time) {
        return carAdapter.isCarBookedForTestDrive(carId, time);
    }

    @Override
    public List<TestDriveRequest> findByManagerId(String managerId) { return managerAdapter.findByManagerId(managerId); }
    @Override
    public List<TestDriveRequest> findByManagerIdAndStatus(String managerId, TestDriveStatus status) {
        return managerAdapter.findByManagerIdAndStatus(managerId, status);
    }
    @Override
    public List<TestDriveRequest> findUpcomingByManagerId(String managerId) {
        return managerAdapter.findUpcomingByManagerId(managerId);
    }
    @Override
    public long countAssignedToManager(String managerId) { return managerAdapter.countAssignedToManager(managerId); }

    @Override
    public List<TestDriveRequest> findByStatus(TestDriveStatus status) { return statusAdapter.findByStatus(status); }
    @Override
    public List<TestDriveRequest> findByStatusIn(List<TestDriveStatus> statuses) {
        return statusAdapter.findByStatusIn(statuses);
    }
    @Override
    public long countByStatus(TestDriveStatus status) { return statusAdapter.countByStatus(status); }

    @Override
    public List<TestDriveRequest> findByDateRange(LocalDateTime start, LocalDateTime end) {
        return dateAdapter.findByDateRange(start, end);
    }
    @Override
    public List<TestDriveRequest> findUpcomingTestDrives() { return dateAdapter.findUpcomingTestDrives(); }
    @Override
    public List<TestDriveRequest> findPastTestDrives() { return dateAdapter.findPastTestDrives(); }
    @Override
    public List<TestDriveRequest> findByDateAndStatus(LocalDateTime date, TestDriveStatus status) {
        return dateAdapter.findByDateAndStatus(date, status);
    }
    @Override
    public List<TestDriveRequest> findByDateTimeBetween(LocalDateTime start, LocalDateTime end) {
        return dateAdapter.findByDateTimeBetween(start, end);
    }

    @Override
    public List<TestDriveRequest> findConflictingRequests(String carId, LocalDateTime time) {
        return conflictAdapter.findConflictingRequests(carId, time);
    }
    @Override
    public List<TestDriveRequest> findConflictingForManager(String managerId, LocalDateTime time) {
        return conflictAdapter.findConflictingForManager(managerId, time);
    }
    @Override
    public boolean hasConflict(String carId, LocalDateTime time) {
        return conflictAdapter.hasConflict(carId, time);
    }

    @Override
    public long countByClientId(String clientId) {
        return clientAdapter.countByClientId(clientId);
    }
}