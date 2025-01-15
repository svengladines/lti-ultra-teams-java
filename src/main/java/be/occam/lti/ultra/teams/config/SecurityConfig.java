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
            .authorizeHttpRequests(authz -> authz.requestMatchers("/favicon.ico").permitAll())

            .authorizeHttpRequests(authz -> authz.requestMatchers("/ltiLogin").permitAll())
            .authorizeHttpRequests(authz -> authz.requestMatchers("/ltiLoginLocal").permitAll())
            .authorizeHttpRequests(authz -> authz.requestMatchers("/login/*").permitAll())
            .authorizeHttpRequests(authz -> authz.requestMatchers("/jwks").permitAll())
            // permit all as it is the local instance that is authenticated
            .authorizeHttpRequests(authz -> authz.requestMatchers("/meeting").permitAll())
            .authorizeHttpRequests(authz -> authz.requestMatchers("/meetingLocal").permitAll())
            .authorizeHttpRequests(authz -> authz.requestMatchers("/meeting/create").authenticated())
            .build();
    }

}
