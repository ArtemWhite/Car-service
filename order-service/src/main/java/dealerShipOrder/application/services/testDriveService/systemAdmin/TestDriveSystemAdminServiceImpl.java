package dealerShipOrder.application.services.testDriveService.systemAdmin;

import dealerShipOrder.application.dtos.request.testDriveRequest.UpdateTestDriveRequest;
import dealerShipOrder.application.dtos.response.testDriveResponse.TestDriveResponse;
import dealerShipOrder.application.mapper.TestDriveMapper;
import dealerShipOrder.application.services.testDriveService.BaseTestDriveService;
import dealerShipOrder.domain.repository.testDriveRequestRepository.testDriveRequestRepository.TestDriveRequestRepository;
import dealerShipOrder.domain.repository.usersRepository.userRepository.UserRepository;
import domain.exception.DomainValidationException;
import domain.models.car.Car;
import dealerShipOrder.domain.models.testDriveRequest.TestDriveRequest;
import dealerShipOrder.domain.models.testDriveRequest.TestDriveStatus;
import dealerShipOrder.domain.models.users.User;
import dealerShipOrder.domain.models.users.client.Client;
import dealerShipOrder.domain.models.users.systemAdmin.SystemAdmin;
import domain.repository.carRepository.CarRepository;
import infrastructure.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TestDriveSystemAdminServiceImpl extends BaseTestDriveService implements TestDriveSystemAdminService {

    public TestDriveSystemAdminServiceImpl(
            TestDriveRequestRepository testDriveRepository,
            UserRepository userRepository,
            CarRepository carRepository,
            TestDriveMapper testDriveMapper) {
        super(testDriveRepository, userRepository, carRepository, testDriveMapper);
    }

    @Override
    public TestDriveResponse updateRequest(String requestId, UpdateTestDriveRequest request) {
        String adminId = SecurityUtils.getCurrentUserId();
        User user = findUserById(adminId);
        if (!(user instanceof SystemAdmin admin)) {
            throw new DomainValidationException("User is not a system admin");
        }

        TestDriveRequest testDrive = findRequestById(requestId);

        testDriveMapper.updateDomain(testDrive, request);

        if (request.getManagerId() != null && !request.getManagerId().equals(testDrive.getManagerId())) {
            if (testDrive.getStatus() == TestDriveStatus.PENDING) {
                testDrive.assignManager(request.getManagerId());
            } else {
                throw new DomainValidationException(
                        "Cannot assign manager to test drive in status: " + testDrive.getStatus()
                );
            }
        }

        TestDriveRequest updated = saveRequest(testDrive);

        admin.logAction("UPDATE_TEST_DRIVE", "Updated test drive: " + requestId);
        userRepository.save(admin);

        Car car = findCarById(updated.getCarId());
        Client client = findClientById(updated.getClientId());
        String managerName = updated.getManagerId() != null ?
                getManagerName(updated.getManagerId()) : null;

        return testDriveMapper.toResponse(
                updated,
                client.getFullName(),
                car.getCarInfo(),
                managerName
        );
    }

    @Override
    public void deleteRequest(String requestId, String reason) {
        String adminId = SecurityUtils.getCurrentUserId();
        User user = findUserById(adminId);
        if (!(user instanceof SystemAdmin admin)) {
            throw new DomainValidationException("User is not a system admin");
        }

        TestDriveRequest request = findRequestById(requestId);
        testDriveRepository.delete(requestId);

        admin.logAction("DELETE_TEST_DRIVE", "Deleted test drive: " + requestId + ". Reason: " + reason);
        userRepository.save(admin);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TestDriveResponse> getRequestsByStatus(String status) {
        TestDriveStatus testDriveStatus;
        try {
            testDriveStatus = TestDriveStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new DomainValidationException("Invalid status: " + status);
        }

        return testDriveRepository.findByStatus(testDriveStatus).stream()
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
}