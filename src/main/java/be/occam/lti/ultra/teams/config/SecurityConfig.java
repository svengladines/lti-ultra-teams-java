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

    protected  final SystemProperties systemProperties;

    public SecurityConfig(SystemProperties systemProperties) {
        this.systemProperties = systemProperties;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(c -> c.disable())
            .headers(c -> c.contentSecurityPolicy( p -> p.policyDirectives("frame-ancestors 'self' %s".formatted(this.systemProperties.ultraURL()))))

            .authorizeHttpRequests(authz -> authz.requestMatchers("/actuator/health").permitAll())

            .authorizeHttpRequests(authz -> authz.requestMatchers("/error").permitAll())
            .authorizeHttpRequests(authz -> authz.requestMatchers("/favicon.ico").permitAll())
            .authorizeHttpRequests(authz -> authz.requestMatchers("/javascript/*").permitAll())
            .authorizeHttpRequests(authz -> authz.requestMatchers("/login/*").permitAll())
            .authorizeHttpRequests(authz -> authz.requestMatchers("/ltiLogin").permitAll())
            .authorizeHttpRequests(authz -> authz.requestMatchers("/jwks").permitAll())

            // permit all (cookieless and therefore sessionless and therefore not authenticated)
            .authorizeHttpRequests(authz -> authz.requestMatchers("/meeting").permitAll())
                .authorizeHttpRequests(authz -> authz.requestMatchers("/meeting/**").permitAll())
            .authorizeHttpRequests(authz -> authz.requestMatchers("/api/meetings").permitAll())
            .authorizeHttpRequests(authz -> authz.requestMatchers("/api/meetings/**").permitAll())

                // only for development
            .authorizeHttpRequests(authz -> authz.requestMatchers("/local/**").permitAll())
            .build();
    }

}
