package dealerShipOrder.infrastructure.config;

import dealerShipOrder.infrastructure.security.KeycloakJwtAuthenticationConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final KeycloakJwtAuthenticationConverter keycloakJwtAuthenticationConverter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers("/api/admin/**").hasRole("SYSTEM_ADMIN")
                .antMatchers("/api/manager/**").hasAnyRole("MANAGER", "SYSTEM_ADMIN")
                .antMatchers("/api/client/**").hasRole("CLIENT")
                .antMatchers("/api/warehouse-admin/**").hasAnyRole("WAREHOUSE_ADMIN", "SYSTEM_ADMIN")
                .antMatchers(
                        "/api/orders",
                        "/api/orders/**",
                        "/api/payments",
                        "/api/payments/**",
                        "/api/test-drives",
                        "/api/test-drives/**",
                        "/api/users",
                        "/api/users/**",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/actuator/health"
                ).permitAll()
                .anyRequest().authenticated()
                .and()
                .oauth2ResourceServer()
                .jwt()
                .jwtAuthenticationConverter(keycloakJwtAuthenticationConverter);

        return http.build();
    }
}