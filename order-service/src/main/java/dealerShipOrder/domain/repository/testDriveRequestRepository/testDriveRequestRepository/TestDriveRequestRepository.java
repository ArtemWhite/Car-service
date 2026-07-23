package dealerShipOrder.domain.repository.testDriveRequestRepository.testDriveRequestRepository;

import dealerShipOrder.domain.models.testDriveRequest.TestDriveRequest;
import dealerShipOrder.domain.repository.BaseRepository;

public interface TestDriveRequestRepository extends
        BaseRepository<TestDriveRequest>,
        TestDriveClientSearch,
        TestDriveCarSearch,
        TestDriveManagerSearch,
        TestDriveStatusSearch,
        TestDriveDateSearch,
        TestDriveConflictSearch {
}
