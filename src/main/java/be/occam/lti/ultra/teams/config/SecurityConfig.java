package be.occam.lti.ultra.teams.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(c -> c.disable())
            .authorizeHttpRequests(authz -> authz.requestMatchers("/actuator/health").permitAll())
            .authorizeHttpRequests(authz -> authz.requestMatchers("/error").permitAll())
            .authorizeHttpRequests(authz -> authz.requestMatchers("/ltiLogin").permitAll())
            .authorizeHttpRequests(authz -> authz.requestMatchers("/ltiLoginLocal").permitAll())
            // temporary!
            .authorizeHttpRequests(authz -> authz.requestMatchers("/ltiLaunch").permitAll())
            .build();
    }

}
