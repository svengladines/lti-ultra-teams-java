package be.occam.lti.ultra.teams.web;

import be.occam.lti.ultra.teams.domain.service.LTIService;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.Issuer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class LTIController {

    public static final String LTI_LOGIN_PATH = "/login";
    public static final String LTI_LAUNCH_PATH = "/launch";

    protected final LTIService ltiService;

    @Autowired
    public LTIController(LTIService ltiService) {
        this.ltiService = ltiService;
    }

    @GetMapping(value = LTI_LOGIN_PATH)
    public void login( @RequestParam Issuer iss,
                       @RequestParam("target_link_uri") URI targetLinkUri,
                       @RequestParam ClientID clientId,
                       @RequestParam("login_hint") String loginHint,
                       @RequestParam String lti_message_hint,
                       Model model) {
        ltiService.thirdPartyLogin(iss, targetLinkUri, clientId, loginHint, lti_message_hint);
    }

    @PostMapping(value = LTI_LAUNCH_PATH)
    public void launch() {

    }
}
