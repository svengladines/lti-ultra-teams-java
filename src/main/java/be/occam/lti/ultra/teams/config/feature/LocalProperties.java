package be.occam.lti.ultra.teams.config.feature;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "occam.lti.ultra.feature.local")
public record LocalProperties(boolean enabled) {
}
