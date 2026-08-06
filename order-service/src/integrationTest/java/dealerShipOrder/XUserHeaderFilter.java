package dealerShipOrder;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

public class XUserHeaderFilter extends OncePerRequestFilter {

    private final JdbcTemplate jdbcTemplate;

    private static final Map<String, String> USER_TYPE_TO_ROLE = Map.of(
            "SYSTEM_ADMIN", "SYSTEM_ADMIN",
            "MANAGER", "MANAGER",
            "CLIENT", "CLIENT",
            "WAREHOUSE_ADMIN", "WAREHOUSE_ADMIN"
    );

    public XUserHeaderFilter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String userId = request.getHeader("X-User-Id");
        String uri = request.getRequestURI();

        String authHeader = request.getHeader("Authorization");
        
        if (userId == null || userId.isBlank()) {
            if (uri.equals("/api/users/me")) {
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Invalid or expired token\"}");
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Missing X-User-Id header\"}");
                }
                return;
            }
        } else {
            try {
                UUID uuid = UUID.fromString(userId);
                Map<String, Object> userData = jdbcTemplate.queryForMap(
                        "SELECT ut.name as role, us.name as status FROM users u " +
                                "JOIN user_types ut ON u.user_type_id = ut.id " +
                                "JOIN user_statuses us ON u.status_id = us.id " +
                                "WHERE u.id = ?::uuid AND u.removed = false", uuid);

                String role = (String) userData.get("role");
                String status = (String) userData.get("status");

                if ("BLOCKED".equalsIgnoreCase(status)) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Account is blocked\"}");
                    return;
                }

                if ("INACTIVE".equalsIgnoreCase(status) && !uri.equals("/api/users/me")) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Account is inactive\"}");
                    return;
                }

                if (role != null) {
                    String springRole = USER_TYPE_TO_ROLE.getOrDefault(role, role);

                    Jwt jwt = Jwt.withTokenValue("test-token-" + userId)
                            .header("alg", "none")
                            .claim("sub", userId)
                            .claim("realm_access", Map.of("roles", List.of(springRole)))
                            .issuedAt(Instant.now())
                            .expiresAt(Instant.now().plusSeconds(3600))
                            .build();

                    Collection<GrantedAuthority> authorities = List.of(
                            new SimpleGrantedAuthority("ROLE_" + springRole));
                    JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt, authorities);
                    auth.setAuthenticated(true);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (org.springframework.dao.EmptyResultDataAccessException e) {
                if (uri.startsWith("/api/users/me")) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"User not found\"}");
                    return;
                }
            } catch (Exception e) {
                // Invalid UUID format or other error
            }
        }

        filterChain.doFilter(request, response);
    }
}
