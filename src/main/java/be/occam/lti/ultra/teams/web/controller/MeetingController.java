package be.occam.lti.ultra.teams.web;

import be.occam.lti.ultra.teams.config.feature.LocalProperties;
import be.occam.lti.ultra.teams.domain.LTIUser;
import be.occam.lti.ultra.teams.domain.TeamsMeeting;
import be.occam.lti.ultra.teams.domain.service.LTIService;
import be.occam.lti.ultra.teams.domain.service.MeetingService;
import com.nimbusds.jwt.JWT;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MeetingController {

    public static final String PATH = "/meeting";
    public static final String PATH_LOCAL = "/meetingLocal";
    // #TODO not REST!
    public static final String PATH_GET = "/meetings/{id}";

    protected final LTIService ltiService;
    protected final MeetingService meetingService;
    protected final LocalProperties localProperties;

    @Autowired
    public MeetingController(LTIService ltiService, MeetingService meetingService, LocalProperties localProperties) {
        this.ltiService = ltiService;
        this.meetingService = meetingService;
        this.localProperties = localProperties;
    }

    @PostMapping(value = PATH)
    public String launch(
            @RequestParam("id_token") String idToken,
            @RequestParam("state") String state,
            HttpServletRequest httpRequest,
            Model model
    ) {
        if (this.localProperties.enabled()) {
            return "redirect:http://localhost:8080/meetingLocal";
        }
        // verify LTI claims, etc.
        LTIUser ltiUser = this.ltiService.authenticated(idToken, state,httpRequest);
        TeamsMeeting teamsMeeting = this.meetingService.create(ltiUser, subject, httpRequest);
        JWT jwt = this.ltiService.deepLinkingResponseToken(subject,teamsMeeting.url(),httpRequest);
        model.addAttribute("responseUrl", this.ltiService.deepLinkingResponseURL(httpRequest));
        model.addAttribute("jwt", jwt.serialize());
        return "deeplinking-response-meeting";
    }

    @PostMapping(value = PATH_LOCAL)
    public String launchLocal(
            @RequestParam("id_token") String idToken,
            @RequestParam("state") String state,
            HttpServletRequest httpRequest,
            Model model) {
        // verify LTI claims, etc.
        LTIUser ltiUser = this.ltiService.authenticated(idToken, state,httpRequest);
        TeamsMeeting teamsMeeting = this.meetingService.create(ltiUser, subject, httpRequest);
        JWT jwt = this.ltiService.deepLinkingResponseToken(subject,teamsMeeting.url(),httpRequest);
        model.addAttribute("responseUrl", this.ltiService.deepLinkingResponseURL(httpRequest));
        model.addAttribute("jwt", jwt.serialize());
        return "deeplinking-response-meeting";
    }

    @GetMapping(value = PATH_GET)
    public String get(
            @PathVariable("id") String id,
            PreAuthenticatedAuthenticationToken user) {
        TeamsMeeting meeting = this.meetingService.get(id);
        return "redirect:%s".formatted(meeting.joinURL());
    }
}
