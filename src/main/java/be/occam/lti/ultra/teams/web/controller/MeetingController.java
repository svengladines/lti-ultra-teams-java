package be.occam.lti.ultra.teams.web.controller;

import be.occam.lti.ultra.teams.config.SystemProperties;
import be.occam.lti.ultra.teams.config.feature.LocalProperties;
import be.occam.lti.ultra.teams.domain.LTILaunchType;
import be.occam.lti.ultra.teams.domain.LTIUser;
import be.occam.lti.ultra.teams.domain.TeamsMeeting;
import be.occam.lti.ultra.teams.domain.service.LTIService;
import be.occam.lti.ultra.teams.domain.service.MeetingService;
import be.occam.lti.ultra.teams.web.dto.MeetingDTO;
import com.nimbusds.jwt.JWT;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
public class MeetingController {

    public static final String VIEW_PATH = "/meeting/{organizer}/{id}";

    public static final String RESOURCE_COLLECTION_PATH = "/api/meetings";
    public static final String RESOURCE_SINGLE_PATH = "/api/meetings/{id}";

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

    @GetMapping(value = RESOURCE_SINGLE_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<MeetingDTO> get(
            @PathVariable("organizer") String organizer,
            @PathVariable("id") String id,
            Model model) {
        Optional<TeamsMeeting> oMeeting = this.meetingService.get(organizer,id);
        if (oMeeting.isPresent()) {
            return new ResponseEntity<>(map(oMeeting.get()), HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

    }

    @GetMapping(value = VIEW_PATH, produces = MediaType.TEXT_HTML_VALUE)
    public String view(
            @PathVariable("organizer") String organizer,
            @PathVariable("id") String id,
            Model model) {
            this.meetingService.get(organizer,id).ifPresent(m -> {
                model.addAttribute("meeting", map(m));
            });
        return "meeting/view";
    }

    @PostMapping(value = RESOURCE_COLLECTION_PATH, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String postForm(
            @ModelAttribute MeetingDTO meetingDTO,
            HttpServletRequest httpRequest,
            Model model) {
        try {
            String subject = meetingDTO.getSubject();
            TeamsMeeting teamsMeeting = this.meetingService.create(meetingDTO.getOrganizer(), subject, httpRequest).orElseThrow(() -> new RuntimeException("could not create meeting"));
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
