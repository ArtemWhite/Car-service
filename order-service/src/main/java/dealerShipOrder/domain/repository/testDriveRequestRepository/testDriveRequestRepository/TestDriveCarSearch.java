package dealerShipOrder.domain.repository.testDriveRequestRepository.testDriveRequestRepository;

import dealerShipOrder.domain.models.testDriveRequest.TestDriveRequest;
import dealerShipOrder.domain.models.testDriveRequest.TestDriveStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface TestDriveCarSearch {
    List<TestDriveRequest> findByCarId(String carId);
    List<TestDriveRequest> findByCarIdAndStatus(String carId, TestDriveStatus status);
    List<TestDriveRequest> findUpcomingByCarId(String carId);
    boolean isCarBookedForTestDrive(String carId, LocalDateTime time);
}
