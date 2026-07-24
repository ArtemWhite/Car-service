package dealerShipOrder.presentation.controllers.userControllers;

import dealerShipOrder.domain.repository.usersRepository.userRepository.UserRepository;
import dealerShipOrder.domain.models.users.User;
import dealerShipOrder.domain.models.users.UserStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication operations")
public class AuthController {

    private final UserRepository userRepository;
    private final Map<String, TokenInfo> activeTokens = new ConcurrentHashMap<>();

    @PostMapping("/login")
    @Operation(summary = "Authenticate user")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");

        if (email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email and password are required"));
        }

        Optional<User> userOpt = userRepository.findByEmailAndPassword(email, password);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }

        User user = userOpt.get();

        if (user.getStatus() == UserStatus.BLOCKED) {
            return ResponseEntity.status(403).body(Map.of("error", "Account is blocked"));
        }

        if (user.getStatus() == UserStatus.INACTIVE) {
            return ResponseEntity.status(403).body(Map.of("error", "Account is inactive"));
        }

        if (user.getLastPasswordChangeAt() != null) {
            long daysSinceChange = ChronoUnit.DAYS.between(user.getLastPasswordChangeAt(), LocalDateTime.now());
            if (daysSinceChange > 90) {
                return ResponseEntity.status(403).body(Map.of("error", "Password expired, please change your password"));
            }
        }

        user.updateLastActive();
        userRepository.save(user);

        String token = UUID.randomUUID().toString();
        activeTokens.put(token, new TokenInfo(user.getId(), user.getEmail(), user.getUserType().name()));

        return ResponseEntity.ok(Map.of(
                "userId", user.getId(),
                "userType", user.getUserType().name(),
                "status", user.getStatus().name(),
                "token", token
        ));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            activeTokens.remove(token);
        }
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    public boolean isTokenValid(String token) {
        return activeTokens.containsKey(token);
    }

    public TokenInfo getTokenInfo(String token) {
        return activeTokens.get(token);
    }

    public record TokenInfo(String userId, String email, String userType) {}
}
