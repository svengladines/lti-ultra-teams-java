package be.occam.lti.ultra.teams.web.controller;

import be.occam.lti.ultra.teams.config.feature.LocalProperties;
import be.occam.lti.ultra.teams.domain.service.LTIService;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.Issuer;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriUtils;

import java.net.URI;
import java.nio.charset.Charset;

@Controller
public class LoginController {

    public static final String LTI_LOGIN_PATH = "/ltiLogin";
    public static final String OIDC_LOGIN_PATH = "/oidc";
    public static final String LTI_LOGIN_PATH_LOCAL = "/ltiLoginLocal";

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected final LTIService ltiService;
    protected final LocalProperties localProperties;

    @Autowired
    public LoginController(LTIService ltiService, LocalProperties localProperties) {
        this.ltiService = ltiService;
        this.localProperties = localProperties;
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
        /*
        if (this.localProperties.enabled() ) {
            URI redirectURI = new DefaultUriBuilderFactory()
                    .builder()
                    .scheme("http")
                    .host("localhost")
                    .port(8080)
                    .path(LTI_LOGIN_PATH_LOCAL)
                    .queryParam("iss", UriUtils.encodeQueryParam(iss.getValue(), Charset.defaultCharset()))
                    .queryParam("target_link_uri", UriUtils.encodeQueryParam(targetLinkUri.toString(),Charset.defaultCharset()))
                    .queryParam("client_id", clientId.getValue())
                    .queryParam("login_hint", loginHint)
                    .queryParam("lti_message_hint", lti_message_hint)
                    .build();
            MultiValueMap<String,String> headers = new HttpHeaders();
            headers.add("Location", redirectURI.toString());
            return new ResponseEntity<>(headers, HttpStatus.TEMPORARY_REDIRECT);
        }
        else {
            URI redirect = ltiService.thirdPartyLogin(iss, targetLinkUri, clientId, loginHint, lti_message_hint, httpRequest);
            MultiValueMap<String,String> headers = new HttpHeaders();
            headers.add("Location", redirect.toString());
            return new ResponseEntity<>(headers, HttpStatus.TEMPORARY_REDIRECT);
        }
        */
        return "lti/login.html";
    }

    @GetMapping(value = LTI_LOGIN_PATH_LOCAL)
    public ResponseEntity<String> loginLocal(@RequestParam("iss") Issuer iss,
                        @RequestParam("target_link_uri") URI targetLinkUri,
                        @RequestParam("client_id") ClientID clientId,
                        @RequestParam("login_hint") String loginHint,
                        @RequestParam("lti_message_hint") String lti_message_hint,
                        HttpServletRequest httpRequest,
                        Model model) {
        URI redirect = ltiService.thirdPartyLogin(iss, targetLinkUri, clientId, loginHint, lti_message_hint, httpRequest);
        MultiValueMap<String,String> headers = new HttpHeaders();
        headers.add("Location", redirect.toString());
        return new ResponseEntity<>(headers, HttpStatus.TEMPORARY_REDIRECT);
    }

}
