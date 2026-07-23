package dealerShipOrder.applicationTest.userServices;

import dealerShipOrder.application.services.userService.BaseUserService;
import dealerShipOrder.domain.models.expection.EntityNotFoundException;
import dealerShipOrder.domain.models.users.User;
import dealerShipOrder.domain.repository.usersRepository.userRepository.UserRepository;
import dealerShipOrder.application.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import dealerShipOrder.applicationTest.WithMockSecurityExtension;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith({MockitoExtension.class, WithMockSecurityExtension.class})
@DisplayName("BaseUserService Tests")
class BaseUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    private TestBaseUserService baseService;
    private User user;

    @BeforeEach
    void setUp() {
        baseService = new TestBaseUserService(userRepository, userMapper);
        user = mock(User.class);
    }

    private static class TestBaseUserService extends BaseUserService {
        public TestBaseUserService(UserRepository userRepository, UserMapper userMapper) {
            super(userRepository, userMapper);
        }

        public User testFindUserById(String id) {
            return findUserById(id);
        }

        public User testSaveUser(User user) {
            return saveUser(user);
        }
    }

    @Test
    @DisplayName("Should find user by id successfully")
    void shouldFindUserByIdSuccessfully() {
        when(userRepository.findById("user123")).thenReturn(Optional.of(user));

        User result = baseService.testFindUserById("user123");

        assertNotNull(result);
        assertEquals(user, result);
        verify(userRepository, times(1)).findById("user123");
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findById("user999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            baseService.testFindUserById("user999");
        });
        verify(userRepository, times(1)).findById("user999");
    }

    @Test
    @DisplayName("Should save user successfully")
    void shouldSaveUserSuccessfully() {
        when(userRepository.save(user)).thenReturn(user);

        User result = baseService.testSaveUser(user);

        assertNotNull(result);
        assertEquals(user, result);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("Should return saved user")
    void shouldReturnSavedUser() {
        User savedUser = mock(User.class);
        when(userRepository.save(user)).thenReturn(savedUser);

        User result = baseService.testSaveUser(user);

        assertEquals(savedUser, result);
        verify(userRepository, times(1)).save(user);
    }
}