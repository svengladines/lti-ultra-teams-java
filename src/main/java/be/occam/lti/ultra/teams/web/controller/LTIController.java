package be.occam.lti.ultra.teams.web.controller;

import be.occam.lti.ultra.teams.config.SystemProperties;
import be.occam.lti.ultra.teams.config.feature.LocalProperties;
import be.occam.lti.ultra.teams.domain.LTILaunchType;
import be.occam.lti.ultra.teams.domain.LTILoginData;
import be.occam.lti.ultra.teams.domain.LTIUser;
import be.occam.lti.ultra.teams.domain.service.LTIService;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.Issuer;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Controller
public class LTIController {

    public static final String LTI_LOGIN_PATH = "/ltiLogin";
    public static final String LTI_LAUNCH_PATH_MEETING = "/meeting";

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected final LTIService ltiService;
    protected final LocalProperties localProperties;
    protected final SystemProperties systemProperties;

    @Autowired
    public LTIController(LTIService ltiService, LocalProperties localProperties, SystemProperties systemProperties) {
        this.ltiService = ltiService;
        this.localProperties = localProperties;
        this.systemProperties = systemProperties;
    }

    @GetMapping(value = LTI_LOGIN_PATH)
    public String login(@RequestParam("iss") Issuer iss,
                        @RequestParam("target_link_uri") URI targetLinkUri,
                        @RequestParam("client_id") ClientID clientId,
                        @RequestParam("login_hint") String loginHint,
                        @RequestParam("lti_message_hint") String lti_message_hint,
                        HttpServletRequest httpRequest,
                        Model model) {

        logger.info("LTI login with client id [{}]", clientId);
        LTILoginData ltiLoginData = ltiService.thirdPartyLogin(iss, clientId, loginHint, lti_message_hint, LTI_LAUNCH_PATH_MEETING, httpRequest);
        model.addAttribute("state", ltiLoginData.state());
        model.addAttribute("nonce", ltiLoginData.nonce());
        model.addAttribute("redirect", ltiLoginData.redirectUri().toString());
        model.addAttribute("ultraURL", this.systemProperties.ultraURL());
        return "lti/login.html";
    }

    @PostMapping(value = LTI_LAUNCH_PATH_MEETING)
    public String launchMeeting(
            @RequestParam("id_token") String idToken,
            @RequestParam("state") String state,
            HttpServletRequest httpRequest,
            Model model) {
        Map<String,Object> claims = new HashMap<>();
        LTIUser ltiUser = this.ltiService.authenticated(idToken, state,claims);
        logger.info("User [{}] with email [{}] logged in via cookieless LTI", ltiUser.userId(), ltiUser.email());
        LTILaunchType launchType = this.ltiService.launchType(claims);
        if (launchType.equals(LTILaunchType.DEEPLINKING_REQUEST)) {
            model.addAttribute("organizer", ltiUser.email());
            model.addAttribute("jwt", ltiUser.jwt().serialize());
            return "meeting/create";
        }
        else if (launchType.equals(LTILaunchType.RESOURCE_LINK_REQUEST)) {
            URL targetURL = this.ltiService.targetURL(claims);
            model.addAttribute("redirectUrl", targetURL.toString());
            return "lti/resource-link-response";
        }
        else {
            throw new RuntimeException("invalid message type");
        }
    }

}
