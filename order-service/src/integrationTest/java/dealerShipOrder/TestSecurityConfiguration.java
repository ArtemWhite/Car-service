package dealerShipOrder;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

@TestConfiguration
public class TestSecurityConfiguration {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain testFilterChain(HttpSecurity http, JdbcTemplate jdbcTemplate) throws Exception {
        http
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilterBefore(new XUserHeaderFilter(jdbcTemplate), AnonymousAuthenticationFilter.class)
                .authorizeRequests()
                .antMatchers("/api/admin/**").hasRole("SYSTEM_ADMIN")
                .antMatchers("/api/manager/**").hasAnyRole("MANAGER", "SYSTEM_ADMIN")
                .antMatchers("/api/client/**").hasRole("CLIENT")
                .antMatchers("/api/warehouse-admin/**").hasAnyRole("WAREHOUSE_ADMIN", "SYSTEM_ADMIN")
                .antMatchers("/api/auth/**", "/api/orders/**", "/api/payments/**", "/api/test-drives/**",
                        "/api/users/**", "/api/v1/**", "/swagger-ui/**", "/v3/api-docs/**",
                        "/actuator/health").permitAll()
                .anyRequest().authenticated();

        return http.build();
    }
}
