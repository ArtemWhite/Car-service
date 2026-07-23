package dealerShipOrder.infrastructure.security;

import dealerShipOrder.application.services.orderService.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component("orderSecurity")
@RequiredArgsConstructor
public class OrderSecurity {

    private final OrderService orderService;

    public boolean isOwner(String orderId, Authentication authentication) {
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
            var order = orderService.getOrderById(orderId);
            return order.getClientId().equals(currentUserId);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isStrictOwner(String orderId, Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return false;
        }

        String currentUserId = jwt.getClaim("sub");
        if (currentUserId == null) {
            return false;
        }

        try {
            var order = orderService.getOrderById(orderId);
            return order.getClientId().equals(currentUserId);
        } catch (Exception e) {
            return false;
        }
    }
}