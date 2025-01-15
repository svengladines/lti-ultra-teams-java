package be.occam.lti.ultra.teams.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "occam.lti.ultra.system")
public record SystemProperties(
        String baseURL,
        String jwkID,
        String jwkPublic,
        String jwkPrivate
        ) {
}
