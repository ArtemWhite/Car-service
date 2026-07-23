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

        if (userId != null && !userId.isBlank()) {
            try {
                UUID uuid = UUID.fromString(userId);
                String role = jdbcTemplate.queryForObject(
                        "SELECT ut.name FROM users u JOIN user_types ut ON u.user_type_id = ut.id WHERE u.id = ?::uuid",
                        String.class, uuid);

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
            } catch (Exception e) {
                // User not found or invalid UUID — leave unauthenticated
            }
        }

        filterChain.doFilter(request, response);
    }
}
