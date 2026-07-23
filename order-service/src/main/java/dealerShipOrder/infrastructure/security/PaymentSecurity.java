package dealerShipOrder.infrastructure.security;

import dealerShipOrder.application.services.paymentService.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component("paymentSecurity")
@RequiredArgsConstructor
public class PaymentSecurity {

    private final PaymentService paymentService;

    public boolean isOwner(String paymentId, Authentication authentication) {
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
            var payment = paymentService.getPaymentById(paymentId);
            return payment.getClientId().equals(currentUserId);
        } catch (Exception e) {
            return false;
        }
    }
}