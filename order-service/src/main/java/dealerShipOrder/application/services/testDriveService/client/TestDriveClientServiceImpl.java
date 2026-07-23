package dealerShipOrder.application.services.testDriveService.client;

import dealerShipOrder.application.dtos.request.testDriveRequest.CreateTestDriveRequest;
import dealerShipOrder.application.dtos.response.testDriveResponse.TestDriveResponse;
import dealerShipOrder.application.mapper.TestDriveMapper;
import dealerShipOrder.application.services.testDriveService.BaseTestDriveService;
import dealerShipOrder.domain.repository.testDriveRequestRepository.testDriveRequestRepository.TestDriveRequestRepository;
import dealerShipOrder.domain.repository.usersRepository.userRepository.UserRepository;
import domain.exception.DomainValidationException;
import domain.models.car.Car;
import dealerShipOrder.domain.models.testDriveRequest.TestDriveRequest;
import dealerShipOrder.domain.models.users.User;
import dealerShipOrder.domain.models.users.client.Client;
import domain.repository.carRepository.CarRepository;
import infrastructure.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TestDriveClientServiceImpl extends BaseTestDriveService implements TestDriveClientService {

    public TestDriveClientServiceImpl(
            TestDriveRequestRepository testDriveRepository,
            UserRepository userRepository,
            CarRepository carRepository,
            TestDriveMapper testDriveMapper) {
        super(testDriveRepository, userRepository, carRepository, testDriveMapper);
    }

    @Override
    public TestDriveResponse createRequest(CreateTestDriveRequest request) {
        String clientId = SecurityUtils.getCurrentUserId();
        request.setClientId(clientId);

        User user = findUserById(clientId);
        if (!(user instanceof Client client)) {
            throw new DomainValidationException("User is not a client");
        }

        Car car = findCarById(request.getCarId());

        if (!car.isAvailableForTestDrive()) {
            throw new DomainValidationException("Car is not available for test drive");
        }

        if (testDriveRepository.hasConflict(request.getCarId(), request.getStartTime())) {
            throw new DomainValidationException("This time slot is already booked");
        }

        TestDriveRequest testDrive = testDriveMapper.toDomain(request);
        TestDriveRequest saved = saveRequest(testDrive);

        client.addTestDriveRequest(saved.getId());
        userRepository.save(client);

        return testDriveMapper.toResponse(
                saved,
                client.getFullName(),
                car.getCarInfo(),
                null
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<TestDriveResponse> getMyRequests() {
        String clientId = SecurityUtils.getCurrentUserId();
        User user = findUserById(clientId);
        if (!(user instanceof Client client)) {
            throw new DomainValidationException("User is not a client");
        }

        return testDriveRepository.findByClientId(clientId).stream()
                .map(req -> {
                    Car car = findCarById(req.getCarId());
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
    public TestDriveResponse cancelRequest(String requestId, String reason) {
        String clientId = SecurityUtils.getCurrentUserId();
        User user = findUserById(clientId);
        if (!(user instanceof Client client)) {
            throw new DomainValidationException("User is not a client");
        }

        TestDriveRequest request = findRequestById(requestId);

        if (!request.getClientId().equals(clientId)) {
            throw new DomainValidationException("Request does not belong to this client");
        }

        request.cancel();
        TestDriveRequest updated = saveRequest(request);

        Car car = findCarById(updated.getCarId());

        return testDriveMapper.toResponse(
                updated,
                client.getFullName(),
                car.getCarInfo(),
                updated.getManagerId() != null ? getManagerName(updated.getManagerId()) : null
        );
    }

    @Override
    public TestDriveResponse rescheduleRequest(String requestId, LocalDateTime newTime) {
        String clientId = SecurityUtils.getCurrentUserId();
        User user = findUserById(clientId);
        if (!(user instanceof Client client)) {
            throw new DomainValidationException("User is not a client");
        }

        TestDriveRequest request = findRequestById(requestId);

        if (!request.getClientId().equals(clientId)) {
            throw new DomainValidationException("Request does not belong to this client");
        }

        if (testDriveRepository.hasConflict(request.getCarId(), newTime)) {
            throw new DomainValidationException("New time slot is already booked");
        }

        request.reschedule(newTime);
        TestDriveRequest updated = saveRequest(request);

        Car car = findCarById(updated.getCarId());

        return testDriveMapper.toResponse(
                updated,
                client.getFullName(),
                car.getCarInfo(),
                updated.getManagerId() != null ? getManagerName(updated.getManagerId()) : null
        );
    }
}