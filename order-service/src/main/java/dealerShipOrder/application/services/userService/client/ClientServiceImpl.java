package dealerShipOrder.application.services.userService.client;

import dealerShipOrder.application.dtos.request.userRequest.ChangePasswordRequest;
import dealerShipOrder.application.dtos.request.userRequest.UpdateUserRequest;
import dealerShipOrder.application.dtos.response.orderResponse.OrderResponse;
import dealerShipOrder.application.dtos.response.testDriveResponse.TestDriveResponse;
import dealerShipOrder.application.dtos.response.userResponse.users.ClientResponse;
import dealerShipOrder.application.mapper.UserMapper;
import dealerShipOrder.application.services.userService.BaseUserService;
import dealerShipOrder.domain.models.expection.DomainValidationException;
import dealerShipOrder.domain.models.users.User;
import dealerShipOrder.domain.models.users.client.Client;
import dealerShipOrder.domain.repository.orderRepository.OrderRepository;
import dealerShipOrder.domain.repository.testDriveRequestRepository.testDriveRequestRepository.TestDriveRequestRepository;
import dealerShipOrder.domain.repository.usersRepository.userRepository.UserRepository;
import dealerShipOrder.infrastructure.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class ClientServiceImpl extends BaseUserService implements ClientService {

    private final OrderRepository orderRepository;
    private final TestDriveRequestRepository testDriveRepository;

    public ClientServiceImpl(
            UserRepository userRepository,
            UserMapper userMapper,
            OrderRepository orderRepository,
            TestDriveRequestRepository testDriveRepository) {
        super(userRepository, userMapper);
        this.orderRepository = orderRepository;
        this.testDriveRepository = testDriveRepository;
    }

    private Client getCurrentClient() {
        String clientId = SecurityUtils.getCurrentUserId();
        User user = findUserById(clientId);
        if (!(user instanceof Client client)) {
            throw new DomainValidationException("User is not a client");
        }
        return client;
    }

    @Override
    public ClientResponse getMyProfile() {
        Client client = getCurrentClient();
        return userMapper.toClientResponse(client);
    }

    @Override
    public ClientResponse getUserById(String id) {
        Client client = getCurrentClient();
        return userMapper.toClientResponse(client);
    }

    @Override
    public ClientResponse updateOwnProfile(UpdateUserRequest request) {
        Client client = getCurrentClient();
        userMapper.updateDomain(client, request);
        Client updated = (Client) saveUser(client);
        log.info("Client {} updated own profile", client.getId());
        return userMapper.toClientResponse(updated);
    }

    @Override
    public ClientResponse changeOwnPassword(ChangePasswordRequest request) {
        Client client = getCurrentClient();
        if (!client.authenticate(request.getOldPassword())) {
            throw new DomainValidationException("Old password is incorrect");
        }
        client.changePassword(request.getOldPassword(), request.getNewPassword());
        Client updated = (Client) saveUser(client);
        log.info("Client {} changed password", client.getId());
        return userMapper.toClientResponse(updated);
    }

    @Override
    public List<OrderResponse> getMyOrders() {
        String clientId = SecurityUtils.getCurrentUserId();
        findUserById(clientId);

        return orderRepository.findByClientId(clientId).stream()
                .map(order -> {
                    OrderResponse response = new OrderResponse();
                    response.setId(order.getId());
                    response.setOrderType(order.getType().name());
                    response.setStatus(order.getStatus().name());
                    response.setCreatedAt(order.getCreatedAt());
                    response.setCarId(order.getCarId());
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<TestDriveResponse> getMyTestDrives() {
        String clientId = SecurityUtils.getCurrentUserId();
        findUserById(clientId);

        return testDriveRepository.findByClientId(clientId).stream()
                .map(td -> {
                    TestDriveResponse response = new TestDriveResponse();
                    response.setId(td.getId());
                    response.setStatus(td.getStatus().name());
                    response.setRequestedTime(td.getRequestedTime());
                    response.setCarId(td.getCarId());
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    public ClientResponse subscribeToNewsletter() {
        Client client = getCurrentClient();
        client.subscribeToNewsletter();
        Client updated = (Client) saveUser(client);
        log.info("Client {} subscribed to newsletter", client.getId());
        return userMapper.toClientResponse(updated);
    }

    @Override
    public ClientResponse unsubscribeFromNewsletter() {
        Client client = getCurrentClient();
        client.unsubscribeFromNewsletter();
        Client updated = (Client) saveUser(client);
        log.info("Client {} unsubscribed from newsletter", client.getId());
        return userMapper.toClientResponse(updated);
    }

    @Override
    public ClientResponse setPreferredContactMethod(String method) {
        Client client = getCurrentClient();
        client.setPreferredContactMethod(method);
        Client updated = (Client) saveUser(client);
        log.info("Client {} set preferred contact method to: {}", client.getId(), method);
        return userMapper.toClientResponse(updated);
    }
}