package be.occam.lti.ultra.teams.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "occam.lti.ultra.teams.system")
public record SystemProperties(
        String baseURL,
        String frameURL,
        String ultraURL,
        String deeplinkURL,
        String ltiLaunchPath,
        String jwkId,
        String jwkPublic,
        String jwkPrivate
        ) {
}
