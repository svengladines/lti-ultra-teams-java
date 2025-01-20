package be.occam.lti.ultra.teams.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "occam.lti.ultra.teams.system")
public record SystemProperties(
        String baseURL,
        String deeplinkingResponseURL,
        String jwkID,
        String jwkPublic,
        String jwkPrivate
        ) {
}
