package be.occam.lti.ultra.teams;

import be.occam.lti.ultra.teams.infrastructure.microsoft.MicrosoftProperties;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({MicrosoftProperties.class})
public class UltraTeamsApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .bannerMode(Banner.Mode.OFF)
                .sources(UltraTeamsApplication.class)
                .run(args);
    }
}
