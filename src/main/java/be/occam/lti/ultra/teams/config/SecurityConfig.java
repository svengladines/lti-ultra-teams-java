package be.occam.lti.ultra.teams.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(c -> c.disable())
            .authorizeHttpRequests(authz -> authz.requestMatchers("/**").hasAuthority("SCOPE_toledo-isp.readwrite"))
            .authorizeHttpRequests(authz -> authz
                    .anyRequest()
                    .authenticated())
                .oauth2Login(Customizer.withDefaults())
            .build();
    }

}
