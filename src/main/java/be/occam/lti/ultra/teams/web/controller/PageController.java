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
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.net.URI;

@Controller
public class PageController {

    public static final String RESOURCE_PATH = "/pages/{area}/{page}.html";

    protected final LTIService ltiService;
    protected final MeetingService meetingService;
    protected final LocalProperties localProperties;

    @Autowired
    public PageController(LTIService ltiService, MeetingService meetingService, LocalProperties localProperties) {
        this.ltiService = ltiService;
        this.meetingService = meetingService;
        this.localProperties = localProperties;
    }

    @GetMapping(value = RESOURCE_PATH)
    public String get(
            @PathVariable("area") String area,
            @PathVariable("page") String page
    ) {
        return "%s/%s".formatted(area,page);
    }
}
