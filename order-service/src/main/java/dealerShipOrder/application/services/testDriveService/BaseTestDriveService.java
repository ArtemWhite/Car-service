package dealerShipOrder.application.services.testDriveService;

import dealerShipOrder.domain.repository.testDriveRequestRepository.testDriveRequestRepository.TestDriveRequestRepository;
import dealerShipOrder.domain.repository.usersRepository.userRepository.UserRepository;
import dealerShipOrder.domain.models.expection.DomainValidationException;
import dealerShipOrder.domain.models.expection.EntityNotFoundException;
import domain.models.car.Car;
import dealerShipOrder.domain.models.testDriveRequest.TestDriveRequest;
import dealerShipOrder.domain.models.users.User;
import dealerShipOrder.domain.models.users.client.Client;
import dealerShipOrder.domain.models.users.manager.Manager;
import domain.repository.carRepository.CarRepository;
import dealerShipOrder.application.mapper.TestDriveMapper;

public abstract class BaseTestDriveService {

    protected final TestDriveRequestRepository testDriveRepository;
    protected final UserRepository userRepository;
    protected final CarRepository carRepository;
    protected final TestDriveMapper testDriveMapper;

    public BaseTestDriveService(
            TestDriveRequestRepository testDriveRepository,
            UserRepository userRepository,
            CarRepository carRepository,
            TestDriveMapper testDriveMapper) {
        this.testDriveRepository = testDriveRepository;
        this.userRepository = userRepository;
        this.carRepository = carRepository;
        this.testDriveMapper = testDriveMapper;
    }

    protected TestDriveRequest findRequestById(String id) {
        return testDriveRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Test drive request not found with id: " + id));
    }

    protected User findUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
    }

    protected Client findClientById(String clientId) {
        User user = findUserById(clientId);
        if (!(user instanceof Client client)) {
            throw new DomainValidationException("User is not a client: " + clientId);
        }
        return client;
    }

    protected String getManagerName(String managerId) {
        if (managerId == null) return null;
        Manager manager = findManagerById(managerId);
        return manager.getFullName();
    }

    protected Manager findManagerById(String managerId) {
        User user = findUserById(managerId);
        if (!(user instanceof Manager manager)) {
            throw new DomainValidationException("User is not a manager: " + managerId);
        }
        return manager;
    }

    protected Car findCarById(String carId) {
        return carRepository.findById(carId)
                .orElseThrow(() -> new EntityNotFoundException("Car not found with id: " + carId));
    }

    protected TestDriveRequest saveRequest(TestDriveRequest request) {
        return testDriveRepository.save(request);
    }

    protected String getClientName(String clientId) {
        Client client = findClientById(clientId);
        return client.getFullName();
    }

    protected void checkCarAvailableForTestDrive(String carId) {
        Car car = findCarById(carId);
        if (!car.isAvailableForTestDrive()) {
            throw new DomainValidationException("Car is not available for test drive");
        }
    }

    protected void checkRequestBelongsToClient(TestDriveRequest request, String clientId) {
        if (!request.getClientId().equals(clientId)) {
            throw new DomainValidationException("Test drive request does not belong to this client");
        }
    }

    protected void checkRequestAssignedToManager(TestDriveRequest request, String managerId) {
        if (!managerId.equals(request.getManagerId())) {
            throw new DomainValidationException("This request is not assigned to this manager");
        }
    }
}