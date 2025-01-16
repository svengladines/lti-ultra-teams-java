package be.occam.lti.ultra.teams.web.controller;

import be.occam.lti.ultra.teams.config.feature.LocalProperties;
import be.occam.lti.ultra.teams.domain.LTIUser;
import be.occam.lti.ultra.teams.domain.TeamsMeeting;
import be.occam.lti.ultra.teams.domain.service.LTIService;
import be.occam.lti.ultra.teams.domain.service.MeetingService;
import be.occam.lti.ultra.teams.web.dto.MeetingDTO;
import com.nimbusds.jwt.JWT;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.web.util.UriUtils;

import java.net.URI;
import java.nio.charset.Charset;

@Controller
public class MeetingController {

    public static final String LAUNCH_PATH = "/meeting";
    public static final String LAUNCH_PATH_LOCAL = "/meetingLocal";

    public static final String RESOURCE_PATH = "/api/meetings";

    protected final LTIService ltiService;
    protected final MeetingService meetingService;
    protected final LocalProperties localProperties;

    @Autowired
    public MeetingController(LTIService ltiService, MeetingService meetingService, LocalProperties localProperties) {
        this.ltiService = ltiService;
        this.meetingService = meetingService;
        this.localProperties = localProperties;
    }

    @PostMapping(value = LAUNCH_PATH)
    public ResponseEntity launch(
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
            MultiValueMap<String,String> headers = new HttpHeaders();
            headers.add("Location", "/pages/meeting/create");
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        }
    }

    @PostMapping(value = LAUNCH_PATH_LOCAL)
    public String launchLocal(
            @RequestParam("id_token") String idToken,
            @RequestParam("state") String state,
            HttpServletRequest httpRequest,
            Model model) {
        // verify LTI claims, etc.
        LTIUser ltiUser = this.ltiService.authenticated(idToken, state,httpRequest);
        return "meeting/create";
    }

    @GetMapping(value = RESOURCE_PATH + "/{id}")
    public String get(
            @PathVariable("id") String id,
            PreAuthenticatedAuthenticationToken user) {
        TeamsMeeting meeting = this.meetingService.get(id);
        return "redirect:%s".formatted(meeting.joinURL());
    }

    @PostMapping(value = RESOURCE_PATH, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String post(
            @ModelAttribute MeetingDTO meetingDTO,
            PreAuthenticatedAuthenticationToken user,
            HttpServletRequest httpRequest,
            Model model) {

        // verify LTI claims, etc.
        LTIUser ltiUser = (LTIUser) user.getDetails();
        String subject = meetingDTO.getSubject();
        TeamsMeeting teamsMeeting = this.meetingService.create(ltiUser, subject, httpRequest);
        JWT jwt = this.ltiService.deepLinkingResponseToken(subject,teamsMeeting.url(),httpRequest);
        model.addAttribute("responseUrl", this.ltiService.deepLinkingResponseURL(httpRequest));
        model.addAttribute("jwt", jwt.serialize());
        return "lti/deeplinking-meeting-created";
    }
}
