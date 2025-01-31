package be.occam.lti.ultra.teams.util;

import com.azure.core.util.UrlBuilder;

import java.net.URL;

public class URLHelper {

    public static UrlBuilder builder(String urlString) {
        try {
            return UrlBuilder.parse(urlString);
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static UrlBuilder builder(URL from) {
        try {
            return UrlBuilder.parse(from);
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static URL url(UrlBuilder builder) {
        try {
            return builder.toUrl();
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
