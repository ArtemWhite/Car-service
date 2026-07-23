package dealerShipOrder.applicationTest;

import dealerShipOrder.infrastructure.security.SecurityUtils;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.MockedStatic;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

public class WithMockSecurityExtension implements BeforeEachCallback, AfterEachCallback {
    private MockedStatic<SecurityUtils> mockedSecurity;

    @Override
    public void beforeEach(ExtensionContext context) {
        mockedSecurity = mockStatic(SecurityUtils.class);
        mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn("test-user-id");
        mockedSecurity.when(SecurityUtils::getCurrentJwt).thenReturn(null);
        mockedSecurity.when(() -> SecurityUtils.hasRole(anyString())).thenReturn(true);
        mockedSecurity.when(() -> SecurityUtils.getCurrentUserRoles()).thenReturn(java.util.List.of());
        mockedSecurity.when(SecurityUtils::getCurrentUserEmail).thenReturn("test@example.com");
    }

    @Override
    public void afterEach(ExtensionContext context) {
        if (mockedSecurity != null) {
            mockedSecurity.close();
        }
    }
}
