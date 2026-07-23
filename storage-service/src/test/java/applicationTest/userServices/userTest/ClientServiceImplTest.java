package applicationTest.userServices.userTest;

import application.dtos.request.userRequest.UpdateUserRequest;
import application.dtos.request.userRequest.ChangePasswordRequest;
import application.dtos.response.orderResponse.OrderResponse;
import application.dtos.response.testDriveResponse.TestDriveResponse;
import application.dtos.response.userResponse.users.ClientResponse;
import application.mapper.UserMapper;
import application.services.userService.client.ClientServiceImpl;
import domain.exception.DomainValidationException;
import domain.exception.EntityNotFoundException;
import domain.models.order.Order;
import domain.models.testDriveRequest.TestDriveRequest;
import domain.models.users.client.Client;
import domain.models.users.manager.Manager;
import domain.repository.orderRepository.OrderRepository;
import domain.repository.testDriveRequestRepository.TestDriveRequestRepository;
import domain.repository.userRepository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClientService Tests")
class ClientServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private TestDriveRequestRepository testDriveRepository;

    @InjectMocks
    private ClientServiceImpl clientService;

    private Client client;
    private ClientResponse clientResponse;
    private UpdateUserRequest updateRequest;
    private ChangePasswordRequest changePasswordRequest;

    @BeforeEach
    void setUp() {
        client = new Client("client123", "John", "Doe", null, "john@email.com", "+1234567890", "password123");
        clientResponse = new ClientResponse();
        clientResponse.setId("client123");
        clientResponse.setFirstName("John");
        clientResponse.setLastName("Doe");

        updateRequest = new UpdateUserRequest();
        updateRequest.setFirstName("Jonathan");
        updateRequest.setLastName("Smith");
        updateRequest.setEmail("newemail@email.com");
        updateRequest.setPhone("+9876543210");
        updateRequest.setStatus("ACTIVE");
        updateRequest.setPreferredContactMethod("phone");
        updateRequest.setNewsletterSubscribed(true);

        changePasswordRequest = new ChangePasswordRequest();
        changePasswordRequest.setUserId("client123");
        changePasswordRequest.setOldPassword("password123");
        changePasswordRequest.setNewPassword("newPassword456");
    }

    @Test
    @DisplayName("Should get client by id successfully")
    void shouldGetClientByIdSuccessfully() {
        when(userRepository.findById("client123")).thenReturn(Optional.of(client));
        when(userMapper.toClientResponse(client)).thenReturn(clientResponse);

        ClientResponse result = clientService.getUserById("client123");

        assertNotNull(result);
        verify(userRepository, times(1)).findById("client123");
        verify(userMapper, times(1)).toClientResponse(client);
    }

    @Test
    @DisplayName("Should throw exception when client not found")
    void shouldThrowExceptionWhenClientNotFound() {
        when(userRepository.findById("client999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            clientService.getUserById("client999");
        });
    }

    @Test
    @DisplayName("Should throw exception when user is not a client")
    void shouldThrowExceptionWhenUserIsNotClient() {
        Manager manager = mock(Manager.class);
        when(userRepository.findById("manager123")).thenReturn(Optional.of(manager));

        assertThrows(DomainValidationException.class, () -> {
            clientService.getUserById("manager123");
        });
    }

    @Test
    @DisplayName("Should update client profile successfully")
    void shouldUpdateClientProfileSuccessfully() {
        when(userRepository.findById("client123")).thenReturn(Optional.of(client));
        doNothing().when(userMapper).updateDomain(client, updateRequest);
        when(userRepository.save(client)).thenReturn(client);
        when(userMapper.toClientResponse(client)).thenReturn(clientResponse);

        ClientResponse result = clientService.updateOwnProfile( updateRequest);

        assertNotNull(result);
        verify(userRepository, times(1)).save(client);
        verify(userMapper, times(1)).updateDomain(client, updateRequest);
    }


    @Test
    @DisplayName("Should change password successfully")
    void shouldChangePasswordSuccessfully() {
        Client clientSpy = spy(client);

        when(userRepository.findById("client123")).thenReturn(Optional.of(clientSpy));
        when(clientSpy.authenticate("password123")).thenReturn(true);
        when(userRepository.save(clientSpy)).thenReturn(clientSpy);
        when(userMapper.toClientResponse(clientSpy)).thenReturn(clientResponse);

        ClientResponse result = clientService.changeOwnPassword( changePasswordRequest);

        assertNotNull(result);
        verify(clientSpy, times(1)).changePassword("password123", "newPassword456");
        verify(userRepository, times(1)).save(clientSpy);
    }

    @Test
    @DisplayName("Should throw exception when user ID mismatch")
    void shouldThrowExceptionWhenUserIdMismatch() {
        changePasswordRequest.setUserId("otherClient");

        assertThrows(DomainValidationException.class, () -> {
            clientService.changeOwnPassword( changePasswordRequest);
        });
    }

    @Test
    @DisplayName("Should throw exception when old password is incorrect")
    void shouldThrowExceptionWhenOldPasswordIncorrect() {
        Client clientSpy = spy(client);

        when(userRepository.findById("client123")).thenReturn(Optional.of(clientSpy));
        when(clientSpy.authenticate("password123")).thenReturn(false);

        assertThrows(DomainValidationException.class, () -> {
            clientService.changeOwnPassword( changePasswordRequest);
        });
    }

    @Test
    @DisplayName("Should throw exception when client not found for password change")
    void shouldThrowExceptionWhenClientNotFoundForPasswordChange() {
        when(userRepository.findById("client999")).thenReturn(Optional.empty());
        changePasswordRequest.setUserId("client999");

        assertThrows(EntityNotFoundException.class, () -> {
            clientService.changeOwnPassword( changePasswordRequest);
        });
    }

    @Test
    @DisplayName("Should get client orders successfully")
    void shouldGetClientOrdersSuccessfully() {
        Order order = mock(Order.class);
        when(order.getId()).thenReturn("order123");
        when(order.getType()).thenReturn(domain.models.order.OrderType.IN_STOCK);
        when(order.getStatus()).thenReturn(domain.models.order.OrderStatus.PAID);
        when(order.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(order.getCarId()).thenReturn("car123");

        when(userRepository.findById("client123")).thenReturn(Optional.of(client));
        when(orderRepository.findByClientId("client123")).thenReturn(List.of(order));

        List<OrderResponse> result = clientService.getMyOrders();

        assertNotNull(result);
    }

    @Test
    @DisplayName("Should return empty list when client has no orders")
    void shouldReturnEmptyListWhenClientHasNoOrders() {
        when(userRepository.findById("client123")).thenReturn(Optional.of(client));
        when(orderRepository.findByClientId("client123")).thenReturn(List.of());

        List<OrderResponse> result = clientService.getMyOrders();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should get client test drives successfully")
    void shouldGetClientTestDrivesSuccessfully() {
        TestDriveRequest testDrive = mock(TestDriveRequest.class);
        when(testDrive.getId()).thenReturn("td123");
        when(testDrive.getStatus()).thenReturn(domain.models.testDriveRequest.TestDriveStatus.PENDING);
        when(testDrive.getRequestedTime()).thenReturn(LocalDateTime.now());
        when(testDrive.getCarId()).thenReturn("car123");

        when(userRepository.findById("client123")).thenReturn(Optional.of(client));
        when(testDriveRepository.findByClientId("client123")).thenReturn(List.of(testDrive));

        List<TestDriveResponse> result = clientService.getMyTestDrives();

        assertNotNull(result);
    }

    @Test
    @DisplayName("Should return empty list when client has no test drives")
    void shouldReturnEmptyListWhenClientHasNoTestDrives() {
        when(userRepository.findById("client123")).thenReturn(Optional.of(client));
        when(testDriveRepository.findByClientId("client123")).thenReturn(List.of());

        List<TestDriveResponse> result = clientService.getMyTestDrives();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should subscribe to newsletter successfully")
    void shouldSubscribeToNewsletterSuccessfully() {
        when(userRepository.findById("client123")).thenReturn(Optional.of(client));
        when(userRepository.save(client)).thenReturn(client);
        when(userMapper.toClientResponse(client)).thenReturn(clientResponse);

        ClientResponse result = clientService.subscribeToNewsletter();

        assertNotNull(result);
        assertTrue(client.isNewsletterSubscribed());
        verify(userRepository, times(1)).save(client);
    }

    @Test
    @DisplayName("Should unsubscribe from newsletter successfully")
    void shouldUnsubscribeFromNewsletterSuccessfully() {
        client.subscribeToNewsletter();
        assertTrue(client.isNewsletterSubscribed());

        when(userRepository.findById("client123")).thenReturn(Optional.of(client));
        when(userRepository.save(client)).thenReturn(client);
        when(userMapper.toClientResponse(client)).thenReturn(clientResponse);

        ClientResponse result = clientService.unsubscribeFromNewsletter();

        assertNotNull(result);
        assertFalse(client.isNewsletterSubscribed());
        verify(userRepository, times(1)).save(client);
    }

    @Test
    @DisplayName("Should set preferred contact method to email")
    void shouldSetPreferredContactMethodToEmail() {
        when(userRepository.findById("client123")).thenReturn(Optional.of(client));
        when(userRepository.save(client)).thenReturn(client);
        when(userMapper.toClientResponse(client)).thenReturn(clientResponse);

        ClientResponse result = clientService.setPreferredContactMethod("email");

        assertNotNull(result);
        assertEquals("email", client.getPreferredContactMethod());
        verify(userRepository, times(1)).save(client);
    }

    @Test
    @DisplayName("Should set preferred contact method to phone")
    void shouldSetPreferredContactMethodToPhone() {
        when(userRepository.findById("client123")).thenReturn(Optional.of(client));
        when(userRepository.save(client)).thenReturn(client);
        when(userMapper.toClientResponse(client)).thenReturn(clientResponse);

        ClientResponse result = clientService.setPreferredContactMethod("phone");

        assertNotNull(result);
        assertEquals("phone", client.getPreferredContactMethod());
        verify(userRepository, times(1)).save(client);
    }

    @Test
    @DisplayName("Should throw exception when setting invalid contact method")
    void shouldThrowExceptionWhenSettingInvalidContactMethod() {
        when(userRepository.findById("client123")).thenReturn(Optional.of(client));

        assertThrows(DomainValidationException.class, () -> {
            clientService.setPreferredContactMethod("invalid");
        });
    }

    @Test
    @DisplayName("Should throw exception when client not found for setting contact method")
    void shouldThrowExceptionWhenClientNotFoundForContactMethod() {
        when(userRepository.findById("client999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            clientService.setPreferredContactMethod("email");
        });
    }
}