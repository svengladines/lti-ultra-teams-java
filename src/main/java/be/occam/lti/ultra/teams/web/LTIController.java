package be.occam.lti.ultra.teams.web;

import be.occam.lti.ultra.teams.domain.service.LTIService;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.Issuer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;
import org.yaml.snakeyaml.util.UriEncoder;

import java.net.URI;
import java.nio.charset.Charset;

@Controller
public class LTIController {

    public static final String LTI_LOGIN_PATH = "/ltiLogin";
    public static final String LTI_LAUNCH_PATH = "/ltiLaunch";

    protected final LTIService ltiService;

    @Autowired
    public LTIController(LTIService ltiService) {
        this.ltiService = ltiService;
    }

    @GetMapping(value = LTI_LOGIN_PATH)
    public String login(@RequestParam("iss") Issuer iss,
                        @RequestParam("target_link_uri") URI targetLinkUri,
                        @RequestParam("client_id") ClientID clientId,
                        @RequestParam("login_hint") String loginHint,
                        @RequestParam("lti_message_hint") String lti_message_hint,
                        Model model) {
        // ltiService.thirdPartyLogin(iss, targetLinkUri, clientId, loginHint, lti_message_hint);
        URI redirectURI = new DefaultUriBuilderFactory()
                .builder()
                .scheme("http")
                .host("localhost")
                .port(8080)
                .path("%s%s".formatted(LTI_LOGIN_PATH,"Local"))
                .queryParam("iss", UriUtils.encodeQueryParam(iss.getValue(), Charset.defaultCharset()))
                .queryParam("target_link_uri", UriUtils.encodeQueryParam(targetLinkUri.toString(),Charset.defaultCharset()))
                .queryParam("client_id", clientId.getValue())
                .queryParam("login_hint", loginHint)
                .queryParam("lti_message_hint", lti_message_hint)
                .build();
        return "redirect:%s".formatted(redirectURI.toString());
    }

    @GetMapping(value = LTI_LOGIN_PATH + "Local")
    public void loginLocal(@RequestParam("iss") Issuer iss,
                        @RequestParam("target_link_uri") URI targetLinkUri,
                        @RequestParam("client_id") ClientID clientId,
                        @RequestParam("login_hint") String loginHint,
                        @RequestParam("lti_message_hint") String lti_message_hint,
                         Model model) {
        ltiService.thirdPartyLogin(iss, targetLinkUri, clientId, loginHint, lti_message_hint);
    }

    @PostMapping(value = LTI_LAUNCH_PATH)
    public void launch() {

    }
}
