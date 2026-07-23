package dealerShipOrder.application.services.testDriveService.manager;

import dealerShipOrder.application.dtos.response.testDriveResponse.TestDriveResponse;
import dealerShipOrder.application.mapper.TestDriveMapper;
import dealerShipOrder.application.services.testDriveService.BaseTestDriveService;
import dealerShipOrder.domain.repository.testDriveRequestRepository.testDriveRequestRepository.TestDriveRequestRepository;
import dealerShipOrder.domain.repository.usersRepository.userRepository.UserRepository;
import dealerShipOrder.domain.models.expection.DomainValidationException;
import domain.models.car.Car;
import dealerShipOrder.domain.models.testDriveRequest.TestDriveRequest;
import dealerShipOrder.domain.models.testDriveRequest.TestDriveStatus;
import dealerShipOrder.domain.models.users.User;
import dealerShipOrder.domain.models.users.client.Client;
import dealerShipOrder.domain.models.users.manager.Manager;
import domain.repository.carRepository.CarRepository;
import dealerShipOrder.infrastructure.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TestDriveManagerServiceImpl extends BaseTestDriveService implements TestDriveManagerService {

    public TestDriveManagerServiceImpl(
            TestDriveRequestRepository testDriveRepository,
            UserRepository userRepository,
            CarRepository carRepository,
            TestDriveMapper testDriveMapper) {
        super(testDriveRepository, userRepository, carRepository, testDriveMapper);
    }

    @Override
    public TestDriveResponse assignManager(String requestId) {
        String managerId = SecurityUtils.getCurrentUserId();
        User user = findUserById(managerId);
        if (!(user instanceof Manager manager)) {
            throw new DomainValidationException("User is not a manager");
        }

        TestDriveRequest request = findRequestById(requestId);

        request.assignManager(managerId);
        manager.assignToTestDrive(requestId);

        TestDriveRequest updated = saveRequest(request);
        userRepository.save(manager);

        Car car = findCarById(updated.getCarId());
        Client client = findClientById(updated.getClientId());

        return testDriveMapper.toResponse(
                updated,
                client.getFullName(),
                car.getCarInfo(),
                manager.getFullName()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<TestDriveResponse> getMyRequests() {
        String managerId = SecurityUtils.getCurrentUserId();
        User user = findUserById(managerId);
        if (!(user instanceof Manager manager)) {
            throw new DomainValidationException("User is not a manager");
        }

        return testDriveRepository.findByManagerId(managerId).stream()
                .map(req -> {
                    Car car = findCarById(req.getCarId());
                    Client client = findClientById(req.getClientId());
                    return testDriveMapper.toResponse(
                            req,
                            client.getFullName(),
                            car.getCarInfo(),
                            manager.getFullName()
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TestDriveResponse> getPendingRequests() {
        return testDriveRepository.findByStatus(TestDriveStatus.PENDING).stream()
                .map(req -> {
                    Car car = findCarById(req.getCarId());
                    Client client = findClientById(req.getClientId());
                    return testDriveMapper.toResponse(
                            req,
                            client.getFullName(),
                            car.getCarInfo(),
                            null
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    public TestDriveResponse confirmRequest(String requestId, LocalDateTime time) {
        String managerId = SecurityUtils.getCurrentUserId();
        User user = findUserById(managerId);
        if (!(user instanceof Manager manager)) {
            throw new DomainValidationException("User is not a manager");
        }

        TestDriveRequest request = findRequestById(requestId);

        if (!managerId.equals(request.getManagerId())) {
            throw new DomainValidationException("This request is not assigned to you");
        }

        if (testDriveRepository.hasConflict(request.getCarId(), time)) {
            throw new DomainValidationException("This time slot is already booked");
        }

        request.confirmTime(time);
        TestDriveRequest updated = saveRequest(request);

        Car car = findCarById(updated.getCarId());
        Client client = findClientById(updated.getClientId());

        return testDriveMapper.toResponse(
                updated,
                client.getFullName(),
                car.getCarInfo(),
                manager.getFullName()
        );
    }

    @Override
    public TestDriveResponse completeRequest(String requestId) {
        String managerId = SecurityUtils.getCurrentUserId();
        User user = findUserById(managerId);
        if (!(user instanceof Manager manager)) {
            throw new DomainValidationException("User is not a manager");
        }

        TestDriveRequest request = findRequestById(requestId);

        if (!managerId.equals(request.getManagerId())) {
            throw new DomainValidationException("This request is not assigned to you");
        }

        request.complete();
        manager.completeTestDrive(requestId);

        TestDriveRequest updated = saveRequest(request);
        userRepository.save(manager);

        Car car = findCarById(updated.getCarId());
        Client client = findClientById(updated.getClientId());

        return testDriveMapper.toResponse(
                updated,
                client.getFullName(),
                car.getCarInfo(),
                manager.getFullName()
        );
    }

    @Override
    public TestDriveResponse markNoShow(String requestId) {
        String managerId = SecurityUtils.getCurrentUserId();
        User user = findUserById(managerId);
        if (!(user instanceof Manager manager)) {
            throw new DomainValidationException("User is not a manager");
        }

        TestDriveRequest request = findRequestById(requestId);

        if (!managerId.equals(request.getManagerId())) {
            throw new DomainValidationException("This request is not assigned to you");
        }

        request.markNoShow();
        TestDriveRequest updated = saveRequest(request);

        Car car = findCarById(updated.getCarId());
        Client client = findClientById(updated.getClientId());

        return testDriveMapper.toResponse(
                updated,
                client.getFullName(),
                car.getCarInfo(),
                manager.getFullName()
        );
    }
}