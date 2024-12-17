package be.occam.lti.ultra.teams.infrastructure.microsoft;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "occam.lti.ultra.infrastructure.microsoft")
public record MicrosoftProperties(
       String tenantId,
       String clientId,
       String clientSecret,
       String scopes) {
}
