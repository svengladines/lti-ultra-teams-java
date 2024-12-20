package be.occam.lti.ultra.teams.domain;

import java.net.URL;

public record LTIContentItem(
        String type,
        String title,
        URL url) {

    @Override
    public String toString() {
        return """
                {
                "type":"%s",
                "title":%s,
                "url":%s
                }
                """.formatted(type(),title(),url().toString());
    }
}
