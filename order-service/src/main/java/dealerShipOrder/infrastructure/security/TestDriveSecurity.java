package dealerShipOrder.infrastructure.security;

import dealerShipOrder.application.services.testDriveService.TestDriveService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component("testDriveSecurity")
@RequiredArgsConstructor
public class TestDriveSecurity {

    private final TestDriveService testDriveService;

    public boolean isOwner(String requestId, Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return false;
        }

        String currentUserId = jwt.getClaim("sub");
        if (currentUserId == null) {
            return false;
        }

        boolean isAdminOrManager = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SYSTEM_ADMIN") ||
                        a.getAuthority().equals("ROLE_MANAGER"));

        if (isAdminOrManager) {
            return true;
        }

        try {
            var request = testDriveService.getTestDriveById(requestId);
            return request.getClientId().equals(currentUserId);
        } catch (Exception e) {
            return false;
        }
    }
}