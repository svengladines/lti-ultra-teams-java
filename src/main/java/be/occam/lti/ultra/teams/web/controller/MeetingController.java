package be.occam.lti.ultra.teams.web.controller;

import be.occam.lti.ultra.teams.config.SystemProperties;
import be.occam.lti.ultra.teams.config.feature.LocalProperties;
import be.occam.lti.ultra.teams.domain.LTIUser;
import be.occam.lti.ultra.teams.domain.TeamsMeeting;
import be.occam.lti.ultra.teams.domain.service.LTIService;
import be.occam.lti.ultra.teams.domain.service.MeetingService;
import be.occam.lti.ultra.teams.web.dto.MeetingDTO;
import com.azure.core.util.UrlBuilder;
import com.nimbusds.jwt.JWT;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;

@Controller
public class MeetingController {

    public static final String LAUNCH_PATH = "/meeting";
    public static final String LAUNCH_PATH_LOCAL = "/meetingLocal";
    public static final String PATH_CREATED = "/meeting/created";
    public static final String RESOURCE_PATH = "/api/meetings";

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected final LTIService ltiService;
    protected final MeetingService meetingService;
    protected final LocalProperties localProperties;
    protected final SystemProperties systemProperties;

    @Autowired
    public MeetingController(LTIService ltiService, MeetingService meetingService, LocalProperties localProperties, SystemProperties systemProperties) {
        this.ltiService = ltiService;
        this.meetingService = meetingService;
        this.localProperties = localProperties;
        this.systemProperties = systemProperties;
    }

    @PostMapping(value = LAUNCH_PATH)
    public ResponseEntity<String> launch(
            @RequestParam("id_token") String idToken,
            @RequestParam("state") String state,
            HttpServletRequest httpRequest,
            Model model
    ) {
        if (this.localProperties.enabled()) {
            URI redirectURI = new DefaultUriBuilderFactory()
                    .builder()
                    .scheme("http")
                    .host("localhost")
                    .port(8080)
                    .path(LAUNCH_PATH_LOCAL)
                    .build();
            MultiValueMap<String,String> headers = new HttpHeaders();
            headers.add("Location", redirectURI.toString());
            return new ResponseEntity<>(headers, HttpStatus.TEMPORARY_REDIRECT);
        }
        else {
            LTIUser ltiUser = this.ltiService.authenticated(idToken, state,httpRequest);
            logger.info("User [{}] with email [{}] logged in via LTI", ltiUser.userId(), ltiUser.email());
            MultiValueMap<String,String> headers = new HttpHeaders();
            headers.add("Location", "/pages/meeting/create");
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        }
    }

    @PostMapping(value = LAUNCH_PATH_LOCAL)
    public ResponseEntity<String> launchLocal(
            @RequestParam("id_token") String idToken,
            @RequestParam("state") String state,
            HttpServletRequest httpRequest,
            Model model) {
        // verify LTI claims, etc.
        LTIUser ltiUser = this.ltiService.authenticated(idToken, state,httpRequest);
        logger.info("User [{}] with email [{}] logged in via LTI", ltiUser.userId(), ltiUser.email());
        MultiValueMap<String,String> headers = new HttpHeaders();
        headers.add("Location", "/pages/meeting/create");
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @GetMapping(value = RESOURCE_PATH + "/{id}")
    public String get(
            @PathVariable("id") String id,
            PreAuthenticatedAuthenticationToken user) {
        TeamsMeeting meeting = this.meetingService.get(id);
        return "redirect:%s".formatted(meeting.joinURL());
    }

    @PostMapping(value = RESOURCE_PATH, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<String> post(
            @ModelAttribute MeetingDTO meetingDTO,
            PreAuthenticatedAuthenticationToken user,
            HttpServletRequest httpRequest,
            Model model) {

        // verify LTI claims, etc.
        LTIUser ltiUser = (LTIUser) user.getDetails();
        String subject = meetingDTO.getSubject();
        TeamsMeeting teamsMeeting = this.meetingService.create(ltiUser, subject, httpRequest);
        JWT jwt = this.ltiService.deepLinkingResponseToken(subject,teamsMeeting.url(),httpRequest);
        // now redirect to page that contains self-posting form, via iframe
        // first build url to self-posting form
        try {
            URL urlToForm = UrlBuilder
                    .parse(this.systemProperties.baseURL())
                    .setPath(PATH_CREATED)
                    .addQueryParameter("fiz", URLEncoder.encode(jwt.serialize(), Charset.defaultCharset()))
                    .toUrl();
            logger.info("url to self submitting form: [{}]", urlToForm);
            URL redirectURI = UrlBuilder
                    .parse(this.systemProperties.frameURL())
                    .addQueryParameter("toolHref", UriComponentsBuilder.fromUriString(urlToForm.toString()).build().encode().toString())
                    .toUrl();
            MultiValueMap<String,String> headers = new HttpHeaders();
            headers.add("Location", redirectURI.toString());
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping(value = PATH_CREATED)
    public String created(
            @RequestParam String fiz,
            HttpServletRequest httpRequest,
            Model model) {
        model.addAttribute("responseUrl", this.systemProperties.deeplinkURL());
        model.addAttribute("jwt", fiz);
        return "lti/deeplinking-meeting-created";
    }
}
