package infrastructure.security;

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
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()

                .antMatchers(
                        "/api/cars",
                        "/api/cars/**",
                        "/api/spare-parts",
                        "/api/spare-parts/**",
                        "/api/test-drives",
                        "/api/test-drives/**",
                        "/api/payments",
                        "/api/payments/**",
                        "/api/orders",
                        "/api/orders/**",
                        "/api/users",
                        "/api/users/**",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/actuator/health"
                ).permitAll()

                .antMatchers("/api/admin/**").hasRole("SYSTEM_ADMIN")

                .antMatchers("/api/manager/**").hasRole("MANAGER")

                .antMatchers("/api/client/**").hasRole("CLIENT")

                .antMatchers("/api/warehouse/**").hasRole("WAREHOUSE_ADMIN")

                .anyRequest().authenticated()

                .and()
                .oauth2ResourceServer()
                .jwt()
                .jwtAuthenticationConverter(new KeycloakJwtAuthenticationConverter());

        return http.build();
    }
}