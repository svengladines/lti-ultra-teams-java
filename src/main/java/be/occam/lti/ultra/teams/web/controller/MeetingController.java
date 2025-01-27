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

    public static final String RESOURCE_COLLECTION_PATH = "/api/meetings";
    public static final String RESOURCE_SINGLE_PATH = "/api/meetings/{id}";
    public static final String RESOURCE_SINGLE_VIEW_PATH = "/api/meetings/{id}.html";

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
    public String launch(
            @RequestParam("id_token") String idToken,
            @RequestParam("state") String state,
            HttpServletRequest httpRequest,
            Model model
    ) {
        /*
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
         */
            LTIUser ltiUser = this.ltiService.authenticated(idToken, state);
            logger.info("User [{}] with email [{}] logged in via cookieless LTI", ltiUser.userId(), ltiUser.email());
            /*
            MultiValueMap<String,String> headers = new HttpHeaders();
            headers.add("Location", "/pages/meeting/create");
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
             */
            model.addAttribute("userEmail", ltiUser.email());
            model.addAttribute("jwt", ltiUser.jwt().serialize());
            return "meeting/create";
            /*
        }
        */
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

    @GetMapping(value = RESOURCE_SINGLE_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<MeetingDTO> get(
            @PathVariable("id") String id,
            Model model) {
        TeamsMeeting meeting = this.meetingService.get(id);
        return new ResponseEntity<>(map(meeting),HttpStatus.OK);
    }

    @GetMapping(value = RESOURCE_SINGLE_VIEW_PATH, produces = MediaType.TEXT_HTML_VALUE)
    public String view(
            @PathVariable("id") String id,
            Model model) {
        TeamsMeeting meeting = this.meetingService.get(id);
        model.addAttribute("meeting", map(meeting));
        return "/meeting/view";
    }

    @PostMapping(value = RESOURCE_COLLECTION_PATH, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String postForm(
            @ModelAttribute MeetingDTO meetingDTO,
            HttpServletRequest httpRequest,
            Model model) {
        try {
            String subject = meetingDTO.getSubject();
            TeamsMeeting teamsMeeting = this.meetingService.create(meetingDTO.getUserEmail(), subject, httpRequest);
            JWT jwt = this.ltiService.deepLinkingResponseToken(subject,teamsMeeting.url(),meetingDTO.getJwt());
            model.addAttribute("jwt", jwt.serialize());
            // TODO, remove hardcoded, use value from launch request
            model.addAttribute("responseUrl", "%s/webapps/blackboard/controller/lti/v2/deeplinking".formatted(this.systemProperties.ultraURL()));
            return "lti/deeplinking-response";
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected static MeetingDTO map(TeamsMeeting from) {
        MeetingDTO to = new MeetingDTO();
        to.setId(from.id());
        to.setSubject(from.subject());
        to.setJoinUrl(from.joinURL());
        return to;
    }
}
